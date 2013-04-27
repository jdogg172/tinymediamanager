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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class UpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class MovieUpdateDatasourceTask extends TmmThreadPool {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieUpdateDatasourceTask.class);

  /** The data sources. */
  private List<String>        dataSources;

  /** The file types. */
  private List<String>        fileTypes;

  /** The movie list. */
  private MovieList           movieList;

  /**
   * Instantiates a new scrape task.
   * 
   */
  public MovieUpdateDatasourceTask() {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
    fileTypes = new ArrayList<String>(Globals.settings.getVideoFileType());
    initThreadPool(3, "update");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Void doInBackground() {
    try {
      startProgressBar("prepare scan...");
      for (String path : dataSources) {
        ArrayList<File> mov = getRootMovieDirs(new File(path), 0);
        // remove dupe dirs
        HashSet<File> h = new HashSet<File>(mov);
        mov.clear();
        mov.addAll(h);

        for (File movieDir : mov) {
          submitTask(new FindMovieTask(movieDir, path));
        }
      }

      waitForCompletionOrCancel();

      LOGGER.info("removing orphaned movies...");
      startProgressBar("cleanup...");
      for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
        Movie movie = movieList.getMovies().get(i);
        File movieDir = new File(movie.getPath());
        if (!movieDir.exists()) {
          movieList.removeMovie(movie);
        }
      }
      LOGGER.info("Done updating datasource :)");

      LOGGER.info("get MediaInfo...");
      // update MediaInfo
      startProgressBar("getting Mediainfo...");
      initThreadPool(1, "mediainfo");
      for (Movie m : movieList.getMovies()) {
        submitTask(new MediaFileInformationFetcherTask(m));
      }
      waitForCompletionOrCancel();
      if (cancel) {
        cancel(false);// swing cancel
      }
      LOGGER.info("Done getting MediaInfo)");

    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
    }
    return null;
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
      parseMovieDirectory(subdir, datasource);
      return subdir.getName();
    }
  }

  /**
   * parses the complete movie directory, andd adds a movie with all found MediaFiles
   * 
   * @param movieDir
   * @param dataSource
   */
  private void parseMovieDirectory(File movieDir, String dataSource) {
    try {
      Movie movie = movieList.getMovieByPath(movieDir.getName());
      if (movie == null) {
        LOGGER.info("parsing movie " + movieDir);
        movie = new Movie();

        ArrayList<MediaFile> mfs = getAllMediaFilesRecursive(movieDir);

        // first round - try to parse NFO(s) first
        for (MediaFile mf : mfs) {
          if (mf.getType().equals(MediaFileType.NFO)) {
            LOGGER.debug("parsing NFO " + mf.getFilename());
            Movie nfo = null;
            switch (Globals.settings.getMovieSettings().getMovieConnector()) {
              case XBMC:
                nfo = MovieToXbmcNfoConnector.getData(mf.getPath() + File.separator + mf.getFilename());
                break;

              case MP:
                nfo = MovieToMpNfoConnector.getData(mf.getPath() + File.separator + mf.getFilename());
                break;
            }
            if (nfo != null) {
              movie = nfo;
            }
          }
        }

        if (movie.getTitle().isEmpty()) {
          movie.setTitle(ParserUtils.detectCleanMoviename(movieDir.getName()));
        }
        if (movie.getPath().isEmpty()) {
          movie.setPath(movieDir.getPath());
        }

        // second round - now add all the other knwon files
        for (MediaFile mf : mfs) {

          if (mf.getPath().toUpperCase().contains("BDMV") || mf.getPath().toUpperCase().contains("VIDEO_TS")) {
            movie.setDisc(true);
          }

          if (mf.getType().equals(MediaFileType.VIDEO)) {
            LOGGER.debug("parsing video file " + mf.getFilename());
            movie.addToMediaFiles(mf);
          }
          else if (mf.getType().equals(MediaFileType.TRAILER)) {
            LOGGER.debug("parsing trailer " + mf.getFilename());
            MediaTrailer mt = new MediaTrailer();
            mt.setName(mf.getFilename());
            mt.setProvider("downloaded");
            mt.setQuality("unknown");
            mt.setInNfo(false);
            mt.setUrl(new File(mf.getPath()).toURI().toString());
            movie.addTrailer(mt);
            movie.addToMediaFiles(mf);
          }
          else if (mf.getType().equals(MediaFileType.SUBTITLE)) {
            LOGGER.debug("parsing subtitle " + mf.getFilename());
            if (!mf.isPacked()) {
              movie.setSubtitles(true);
              movie.addToMediaFiles(mf);
            }
          }
          else if (mf.getType().equals(MediaFileType.POSTER)) {
            LOGGER.debug("parsing poster " + mf.getFilename());
            movie.setPoster(mf.getFilename());
            movie.addToMediaFiles(mf);
          }
          else if (mf.getType().equals(MediaFileType.FANART)) {
            LOGGER.debug("parsing fanart " + mf.getFilename());
            movie.setFanart(mf.getFilename());
            movie.addToMediaFiles(mf);
          }
        }

        // third round - try to match unknown graphics like title.ext or filenmae.ext
        for (MediaFile mf : mfs) {
          if (mf.getType().equals(MediaFileType.GRAPHIC)) {
            LOGGER.debug("parsing unknown graphic " + mf.getFilename());
            List<MediaFile> vid = movie.getMediaFiles(MediaFileType.VIDEO);
            if (vid != null && !vid.isEmpty()) {
              String vfilename = FilenameUtils.getBaseName(vid.get(0).getFilename());
              if (vfilename.equals(FilenameUtils.getBaseName(mf.getFilename()))) {
                // ok, basename matches - must be poster
                mf.setType(MediaFileType.POSTER);
                movie.setPoster(mf.getFilename());
                movie.addToMediaFiles(mf);
              }
              else if (Utils.cleanStackingMarkers(vfilename).trim().equals(FilenameUtils.getBaseName(mf.getFilename()))) {
                // ok, basename matches without stacking information - must be poster
                mf.setType(MediaFileType.POSTER);
                movie.setPoster(mf.getFilename());
                movie.addToMediaFiles(mf);
              }
              else if (movie.getTitle().equals(FilenameUtils.getBaseName(mf.getFilename()))) {
                // ok, basename matches movietitle - must be poster as well
                mf.setType(MediaFileType.POSTER);
                movie.setPoster(mf.getFilename());
                movie.addToMediaFiles(mf);
              }
            }
          }
        }

        movie.setDataSource(dataSource);
        movie.setDateAdded(new Date());
        LOGGER.debug("store movie into DB " + movieDir.getName());
        movie.saveToDb();
        if (movie.getMovieSet() != null) {
          LOGGER.debug("movie is part of a movieset");
          movie.getMovieSet().addMovie(movie);
          movieList.sortMoviesInMovieSet(movie.getMovieSet());
          movie.getMovieSet().saveToDb();
          movie.saveToDb();
        }
        LOGGER.info("add movie to GUI");
        movieList.addMovie(movie);
      }
      else {
        LOGGER.info("Movie already in DB - do nothing");
      }
    }
    catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * searches for file type VIDEO and tries to detect the root movie directory
   * 
   * @param directory
   *          start dir
   * @param level
   *          the level how deep we are (start with 0)
   * @return arraylist of abolute movie dirs
   */
  public ArrayList<File> getRootMovieDirs(File directory, int level) {
    ArrayList<File> ar = new ArrayList<File>();

    // separate files & dirs
    ArrayList<File> files = new ArrayList<File>();
    ArrayList<File> dirs = new ArrayList<File>();
    File[] list = directory.listFiles();
    for (File file : list) {
      if (file.isFile()) {
        files.add(file);
      }
      else {
        dirs.add(file);
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
        if (dirs.isEmpty() && level > 1
            && (!Utils.getStackingMarker(f.getName()).isEmpty() || !Utils.getStackingMarker(moviedir.getName()).isEmpty())) {
          // no more dirs in that directory
          // and at least 2 levels deep
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

  /**
   * recursively gets all MediaFiles from a moviedir
   * 
   * @param dir
   *          the movie root dir
   * @return list of files
   */
  public ArrayList<MediaFile> getAllMediaFilesRecursive(File dir) {
    ArrayList<MediaFile> mv = new ArrayList<MediaFile>();

    File[] list = dir.listFiles();
    for (File file : list) {
      if (file.isFile()) {
        mv.add(new MediaFile(file));
      }
      else {
        mv.addAll(getAllMediaFilesRecursive(file));
      }
    }

    return mv;
  }

  /*
   * Executed in event dispatching thread
   */
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  public void done() {
    stopProgressBar();
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description, int max, int progress) {
    if (!StringUtils.isEmpty(description)) {
      lblProgressAction.setText(description);
    }
    progressBar.setVisible(true);
    progressBar.setIndeterminate(false);
    progressBar.setMaximum(max);
    progressBar.setValue(progress);
    btnCancelTask.setVisible(true);
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description) {
    if (!StringUtils.isEmpty(description)) {
      lblProgressAction.setText(description);
    }
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
    btnCancelTask.setVisible(true);
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setIndeterminate(false);
    progressBar.setVisible(false);
    btnCancelTask.setVisible(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.ui.TmmSwingWorker#cancel()
   */
  @Override
  public void cancel() {
    cancel = true;
    // cancel(false);
  }

  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }
}
