/*
 * Copyright 2012 - 2013 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.ImageCacheTask;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class UpdateDataSourcesTask.
 * 
 * @author Myron Boyle
 */

public class MovieUpdateDatasourceTask extends TmmThreadPool {
  private static final Logger       LOGGER      = LoggerFactory.getLogger(MovieUpdateDatasourceTask.class);

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String> skipFolders = Arrays.asList(".", "..", ".ACTORS", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF",
                                                    "AUXDATA", "AUDIO_TS", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION");

  private List<String>              dataSources;
  private MovieList                 movieList;
  private HashSet<File>             filesFound  = new HashSet<File>();

  public MovieUpdateDatasourceTask() {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
  }

  public MovieUpdateDatasourceTask(String datasource) {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(1);
    dataSources.add(datasource);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Void doInBackground() {
    try {
      long start = System.currentTimeMillis();
      List<File> imageFiles = new ArrayList<File>();

      // cleanup just added for a new UDS run
      for (Movie movie : movieList.getMovies()) {
        movie.justAdded = false;
      }

      for (String ds : dataSources) {
        startProgressBar("prepare scan '" + ds + "'");
        if (Globals.settings.getMovieSettings().isDetectMovieMultiDir()) {
          initThreadPool(1, "update"); // use only one, since the multiDir detection relies on accurate values...
        }
        else {
          initThreadPool(3, "update");
        }
        File[] dirs = new File(ds).listFiles();
        if (dirs == null || dirs.length == 0) {
          // error - continue with next datasource
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
              new String[] { ds }));
          continue;
        }

        boolean parseDsRoot = false;
        for (File file : dirs) {
          if (!cancel) {
            if (file.isDirectory()) {
              String directoryName = file.getName();
              // check against unwanted dirs
              if (skipFolders.contains(directoryName.toUpperCase())) {
                LOGGER.info("ignoring directory " + directoryName);
                continue;
              }

              // dig deeper in this dir
              submitTask(new FindMovieTask(file, ds));
            }
            else {
              if (Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()))) {
                if (Globals.settings.getMovieSettings().isDetectMovieMultiDir()) {
                  parseDsRoot = true; // at least on movie found in DS root
                }
                else {
                  MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.movieinroot",
                      new String[] { file.getName() }));
                }
              }
            }
          }
        }

        if (parseDsRoot) {
          LOGGER.debug("parsing datasource root for movies...");
          parseMovieDirectory(new File(ds), ds);
        }

        waitForCompletionOrCancel();

        if (cancel) {
          break;
        }

        // cleanup
        cleanup(ds);

        // mediainfo
        gatherMediainfo(ds);

        waitForCompletionOrCancel();
        if (cancel) {
          break;
        }

        // build image cache on import
        if (Globals.settings.getMovieSettings().isBuildImageCacheOnImport()) {
          for (Movie movie : movieList.getMovies()) {
            if (!new File(ds).equals(new File(movie.getDataSource()))) {
              // check only movies matching datasource
              continue;
            }
            imageFiles.addAll(movie.getImagesToCache());
          }
        }

      } // END datasource loop

      if (imageFiles.size() > 0) {
        ImageCacheTask task = new ImageCacheTask(imageFiles);
        Globals.executor.execute(task);
      }

      long end = System.currentTimeMillis();
      LOGGER.info("Done updating datasource :) - took " + Utils.MSECtoHHMMSS(end - start));

      if (cancel) {
        cancel(false);// swing cancel
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
    return null;
  }

  /**
   * parses a list of VIDEO files in a dir and creates movies out of it
   */
  private void parseMultiMovieDir(File[] files, File parentDir, String datasource) {
    if (files == null || files.length == 0) {
      return;
    }
    List<File> completeDirContents = new ArrayList<File>(Arrays.asList(parentDir.listFiles()));

    // just compare filename length, to start with longest
    Arrays.sort(files, new Comparator<File>() {
      public int compare(File file1, File file2) {
        return file2.getName().length() - file1.getName().length();
      }
    });
    for (File file : files) {

      Movie movie = null;
      MediaFile mf = new MediaFile(file);
      String basename = Utils.cleanStackingMarkers(mf.getBasename());

      // 1) check if MF is already assigned to a movie within path
      for (Movie m : movieList.getMoviesByPath(mf.getFile().getParentFile())) {
        if (m.getMediaFiles(MediaFileType.VIDEO).contains(mf)) {
          // ok, our MF is already in an movie
          LOGGER.debug("found movie from MediaFIle");
          movie = m;
          break;
        }
        for (MediaFile mfile : m.getMediaFiles(MediaFileType.VIDEO)) {
          // try to match like if we would create a new movie
          if (ParserUtils.detectCleanMoviename(Utils.cleanStackingMarkers(mfile.getBasename())).equals(
              ParserUtils.detectCleanMoviename(Utils.cleanStackingMarkers(mf.getBasename())))) {
            LOGGER.debug("found possible movie from filename");
            movie = m;
            break;
          }
        }
      }

      if (movie == null) {
        // 2) create if not found
        // check for NFO
        File nfoFile = new File(datasource, basename + ".nfo");
        if (completeDirContents.contains(nfoFile)) {
          MediaFile nfo = new MediaFile(nfoFile, MediaFileType.NFO);
          // from NFO?
          LOGGER.debug("found NFO - try to parse");
          switch (Globals.settings.getMovieSettings().getMovieConnector()) {
            case XBMC:
              movie = MovieToXbmcNfoConnector.getData(nfo.getFile());
              break;
            case MP:
              movie = MovieToMpNfoConnector.getData(nfo.getFile());
              break;
          }
          if (movie != null) {
            // valid NFO found, so add itself as MF
            LOGGER.debug("NFO valid - add it");
            movie.addToMediaFiles(nfo);
          }
        }
        if (movie == null) {
          // still NULL, create new movie movie from file
          LOGGER.debug("create new movie");
          movie = new Movie();
          String[] ty = ParserUtils.detectCleanMovienameAndYear(basename);
          movie.setTitle(ty[0]);
          if (!ty[1].isEmpty()) {
            movie.setYear(ty[1]);
          }
          movie.setDateAdded(new Date());
          movie.saveToDb();
        }
        movie.setDataSource(datasource);
        movie.setNewlyAdded(true);
        movie.setPath(mf.getPath());
      }

      if (!Utils.isValidImdbId(movie.getImdbId())) {
        movie.setImdbId(ParserUtils.detectImdbId(mf.getFile().getAbsolutePath()));
      }
      movie.addToMediaFiles(mf);
      movie.setMultiMovieDir(true);

      // 3) find additional files, which start with videoFileName
      List<MediaFile> foundMediaFiles = new ArrayList<MediaFile>();
      for (int i = completeDirContents.size() - 1; i >= 0; i--) {
        File fileInDir = completeDirContents.get(i);
        if (fileInDir.getName().startsWith(basename)) {
          MediaFile mediaFile = new MediaFile(fileInDir);
          if (!movie.getMediaFiles().contains(mediaFile)) {
            // store file for faster cleanup
            synchronized (filesFound) {
              filesFound.add(fileInDir);
            }
            foundMediaFiles.add(mediaFile);
          }
          // started with basename, so remove it for others
          completeDirContents.remove(i);
        }
      }
      addMediafilesToMovie(movie, foundMediaFiles);

      movie.saveToDb();
      if (movie.getMovieSet() != null) {
        LOGGER.debug("movie is part of a movieset");
        // movie.getMovieSet().addMovie(movie);
        movie.getMovieSet().insertMovie(movie);
        movieList.sortMoviesInMovieSet(movie.getMovieSet());
        movie.getMovieSet().saveToDb();
        movie.saveToDb();
      }
      movie.justAdded = true;
      movieList.addMovie(movie);
    } // end for every file
  }

  /**
   * parses the complete movie directory, and adds a movie with all found MediaFiles
   */
  private void parseMovieDirectory(File movieDir, String dataSource) {
    try {
      // store dir for faster cleanup
      synchronized (filesFound) {
        filesFound.add(movieDir);
      }

      // list all type VIDEO files
      File[] files = movieDir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return new MediaFile(new File(dir, name)).getType().equals(MediaFileType.VIDEO); // no trailer or extra vids!
        }
      });

      // check if we have more than one movie in dir
      HashSet<String> h = new HashSet<String>();
      for (File file : files) {
        MediaFile mf = new MediaFile(file);
        if (mf.isDiscFile()) {
          // ignore disc files when trying to detect multi movie dir!
          continue;
        }
        h.add(ParserUtils.detectCleanMoviename(Utils.cleanStackingMarkers(FilenameUtils.getBaseName(file.getName()))));
      }
      // more than 1, or if DS=dir then assume a multi dir (only second level is a normal movie dir)
      if (h.size() > 1 || movieDir.equals(new File(dataSource))) {
        LOGGER.debug("WOOT - we have a multi movie directory: " + movieDir);
        if (Globals.settings.getMovieSettings().isDetectMovieMultiDir()) {
          parseMultiMovieDir(files, movieDir, dataSource);
        }
        else {
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.movieinroot",
              new String[] { movieDir.getName() }));
        }
      }
      else {
        LOGGER.debug("PAH - normal movie directory: " + movieDir);

        Movie movie = movieList.getMovieByPath(movieDir);
        ArrayList<MediaFile> mfs = getAllMediaFilesRecursive(movieDir);

        if (movie == null) {
          LOGGER.info("parsing movie " + movieDir);
          movie = new Movie();

          // first round - try to parse NFO(s) first
          for (MediaFile mf : mfs) {
            if (mf.getType().equals(MediaFileType.NFO)) {
              LOGGER.debug("parsing NFO " + mf.getFilename());
              Movie nfo = null;
              switch (Globals.settings.getMovieSettings().getMovieConnector()) {
                case XBMC:
                  nfo = MovieToXbmcNfoConnector.getData(mf.getFile());
                  break;

                case MP:
                  nfo = MovieToMpNfoConnector.getData(mf.getFile());
                  break;
              }
              if (nfo != null) {
                movie = nfo;
                movie.addToMediaFiles(mf);
              }
              else {
                // is NFO, but parsing exception. try to find at least imdb id within
                try {
                  String imdb = FileUtils.readFileToString(mf.getFile());
                  imdb = StrgUtils.substr(imdb, ".*(tt\\d{7}).*");
                  if (!imdb.isEmpty()) {
                    LOGGER.debug("Found IMDB id: " + imdb);
                    movie.setImdbId(imdb);
                  }
                }
                catch (IOException e) {
                  LOGGER.warn("couldn't read NFO " + mf.getFilename());
                }
              } // end NFO null
            }
          }

          if (movie.getTitle().isEmpty()) {
            String[] ty = ParserUtils.detectCleanMovienameAndYear(movieDir.getName());
            movie.setTitle(ty[0]);
            if (!ty[1].isEmpty()) {
              movie.setYear(ty[1]);
            }
          }
          movie.setPath(movieDir.getPath());
          movie.setDataSource(dataSource);
          movie.setDateAdded(new Date());
          movie.setNewlyAdded(true);

          movie.findActorImages(); // TODO: find as MediaFIles
          LOGGER.debug("store movie into DB " + movieDir.getName());
          movie.saveToDb(); // savepoint

          if (movie.getMovieSet() != null) {
            LOGGER.debug("movie is part of a movieset");
            // movie.getMovieSet().addMovie(movie);
            movie.getMovieSet().insertMovie(movie);
            movieList.sortMoviesInMovieSet(movie.getMovieSet());
            movie.getMovieSet().saveToDb();
            movie.saveToDb();
          }

        } // end movie is null

        // second round - now add all the other known files
        addMediafilesToMovie(movie, mfs);

        // third round - try to match unknown graphics like title.ext or filename.ext as poster
        if (movie.getPoster().isEmpty()) {
          for (MediaFile mf : mfs) {
            if (mf.getType().equals(MediaFileType.GRAPHIC)) {
              LOGGER.debug("parsing unknown graphic " + mf.getFilename());
              List<MediaFile> vid = movie.getMediaFiles(MediaFileType.VIDEO);
              if (vid != null && !vid.isEmpty()) {
                String vfilename = FilenameUtils.getBaseName(vid.get(0).getFilename());
                if (vfilename.equals(FilenameUtils.getBaseName(mf.getFilename())) // basename match
                    || Utils.cleanStackingMarkers(vfilename).trim().equals(FilenameUtils.getBaseName(mf.getFilename())) // basename w/o stacking
                    || movie.getTitle().equals(FilenameUtils.getBaseName(mf.getFilename()))) { // title match
                  mf.setType(MediaFileType.POSTER);
                  movie.addToMediaFiles(mf);
                }
              }
            }
          }
        }

        movie.saveToDb();
        movie.justAdded = true;
        movieList.addMovie(movie);
      }
    }
    catch (NullPointerException e) {
      LOGGER.error("NPE:", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movieDir.getPath(), "message.update.errormoviedir"));
    }
    catch (Exception e) {
      LOGGER.error("error update Datasources", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movieDir.getPath(), "message.update.errormoviedir", new String[] { ":",
          e.getLocalizedMessage() }));
    }
  }

  /**
   * searches for file type VIDEO and tries to detect the root movie directory
   * 
   * @param directory
   *          start dir
   * @param level
   *          the level how deep we are (level 0 = datasource root)
   * @return arraylist of abolute movie dirs
   */
  private ArrayList<File> getRootMovieDirs(File directory, int level) {
    ArrayList<File> ar = new ArrayList<File>();

    // separate files & dirs
    ArrayList<File> files = new ArrayList<File>();
    ArrayList<File> dirs = new ArrayList<File>();
    File[] list = directory.listFiles();
    if (list == null) {
      LOGGER.error("Whops. Cannot access directory: " + directory);
      return ar;
    }
    for (File file : list) {
      if (file.isFile()) {
        files.add(file);
      }
      else {
        // ignore .folders and others
        if (!skipFolders.contains(file.getName().toUpperCase())) {
          dirs.add(file);
        }
      }
    }
    list = null;

    for (File f : files) {
      boolean disc = false;
      MediaFile mf = new MediaFile(f);

      if (mf.getType().equals(MediaFileType.VIDEO)) {

        // get current folder
        File moviedir = f.getParentFile();

        // walk reverse till disc root (if found)
        while (moviedir.getPath().toUpperCase().contains("BDMV") || moviedir.getPath().toUpperCase().contains("VIDEO_TS")) {
          disc = true;
          moviedir = moviedir.getParentFile();
        }
        if (disc) {
          ar.add(moviedir);
          continue; // proceed with next file
        }

        // ok, regular structure
        if (dirs.isEmpty() && level > 1 && !Utils.getStackingMarker(moviedir.getName()).isEmpty()) {
          // no more dirs in that directory and at least 2 levels deep
          // stacking found (either on file or parent dir)
          // -> assume parent as movie dir"
          moviedir = moviedir.getParentFile();
          ar.add(moviedir);
        }
        else {
          // -> assume current dir as movie dir"
          ar.add(moviedir);
        }
      }
    }

    for (File dir : dirs) {
      ar.addAll(getRootMovieDirs(dir, level + 1));
    }

    return ar;
  }

  /*
   * recursively gets all MediaFiles from a moviedir
   */
  private ArrayList<MediaFile> getAllMediaFilesRecursive(File dir) {
    ArrayList<MediaFile> mv = new ArrayList<MediaFile>();

    File[] list = dir.listFiles();
    for (File file : list) {
      if (file.isFile()) {
        mv.add(new MediaFile(file));
        // store dir for faster cleanup
        synchronized (filesFound) {
          filesFound.add(file);
        }
      }
      else {
        // ignore .folders and others
        if (!skipFolders.contains(file.getName().toUpperCase())) {
          mv.addAll(getAllMediaFilesRecursive(file));
        }
      }
    }

    return mv;
  }

  private void addMediafilesToMovie(Movie movie, List<MediaFile> mediaFiles) {
    List<MediaFile> current = movie.getMediaFiles();

    for (MediaFile mf : mediaFiles) {
      if (!current.contains(mf)) { // a new mediafile was found!
        if (mf.getPath().toUpperCase().contains("BDMV") || mf.getPath().toUpperCase().contains("VIDEO_TS") || mf.isDiscFile()) {
          movie.setDisc(true);
        }

        if (!Utils.isValidImdbId(movie.getImdbId())) {
          movie.setImdbId(ParserUtils.detectImdbId(mf.getFile().getAbsolutePath()));
        }

        switch (mf.getType()) {
          case VIDEO:
            LOGGER.debug("parsing video file " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case VIDEO_EXTRA:
            LOGGER.debug("parsing extra " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case TRAILER:
            LOGGER.debug("parsing trailer " + mf.getFilename());
            mf.gatherMediaInformation(); // do this exceptionally here, to set quality in one rush
            MediaTrailer mt = new MediaTrailer();
            mt.setName(mf.getFilename());
            mt.setProvider("downloaded");
            mt.setQuality(mf.getVideoFormat());
            mt.setInNfo(false);
            mt.setUrl(mf.getFile().toURI().toString());
            movie.addTrailer(mt);
            movie.addToMediaFiles(mf);
            break;

          case SUBTITLE:
            LOGGER.debug("parsing subtitle " + mf.getFilename());
            if (!mf.isPacked()) {
              movie.setSubtitles(true);
              movie.addToMediaFiles(mf);
            }
            break;

          case POSTER:
            LOGGER.debug("parsing poster " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case FANART:
            if (mf.getPath().toLowerCase().contains("extrafanart")) {
              // there shouldn't be any files here
              LOGGER.warn("problem: detected media file type FANART in extrafanart folder: " + mf.getPath());
              continue;
            }
            LOGGER.debug("parsing fanart " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case EXTRAFANART:
            LOGGER.debug("parsing extrafanart " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case THUMB:
            LOGGER.debug("parsing thumbnail " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case AUDIO:
            LOGGER.debug("parsing audio stream " + mf.getFilename());
            movie.addToMediaFiles(mf);
            break;

          case GRAPHIC:
          case UNKNOWN:
          default:
            LOGGER.debug("NOT adding unknown media file type: " + mf.getFilename());
            // movie.addToMediaFiles(mf); // DO NOT ADD UNKNOWN
            break;
        } // end switch type
      } // end new MF found
    } // end MF loop
  }

  /*
   * cleanup database - remove orphaned movies/files
   */
  private void cleanup(String datasource) {
    startProgressBar("database cleanup...");
    LOGGER.info("removing orphaned movies/files...");
    for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      Movie movie = movieList.getMovies().get(i);

      // check only movies matching datasource
      if (!new File(datasource).equals(new File(movie.getDataSource()))) {
        continue;
      }

      File movieDir = new File(movie.getPath());
      if (!filesFound.contains(movieDir)) {
        // dir is not in hashset - check with exists to be sure it is not here
        if (!movieDir.exists()) {
          LOGGER.debug("movie directory '" + movieDir + "' not found, removing...");
          movieList.removeMovie(movie);
        }
        else {
          LOGGER.warn("dir " + movie.getPath() + " not in hashset, but on hdd!");
        }
      }
      else {
        // have a look if that movie has just been added -> so we don't need any cleanup
        if (!movie.justAdded) {
          // check and delete all not found MediaFiles
          boolean dirty = false;
          List<MediaFile> mediaFiles = new ArrayList<MediaFile>(movie.getMediaFiles());
          for (MediaFile mf : mediaFiles) {
            if (!filesFound.contains(mf.getFile())) {
              if (!mf.exists()) {
                movie.removeFromMediaFiles(mf);
                dirty = true;
              }
              else {
                LOGGER.warn("file " + mf.getFile().getAbsolutePath() + " not in hashset, but on hdd!");
              }
            }
          }
          if (dirty) {
            movie.saveToDb();
          }
        }
      }
    }
  }

  /*
   * gather mediainfo for ungathered movies
   */
  private void gatherMediainfo(String datasource) {
    // start MI
    initThreadPool(1, "mediainfo");
    startProgressBar("getting Mediainfo");
    LOGGER.info("getting Mediainfo...");
    for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      Movie movie = movieList.getMovies().get(i);

      // check only movies matching datasource
      if (!new File(datasource).equals(new File(movie.getDataSource()))) {
        continue;
      }

      ArrayList<MediaFile> ungatheredMediaFiles = new ArrayList<MediaFile>();
      for (MediaFile mf : movie.getMediaFiles()) {
        if (StringUtils.isBlank(mf.getContainerFormat())) {
          ungatheredMediaFiles.add(mf);
        }
      }

      if (ungatheredMediaFiles.size() > 0) {
        submitTask(new MediaFileInformationFetcherTask(ungatheredMediaFiles, movie, false));
      }
    }
  }

  /*
   * Executed in event dispatching thread
   */
  @Override
  public void done() {
    stopProgressBar();
  }

  @Override
  public void cancel() {
    cancel = true;
  }

  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }

  /**
   * ThreadpoolWorker to work off ONE possible movie from root datasource directory
   * 
   * @author Myron Boyle
   * @version 1.0
   */
  private class FindMovieTask implements Callable<Object> {

    private File   subdir     = null;
    private String datasource = "";

    public FindMovieTask(File subdir, String datasource) {
      this.subdir = subdir;
      this.datasource = datasource;
    }

    @Override
    public String call() throws Exception {
      // find all possible movie folders recursive
      ArrayList<File> mov = getRootMovieDirs(subdir, 1);

      // remove dupe movie dirs
      HashSet<File> h = new HashSet<File>(mov);
      mov.clear();
      mov.addAll(h);
      for (File movieDir : mov) {
        // check if multiple movies or a single one
        parseMovieDirectory(movieDir, datasource);
      }

      // return first level folder name... uhm. yeah
      return subdir.getName();
    }
  }
}
