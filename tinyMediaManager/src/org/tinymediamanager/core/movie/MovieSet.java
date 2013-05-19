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
package org.tinymediamanager.core.movie;

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class MovieSet.
 * 
 * @author Manuel Laggner
 */
@Entity
public class MovieSet extends MediaEntity {

  /** The static LOGGER. */
  private static final Logger LOGGER           = LoggerFactory.getLogger(MovieSet.class);

  /** The movies. */
  private List<Movie>         movies           = new ArrayList<Movie>();

  /** The movies observable. */
  @Transient
  private List<Movie>         moviesObservable = ObservableCollections.observableList(movies);

  /**
   * Instantiates a new movieset. To initialize the propertychangesupport after loading
   */
  public MovieSet() {
  }

  /**
   * Instantiates a new movie set.
   * 
   * @param title
   *          the title
   */
  public MovieSet(String title) {
    setTitle(title);
  }

  /**
   * Sets the observable cast list.
   */
  public void setObservables() {
    moviesObservable = ObservableCollections.observableList(movies);
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    int id = 0;
    try {
      id = (Integer) ids.get("tmdbId");
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param newValue
   *          the new tmdb id
   */
  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put("tmdbId", newValue);
    firePropertyChange(TMDBID, oldValue, newValue);
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    super.setPosterUrl(newValue);
    String posterFilename = "movieset-poster.jpg";

    // write new poster
    writeImageToMovieFolder(moviesObservable, posterFilename, posterUrl);

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(true, false);
    }

    setPoster(posterFilename);
  }

  /**
   * Sets the fanart url.
   * 
   * @param newValue
   *          the new fanart url
   */
  public void setFanartUrl(String newValue) {
    super.setFanartUrl(newValue);
    String fanartFilename = "movieset-fanart.jpg";

    // write new fanart
    writeImageToMovieFolder(moviesObservable, fanartFilename, fanartUrl);

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(false, true);
    }

    setFanart(fanartFilename);
  }

  /**
   * Gets the fanart.
   * 
   * @return the fanart
   */
  public String getFanart() {
    String fanart = "";

    // try to get a fanart from one movie
    for (Movie movie : moviesObservable) {
      String filename = movie.getPath() + File.separator + "movieset-fanart.jpg";
      File fanartFile = new File(filename);
      if (fanartFile.exists()) {
        return filename;
      }
    }

    // we did not find an image from a movie - get the cached file from the url
    File cachedFile = new File(ImageCache.getCacheDir() + File.separator + ImageCache.getCachedFileName(fanartUrl) + ".jpg");
    if (cachedFile.exists()) {
      return cachedFile.getPath();
    }

    // no cached file found - cache it via thread
    if (StringUtils.isNotEmpty(fanartUrl)) {
      ImageFetcher task = new ImageFetcher("fanart", fanartUrl);
      Globals.executor.execute(task);
    }

    return fanart;
  }

  /**
   * Gets the poster.
   * 
   * @return the poster
   */
  public String getPoster() {
    String poster = "";

    // try to get a fanart from one movie
    for (Movie movie : moviesObservable) {
      String filename = movie.getPath() + File.separator + "movieset-poster.jpg";
      File posterFile = new File(filename);
      if (posterFile.exists()) {
        return filename;
      }
    }

    // we did not find an image from a movie - get the cached file from the url
    File cachedFile = new File(ImageCache.getCacheDir() + File.separator + ImageCache.getCachedFileName(posterUrl) + ".jpg");
    if (cachedFile.exists()) {
      return cachedFile.getPath();
    }

    // no cached file found - cache it via thread
    if (StringUtils.isNotEmpty(posterUrl)) {
      ImageFetcher task = new ImageFetcher("poster", posterUrl);
      Globals.executor.execute(task);
    }

    return poster;
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    moviesObservable.add(movie);
    saveToDb();

    // // look for an tmdbid if no one available
    // if (tmdbId == 0) {
    // searchTmdbId();
    // }

    // write images
    List<Movie> movies = new ArrayList<Movie>(1);
    movies.add(movie);
    writeImageToMovieFolder(movies, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(movies, "movieset-poster.jpg", posterUrl);

    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("addedMovie", null, movie);
  }

  /**
   * Removes the movie.
   * 
   * @param movie
   *          the movie
   */
  public void removeMovie(Movie movie) {
    // remove images from movie folder
    File imageFile = new File(movie.getPath() + File.separator + "movieset-fanart.jpg");
    if (imageFile.exists()) {
      imageFile.delete();
    }
    imageFile = new File(movie.getPath() + File.separator + "movieset-poster.jpg");
    if (imageFile.exists()) {
      imageFile.delete();
    }

    moviesObservable.remove(movie);
    saveToDb();

    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("removedMovie", null, movie);
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public List<Movie> getMovies() {
    return moviesObservable;
  }

  /**
   * Sort movies.
   */
  public void sortMovies() {
    Collections.sort(moviesObservable, new MovieInMovieSetComparator());
    firePropertyChange("movies", null, moviesObservable);
  }

  /**
   * Removes the all movies.
   */
  public void removeAllMovies() {
    // remove images from movie folder
    for (Movie movie : moviesObservable) {
      File imageFile = new File(movie.getPath() + File.separator + "movieset-fanart.jpg");
      if (imageFile.exists()) {
        imageFile.delete();
      }
      imageFile = new File(movie.getPath() + File.separator + "movieset-poster.jpg");
      if (imageFile.exists()) {
        imageFile.delete();
      }
    }

    // store all old movies to remove the nodes in the tree
    List<Movie> oldValue = new ArrayList<Movie>(moviesObservable.size());
    oldValue.addAll(moviesObservable);
    moviesObservable.clear();
    saveToDb();

    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("removedAllMovies", oldValue, moviesObservable);
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    synchronized (Globals.entityManager) {
      Globals.entityManager.getTransaction().begin();
      Globals.entityManager.persist(this);
      Globals.entityManager.getTransaction().commit();
    }
  }

  /**
   * toString. used for JComboBox in movie editor
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return getTitle();
  }

  /**
   * Gets the movie index.
   * 
   * @param movie
   *          the movie
   * @return the movie index
   */
  public int getMovieIndex(Movie movie) {
    return movies.indexOf(movie);
  }

  /**
   * Write image to movie folder.
   * 
   * @param movies
   *          the movies
   * @param filename
   *          the filename
   * @param url
   *          the url
   */
  private void writeImageToMovieFolder(List<Movie> movies, String filename, String url) {
    // check for empty strings or movies
    if (movies == null || movies.size() == 0 || StringUtils.isEmpty(filename) || StringUtils.isEmpty(url)) {
      return;
    }

    // write image for all movies
    for (Movie movie : movies) {
      try {
        writeImage(url, movie.getPath() + File.separator + filename);
      }
      catch (IOException e) {
        LOGGER.warn("could not write files", e);
      }
    }
  }

  /**
   * Rewrite all images.
   */
  public void rewriteAllImages() {
    writeImageToMovieFolder(moviesObservable, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(moviesObservable, "movieset-poster.jpg", posterUrl);

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(true, true);
    }
  }

  /**
   * Write images to artwork folder.
   * 
   * @param poster
   *          the poster
   * @param fanart
   *          the fanart
   */
  private void writeImagesToArtworkFolder(boolean poster, boolean fanart) {
    // write images to artwork folder
    File artworkFolder = new File(Globals.settings.getMovieSettings().getMovieSetArtworkFolder());

    // check if folder exists
    if (!artworkFolder.exists()) {
      artworkFolder.mkdirs();
    }

    // write files
    try {
      // poster
      if (poster && StringUtils.isNotBlank(posterUrl)) {
        String providedFiletype = FilenameUtils.getExtension(posterUrl);
        writeImage(posterUrl, artworkFolder.getPath() + File.separator + getTitle() + "-folder." + providedFiletype);
      }
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }

    try {
      // fanart
      if (fanart && StringUtils.isNotBlank(fanartUrl)) {
        String providedFiletype = FilenameUtils.getExtension(fanartUrl);
        writeImage(fanartUrl, artworkFolder.getPath() + File.separator + getTitle() + "-fanart." + providedFiletype);
      }
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }
  }

  /**
   * Write image.
   * 
   * @param url
   *          the url
   * @param pathAndFilename
   *          the path and filename
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void writeImage(String url, String pathAndFilename) throws IOException {
    CachedUrl cachedUrl = new CachedUrl(url);
    FileOutputStream outputStream = new FileOutputStream(pathAndFilename);
    InputStream is = cachedUrl.getInputStream();
    IOUtils.copy(is, outputStream);
    outputStream.close();
    is.close();
  }

  // /**
  // * Search tmdb id for this movieset.
  // */
  // public void searchTmdbId() {
  // try {
  // TmdbMetadataProvider tmdb = new TmdbMetadataProvider();
  // for (Movie movie : moviesObservable) {
  // MediaScrapeOptions options = new MediaScrapeOptions();
  // if (Utils.isValidImdbId(movie.getImdbId()) || movie.getTmdbId() > 0) {
  // options.setTmdbId(movie.getTmdbId());
  // options.setImdbId(movie.getImdbId());
  // MediaMetadata md = tmdb.getMetadata(options);
  // if (md.getTmdbIdSet() > 0) {
  // setTmdbId(md.getTmdbIdSet());
  // saveToDb();
  // break;
  // }
  // }
  // }
  // }
  // catch (Exception e) {
  // LOGGER.warn(e);
  // }
  // }

  /**
   * The Class ImageFetcher.
   * 
   * @author Manuel Laggner
   */
  private class ImageFetcher implements Runnable {

    /** The property name. */
    private String propertyName = "";

    /** The image url. */
    private String imageUrl     = "";

    /**
     * Instantiates a new image fetcher.
     * 
     * @param propertyName
     *          the property name
     * @param url
     *          the url
     */
    public ImageFetcher(String propertyName, String url) {
      this.propertyName = propertyName;
      this.imageUrl = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      String filename = ImageCache.getCachedFileName(imageUrl);
      File outputFile = new File(ImageCache.getCacheDir(), filename + ".jpg");

      try {
        CachedUrl url = new CachedUrl(imageUrl);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        InputStream is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        is.close();
        firePropertyChange(propertyName, "", filename);
      }
      catch (IOException e) {
        LOGGER.warn("error in image fetcher", e);
      }
    }
  }

  /**
   * The Class MovieInMovieSetComparator.
   * 
   * @author Manuel Laggner
   */
  private class MovieInMovieSetComparator implements Comparator<Movie> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Movie o1, Movie o2) {
      Collator collator = null;

      if (o1 == null || o2 == null) {
        return 0;
      }

      collator = Collator.getInstance();
      return collator.compare(o1.getSortTitle(), o2.getSortTitle());
      // return o1.getSortTitle().compareTo(o2.getSortTitle());
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getBanner()
   */
  @Override
  public String getBanner() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setPoster(java.lang.String)
   */
  @Override
  public void setPoster(String newValue) {
    String oldValue = this.poster;
    this.poster = newValue;
    firePropertyChange(POSTER, oldValue, newValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setBanner(java.lang.String)
   */
  @Override
  public void setBanner(String banner) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setFanart(java.lang.String)
   */
  @Override
  public void setFanart(String newValue) {
    String oldValue = this.fanart;
    this.fanart = newValue;
    firePropertyChange(FANART, oldValue, newValue);
  }

  /**
   * Gets the checks for images.
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    if (!StringUtils.isEmpty(poster) && !StringUtils.isEmpty(fanart)) {
      return true;
    }
    return false;
  }
}
