/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.movie.MovieConnectors;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.imdb.ImdbSiteDefinition;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.PosterSizes;

/**
 * The Class Settings.
 */
@XmlRootElement(name = "tinyMediaManager")
public class Settings extends AbstractModelObject {
  /** The Constant logger. */
  private static final Logger           LOGGER                         = Logger.getLogger(Settings.class);

  /** The instance. */
  private static Settings               instance;

  /** The Constant CONFIG_FILE. */
  private final static String           CONFIG_FILE                    = "config.xml";

  /** The Constant MOVIE_DATA_SOURCE. */
  private final static String           MOVIE_DATA_SOURCE              = "movieDataSource";

  /** The Constant PATH. */
  private final static String           PATH                           = "path";

  /** The Constant TITLE_PREFIX. */
  private final static String           TITLE_PREFIX                   = "titlePrefix";

  /** The Constant TITLE_PREFIX. */
  private final static String           PREFIX                         = "prefix";

  /** The Constant VIDEO_FILE_TYPE. */
  private final static String           VIDEO_FILE_TYPE                = "videoFileTypes";

  /** The Constant FILETYPE. */
  private final static String           FILETYPE                       = "filetype";

  /** The Constant PROXY_HOST. */
  private final static String           PROXY_HOST                     = "proxyHost";

  /** The Constant PROXY_PORT. */
  private final static String           PROXY_PORT                     = "proxyPort";

  /** The Constant PROXY_USERNAME. */
  private final static String           PROXY_USERNAME                 = "proxyUsername";

  /** The Constant PROXY_PASSWORD. */
  private final static String           PROXY_PASSWORD                 = "proxyPassword";

  /** The Constant SCRAPER_TMDB_LANGU. */
  private final static String           SCRAPER_TMDB_LANGU             = "scraperTmdbLanguage";

  /** The Constant IMAGE_TMDB_POSTER. */
  private final static String           IMAGE_TMDB_POSTER              = "imageTmdbPosterSize";

  /** The Constant IMAGE_TMDB_FANART. */
  private final static String           IMAGE_TMDB_FANART              = "imageTmdbFanartSize";

  /** The Constant IMAGE_EXTRATHUMBS. */
  private final static String           IMAGE_EXTRATHUMBS              = "imageExtraThumbs";

  /** The Constant IMAGE_EXTRATHUMBS_RESIZE. */
  private final static String           IMAGE_EXTRATHUMBS_RESIZE       = "imageExtraThumbsResize";

  /** The Constant IMAGE_EXTRATHUMBS_SIZE. */
  private final static String           IMAGE_EXTRATHUMBS_SIZE         = "imageExtraThumbsSize";

  /** The Constant IMAGE_EXTRATHUMBS_COUNT. */
  private final static String           IMAGE_EXTRATHUMBS_COUNT        = "imageExtraThumbsCount";

  /** The Constant IMAGE_EXTRAFANART. */
  private final static String           IMAGE_EXTRAFANART              = "imageExtraFanart";

  /** The Constant IMAGE_EXTRAFANART_COUNT. */
  private final static String           IMAGE_EXTRAFANART_COUNT        = "imageExtraFanartCount";

  /** The Constant ENABLE_MOVIESET_ARTWORK_FOLDER. */
  private final static String           ENABLE_MOVIESET_ARTWORK_FOLDER = "enableMovieSetArtworkFolder";

  /** The Constant MOVIESET_ARTWORK_FOLDER. */
  private final static String           MOVIESET_ARTWORK_FOLDER        = "movieSetArtworkFolder";

  /** The Constant CERTIFICATION_COUNTRY. */
  private final static String           CERTIFICATION_COUNTRY          = "certificationCountry";

  /** The Constant MOVIE_CONNECTOR. */
  private final static String           MOVIE_CONNECTOR                = "movieConnector";

  /** The Constant MOVIE_NFO_FILENAME. */
  private final static String           MOVIE_NFO_FILENAME             = "movieNfoFilename";

  /** The Constant MOVIE_POSTER_FILENAME. */
  private final static String           MOVIE_POSTER_FILENAME          = "moviePosterFilename";

  /** The Constant MOVIE_FANART_FILENAME. */
  private final static String           MOVIE_FANART_FILENAME          = "movieFanartFilename";

  /** The Constant FILENAME. */
  private final static String           FILENAME                       = "filename";

  /** The Constant MOVIE_RENAMER_PATHNAME. */
  private final static String           MOVIE_RENAMER_PATHNAME         = "movieRenamerPathname";

  /** The Constant MOVIE_RENAMER_FILENAME. */
  private final static String           MOVIE_RENAMER_FILENAME         = "movieRenamerFilename";

  /** The Constant MOVIE_SCRAPER. */
  private final static String           MOVIE_SCRAPER                  = "movieScraper";

  /** The Constant IMDB_SCRAPE_FOREIGN_LANGU. */
  private final static String           IMDB_SCRAPE_FOREIGN_LANGU      = "imdbScrapeForeignLanguage";

  /** The Constant IMDB_SITE. */
  private final static String           IMDB_SITE                      = "imdbSite";

  /** The Constant CLEAR_CACHE_SHUTDOWN. */
  private final static String           CLEAR_CACHE_SHUTDOWN           = "clearCacheShutdown";

  /** The Constant SCRAPE_BEST_IMAGE. */
  private final static String           SCRAPE_BEST_IMAGE              = "scrapeBestImage";

  /** The Constant IMAGE_SCRAPER_TMDB. */
  private final static String           IMAGE_SCRAPER_TMDB             = "imageScraperTmdb";

  /** The Constant IMAGE_SCRAPER_FANART_TV. */
  private final static String           IMAGE_SCRAPER_FANART_TV        = "imageScraperFanartTv";

  /** The Constant TRAILER_SCRAPER_TMDB. */
  private final static String           TRAILER_SCRAPER_TMDB           = "trailerScraperTmdb";

  /** The Constant TRAILER_SCRAPER_HD_TRAILERS. */
  private final static String           TRAILER_SCRAPER_HD_TRAILERS    = "trailerScraperHdTrailers";

  /** The Constant TRAILER_SCRAPER_OFDB. */
  private final static String           TRAILER_SCRAPER_OFDB           = "trailerScraperOfdb";

  /** The Constant WRITE_ACTOR_IMAGES. */
  private final static String           WRITE_ACTOR_IMAGES             = "writeActorImages";

  /** The video file types. */
  @XmlElementWrapper(name = TITLE_PREFIX)
  @XmlElement(name = PREFIX)
  private final List<String>            titlePrefix                    = ObservableCollections.observableList(new ArrayList<String>());

  /** The video file types. */
  @XmlElementWrapper(name = VIDEO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>            videoFileTypes                 = ObservableCollections.observableList(new ArrayList<String>());

  /** The movie data sources. */
  @XmlElementWrapper(name = MOVIE_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>            movieDataSources               = ObservableCollections.observableList(new ArrayList<String>());

  /** The movie nfo filenames. */
  @XmlElementWrapper(name = MOVIE_NFO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieNfoNaming>    movieNfoFilenames              = new ArrayList<MovieNfoNaming>();

  /** The movie poster filenames. */
  @XmlElementWrapper(name = MOVIE_POSTER_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MoviePosterNaming> moviePosterFilenames           = new ArrayList<MoviePosterNaming>();

  /** The movie fanart filenames. */
  @XmlElementWrapper(name = MOVIE_FANART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieFanartNaming> movieFanartFilenames           = new ArrayList<MovieFanartNaming>();

  /** The proxy host. */
  private String                        proxyHost;

  /** The proxy port. */
  private String                        proxyPort;

  /** The proxy username. */
  private String                        proxyUsername;

  /** The proxy password. */
  private String                        proxyPassword;

  /** The scraper tmdb language. */
  private Languages                     scraperTmdbLanguage            = Languages.en;

  /** The image tmdb poster size. */
  private PosterSizes                   imageTmdbPosterSize            = PosterSizes.w342;

  /** The image tmdb scraper. */
  private boolean                       imageScraperTmdb               = true;

  /** The image fanart tv scraper. */
  private boolean                       imageScraperFanartTv           = true;

  /** The image tmdb fanart size. */
  private FanartSizes                   imageTmdbFanartSize            = FanartSizes.original;

  /** The image extra thumbs. */
  private boolean                       imageExtraThumbs               = false;

  /** The image extra thumbs resize. */
  private boolean                       imageExtraThumbsResize         = true;

  /** The image extra thumbs size. */
  private int                           imageExtraThumbsSize           = 300;

  /** The image extra thumbs count. */
  private int                           imageExtraThumbsCount          = 5;

  /** The image extra fanart. */
  private boolean                       imageExtraFanart               = false;

  /** The image extra fanart count. */
  private int                           imageExtraFanartCount          = 5;

  /** The enable movie set artwork folder. */
  private boolean                       enableMovieSetArtworkFolder    = false;

  /** The movie set artwork folder. */
  private String                        movieSetArtworkFolder          = "MoviesetArtwork";

  /** The country for certification. */
  private CountryCode                   certificationCountry           = CountryCode.US;

  /** The movie connector. */
  private MovieConnectors               movieConnector                 = MovieConnectors.XBMC;

  /** The movie renamer pathname. */
  private String                        movieRenamerPathname           = "$T ($Y)";

  /** The movie renamer filename. */
  private String                        movieRenamerFilename           = "$T ($Y)";

  /** The imdb scrape foreign language. */
  private boolean                       imdbScrapeForeignLanguage      = false;

  /** The movie scraper. */
  private MovieScrapers                 movieScraper                   = MovieScrapers.TMDB;

  /** The dirty flag. */
  private boolean                       dirty                          = false;

  /** The clear cache on shutdown. */
  private boolean                       clearCacheShutdown             = false;

  /** The scrape best image. */
  private boolean                       scrapeBestImage                = true;

  /** The imdb site. */
  private ImdbSiteDefinition            imdbSite                       = ImdbSiteDefinition.IMDB_COM;

  /** The scraperMetadata configuration. */
  private ScraperMetadataConfig         scraperMetadataConfig          = null;

  /** The window config. */
  private WindowConfig                  windowConfig                   = null;

  /** The trailer tmdb scraper. */
  private boolean                       trailerScraperTmdb             = true;

  /** The trailer hd-trailers.net scraper. */
  private boolean                       trailerScraperHdTrailers       = true;

  /** The trailer ofdb.de scraper. */
  private boolean                       trailerScraperOfdb             = true;

  /** The write actor images. */
  private boolean                       writeActorImages               = false;

  /** The property change listener. */
  private PropertyChangeListener        propertyChangeListener;

  /**
   * Instantiates a new settings.
   */
  private Settings() {
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        setDirty();
      }
    };
    addPropertyChangeListener(propertyChangeListener);

    // default values
    scraperMetadataConfig = new ScraperMetadataConfig();
    scraperMetadataConfig.addPropertyChangeListener(propertyChangeListener);
    windowConfig = new WindowConfig();
    windowConfig.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Gets the single instance of Settings.
   * 
   * @return single instance of Settings
   */
  public static Settings getInstance() {
    if (Settings.instance == null) {
      // try to parse XML
      JAXBContext context;
      try {
        context = JAXBContext.newInstance(Settings.class);
        Unmarshaller um = context.createUnmarshaller();
        try {
          Reader in = new InputStreamReader(new FileInputStream(CONFIG_FILE), "UTF-8");
          Settings.instance = (Settings) um.unmarshal(in);
        }
        catch (FileNotFoundException e) {
          // e.printStackTrace();
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        }
        catch (IOException e) {
          // e.printStackTrace();
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        }
      }
      catch (JAXBException e) {
        LOGGER.error("getInstance", e);
      }

      Settings.instance.clearDirty();

    }
    return Settings.instance;
  }

  /**
   * Sets the dirty.
   */
  private void setDirty() {
    dirty = true;
  }

  /**
   * Clear dirty.
   */
  private void clearDirty() {
    dirty = false;
  }

  /**
   * Adds the movie data sources.
   * 
   * @param path
   *          the path
   */
  public void addMovieDataSources(String path) {
    movieDataSources.add(path);
    // setDirty();
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
  }

  /**
   * Removes the movie data sources.
   * 
   * @param path
   *          the path
   */
  public void removeMovieDataSources(String path) {
    MovieList movieList = MovieList.getInstance();
    movieList.removeDatasource(path);
    movieDataSources.remove(path);
    // setDirty();
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
  }

  /**
   * Gets the movie data source.
   * 
   * @return the movie data source
   */
  public List<String> getMovieDataSource() {
    return movieDataSources;
  }

  /**
   * Adds the movie nfo filename.
   * 
   * @param filename
   *          the filename
   */
  public void addMovieNfoFilename(MovieNfoNaming filename) {
    if (!movieNfoFilenames.contains(filename)) {
      movieNfoFilenames.add(filename);
      // setDirty();
      firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
    }
  }

  /**
   * Removes the movie nfo filename.
   * 
   * @param filename
   *          the filename
   */
  public void removeMovieNfoFilename(MovieNfoNaming filename) {
    if (movieNfoFilenames.contains(filename)) {
      movieNfoFilenames.remove(filename);
      // setDirty();
      firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
    }
  }

  /**
   * Clear movie nfo filenames.
   */
  public void clearMovieNfoFilenames() {
    movieNfoFilenames.clear();
    // setDirty();
    firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
  }

  /**
   * Gets the movie nfo filenames.
   * 
   * @return the movie nfo filenames
   */
  public List<MovieNfoNaming> getMovieNfoFilenames() {
    return this.movieNfoFilenames;
  }

  /**
   * Adds the movie poster filename.
   * 
   * @param filename
   *          the filename
   */
  public void addMoviePosterFilename(MoviePosterNaming filename) {
    if (!moviePosterFilenames.contains(filename)) {
      moviePosterFilenames.add(filename);
      // setDirty();
      firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
    }
  }

  /**
   * Removes the movie poster filename.
   * 
   * @param filename
   *          the filename
   */
  public void removeMoviePosterFilename(MoviePosterNaming filename) {
    if (moviePosterFilenames.contains(filename)) {
      moviePosterFilenames.remove(filename);
      // setDirty();
      firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
    }
  }

  /**
   * Clear movie poster filenames.
   */
  public void clearMoviePosterFilenames() {
    moviePosterFilenames.clear();
    // setDirty();
    firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
  }

  /**
   * Gets the movie poster filenames.
   * 
   * @return the movie poster filenames
   */
  public List<MoviePosterNaming> getMoviePosterFilenames() {
    return this.moviePosterFilenames;
  }

  /**
   * Adds the movie fanart filename.
   * 
   * @param filename
   *          the filename
   */
  public void addMovieFanartFilename(MovieFanartNaming filename) {
    if (!movieFanartFilenames.contains(filename)) {
      movieFanartFilenames.add(filename);
      // setDirty();
      firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
    }
  }

  /**
   * Removes the movie fanart filename.
   * 
   * @param filename
   *          the filename
   */
  public void removeMovieFanartFilename(MovieFanartNaming filename) {
    if (movieFanartFilenames.contains(filename)) {
      movieFanartFilenames.remove(filename);
      // setDirty();
      firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
    }
  }

  /**
   * Clear movie fanart filenames.
   */
  public void clearMovieFanartFilenames() {
    movieFanartFilenames.clear();
    // setDirty();
    firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
  }

  /**
   * Gets the movie fanart filenames.
   * 
   * @return the movie fanart filenames
   */
  public List<MovieFanartNaming> getMovieFanartFilenames() {
    return this.movieFanartFilenames;
  }

  /**
   * Adds a title prefix.
   * 
   * @param prfx
   *          the prefix
   */
  public void addTitlePrefix(String prfx) {
    titlePrefix.add(prfx);
    // setDirty();
    firePropertyChange(TITLE_PREFIX, null, titlePrefix);
  }

  /**
   * Removes the video file type.
   * 
   * @param prfx
   *          the prfx
   */
  public void removeTitlePrefix(String prfx) {
    titlePrefix.remove(prfx);
    // setDirty();
    firePropertyChange(TITLE_PREFIX, null, titlePrefix);
  }

  /**
   * Gets the video file type.
   * 
   * @return the video file type
   */
  public List<String> getTitlePrefix() {
    return titlePrefix;
  }

  /**
   * Adds the video file types.
   * 
   * @param type
   *          the type
   */
  public void addVideoFileTypes(String type) {
    videoFileTypes.add(type);
    // setDirty();
    firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
  }

  /**
   * Removes the video file type.
   * 
   * @param type
   *          the type
   */
  public void removeVideoFileType(String type) {
    videoFileTypes.remove(type);
    // setDirty();
    firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
  }

  /**
   * Gets the video file type.
   * 
   * @return the video file type
   */
  public List<String> getVideoFileType() {
    return videoFileTypes;
  }

  /**
   * Save settings.
   */
  public void saveSettings() {
    // is there anything to save?
    if (!dirty) {
      return;
    }

    // create JAXB context and instantiate marshaller
    JAXBContext context;
    Writer w = null;
    try {
      context = JAXBContext.newInstance(Settings.class);
      Marshaller m = context.createMarshaller();
      m.setProperty("jaxb.encoding", "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      w = new StringWriter();
      m.marshal(this, w);
      StringBuilder sb = new StringBuilder(w.toString());
      w.close();

      // on windows make windows conform linebreaks
      if (SystemUtils.IS_OS_WINDOWS) {
        sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
      }

      w = new FileWriter(CONFIG_FILE);
      String xml = sb.toString();
      IOUtils.write(xml, w);

    }
    catch (JAXBException e) {
      LOGGER.error("saveSettings", e);
    }
    catch (IOException e) {
      LOGGER.error("saveSettings", e);
    }
    finally {
      try {
        w.close();
      }
      catch (Exception e) {
        LOGGER.error("saveSettings", e);
      }
    }

    // set proxy information
    setProxy();

    // clear dirty flag
    clearDirty();
  }

  /**
   * Write default settings.
   */
  private void writeDefaultSettings() {
    // default video file types
    // derived from
    // http://wiki.xbmc.org/index.php?title=Advancedsettings.xml#.3Cvideoextensions.3E
    addVideoFileTypes(".3gp");
    addVideoFileTypes(".asf");
    addVideoFileTypes(".asx");
    addVideoFileTypes(".avc");
    addVideoFileTypes(".avi");
    addVideoFileTypes(".bdmv");
    addVideoFileTypes(".bin");
    addVideoFileTypes(".bivx");
    addVideoFileTypes(".dat");
    addVideoFileTypes(".divx");
    addVideoFileTypes(".dv");
    addVideoFileTypes(".dvr-ms");
    addVideoFileTypes(".fli");
    addVideoFileTypes(".flv");
    addVideoFileTypes(".h264");
    addVideoFileTypes(".img");
    addVideoFileTypes(".iso");
    addVideoFileTypes(".mts");
    addVideoFileTypes(".mt2s");
    addVideoFileTypes(".m2ts");
    addVideoFileTypes(".m2v");
    addVideoFileTypes(".m4v");
    addVideoFileTypes(".mkv");
    addVideoFileTypes(".mov");
    addVideoFileTypes(".mp4");
    addVideoFileTypes(".mpeg");
    addVideoFileTypes(".mpg");
    addVideoFileTypes(".nrg");
    addVideoFileTypes(".nsv");
    addVideoFileTypes(".nuv");
    addVideoFileTypes(".ogm");
    addVideoFileTypes(".pva");
    addVideoFileTypes(".qt");
    addVideoFileTypes(".rm");
    addVideoFileTypes(".rmvb");
    addVideoFileTypes(".strm");
    addVideoFileTypes(".svq3");
    addVideoFileTypes(".ts");
    addVideoFileTypes(".ty");
    addVideoFileTypes(".viv");
    addVideoFileTypes(".vob");
    addVideoFileTypes(".vp3");
    addVideoFileTypes(".wmv");
    addVideoFileTypes(".xvid");

    // default title prefix
    titlePrefix.clear();
    addTitlePrefix("A");
    addTitlePrefix("An");
    addTitlePrefix("The");
    addTitlePrefix("Der");
    addTitlePrefix("Die");
    addTitlePrefix("Das");
    addTitlePrefix("Ein");
    addTitlePrefix("Eine");

    setScraperLanguage(Languages.en);
    setImageTmdbPosterSize(PosterSizes.w342);
    setImageTmdbFanartSize(FanartSizes.original);

    setCertificationCountry(CountryCode.US);
    setMovieConnector(MovieConnectors.XBMC);
    addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
    addMoviePosterFilename(MoviePosterNaming.POSTER_PNG);
    addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
    addMovieFanartFilename(MovieFanartNaming.FANART_PNG);
    setMovieScraper(MovieScrapers.TMDB);
    setImdbScrapeForeignLanguage(false);

    saveSettings();
  }

  /**
   * Gets the proxy host.
   * 
   * @return the proxy host
   */
  @XmlElement(name = PROXY_HOST)
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * Sets the proxy host.
   * 
   * @param newValue
   *          the new proxy host
   */
  public void setProxyHost(String newValue) {
    String oldValue = this.proxyHost;
    this.proxyHost = newValue;
    // setDirty();
    firePropertyChange(PROXY_HOST, oldValue, newValue);
  }

  /**
   * Gets the proxy port.
   * 
   * @return the proxy port
   */
  @XmlElement(name = PROXY_PORT)
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * Sets the proxy port.
   * 
   * @param newValue
   *          the new proxy port
   */
  public void setProxyPort(String newValue) {
    String oldValue = this.proxyPort;
    this.proxyPort = newValue;
    // setDirty();
    firePropertyChange(PROXY_PORT, oldValue, newValue);
  }

  /**
   * Gets the proxy username.
   * 
   * @return the proxy username
   */
  @XmlElement(name = PROXY_USERNAME)
  public String getProxyUsername() {
    return proxyUsername;
  }

  /**
   * Sets the proxy username.
   * 
   * @param newValue
   *          the new proxy username
   */
  public void setProxyUsername(String newValue) {
    String oldValue = this.proxyUsername;
    this.proxyUsername = newValue;
    // setDirty();
    firePropertyChange(PROXY_USERNAME, oldValue, newValue);
  }

  /**
   * Gets the proxy password.
   * 
   * @return the proxy password
   */
  @XmlElement(name = PROXY_PASSWORD)
  public String getProxyPassword() {
    return StringEscapeUtils.unescapeXml(proxyPassword);
  }

  /**
   * Sets the proxy password.
   * 
   * @param newValue
   *          the new proxy password
   */
  public void setProxyPassword(String newValue) {
    newValue = StringEscapeUtils.escapeXml(newValue);
    String oldValue = this.proxyPassword;
    this.proxyPassword = newValue;
    // setDirty();
    firePropertyChange(PROXY_PASSWORD, oldValue, newValue);
  }

  /**
   * Checks if is image scraper tmdb.
   * 
   * @return true, if is image scraper tmdb
   */
  public boolean isImageScraperTmdb() {
    return imageScraperTmdb;
  }

  /**
   * Checks if is image scraper fanart tv.
   * 
   * @return true, if is image scraper fanart tv
   */
  public boolean isImageScraperFanartTv() {
    return imageScraperFanartTv;
  }

  /**
   * Sets the image scraper tmdb.
   * 
   * @param newValue
   *          the new image scraper tmdb
   */
  public void setImageScraperTmdb(boolean newValue) {
    boolean oldValue = this.imageScraperTmdb;
    this.imageScraperTmdb = newValue;
    firePropertyChange(IMAGE_SCRAPER_TMDB, oldValue, newValue);
  }

  /**
   * Sets the image scraper fanart tv.
   * 
   * @param newValue
   *          the new image scraper fanart tv
   */
  public void setImageScraperFanartTv(boolean newValue) {
    boolean oldValue = this.imageScraperFanartTv;
    this.imageScraperFanartTv = newValue;
    firePropertyChange(IMAGE_SCRAPER_FANART_TV, oldValue, newValue);
  }

  /**
   * Sets the proxy.
   */
  public void setProxy() {
    if (useProxy()) {
      System.setProperty("proxyHost", getProxyHost());

      if (StringUtils.isNotEmpty(getProxyPort())) {
        System.setProperty("proxyPort", getProxyPort());
      }

      if (StringUtils.isNotEmpty(getProxyUsername())) {
        System.setProperty("http.proxyUser", getProxyUsername());
        System.setProperty("https.proxyUser", getProxyUsername());
      }
      if (StringUtils.isNotEmpty(getProxyPassword())) {
        System.setProperty("http.proxyPassword", getProxyPassword());
        System.setProperty("https.proxyPassword", getProxyPassword());
      }
      // System.setProperty("java.net.useSystemProxies", "true");
    }
  }

  /**
   * Should we use a proxy.
   * 
   * @return true, if successful
   */
  public boolean useProxy() {
    if (StringUtils.isNotEmpty(getProxyHost()) /*
                                                * &&
                                                * !StringUtils.isEmpty(getProxyPort
                                                * ())
                                                */) {
      return true;
    }
    return false;
  }

  /**
   * Gets the image tmdb poster size.
   * 
   * @return the image tmdb poster size
   */
  @XmlElement(name = IMAGE_TMDB_POSTER)
  public PosterSizes getImageTmdbPosterSize() {
    return imageTmdbPosterSize;
  }

  /**
   * Sets the image tmdb poster size.
   * 
   * @param newValue
   *          the new image tmdb poster size
   */
  public void setImageTmdbPosterSize(PosterSizes newValue) {
    PosterSizes oldValue = this.imageTmdbPosterSize;
    this.imageTmdbPosterSize = newValue;
    // setDirty();
    firePropertyChange(IMAGE_TMDB_POSTER, oldValue, newValue);
  }

  /**
   * Gets the image tmdb fanart size.
   * 
   * @return the image tmdb fanart size
   */
  @XmlElement(name = IMAGE_TMDB_FANART)
  public FanartSizes getImageTmdbFanartSize() {
    return imageTmdbFanartSize;
  }

  /**
   * Sets the image tmdb fanart size.
   * 
   * @param newValue
   *          the new image tmdb fanart size
   */
  public void setImageTmdbFanartSize(FanartSizes newValue) {
    FanartSizes oldValue = this.imageTmdbFanartSize;
    this.imageTmdbFanartSize = newValue;
    // setDirty();
    firePropertyChange(IMAGE_TMDB_FANART, oldValue, newValue);
  }

  /**
   * Checks if is image extra thumbs.
   * 
   * @return true, if is image extra thumbs
   */
  public boolean isImageExtraThumbs() {
    return imageExtraThumbs;
  }

  /**
   * Checks if is image extra thumbs resize.
   * 
   * @return true, if is image extra thumbs resize
   */
  public boolean isImageExtraThumbsResize() {
    return imageExtraThumbsResize;
  }

  /**
   * Gets the image extra thumbs size.
   * 
   * @return the image extra thumbs size
   */
  public int getImageExtraThumbsSize() {
    return imageExtraThumbsSize;
  }

  /**
   * Sets the image extra thumbs resize.
   * 
   * @param newValue
   *          the new image extra thumbs resize
   */
  public void setImageExtraThumbsResize(boolean newValue) {
    boolean oldValue = this.imageExtraThumbsResize;
    this.imageExtraThumbsResize = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_RESIZE, oldValue, newValue);
  }

  /**
   * Sets the image extra thumbs size.
   * 
   * @param newValue
   *          the new image extra thumbs size
   */
  public void setImageExtraThumbsSize(int newValue) {
    int oldValue = this.imageExtraThumbsSize;
    this.imageExtraThumbsSize = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_SIZE, oldValue, newValue);
  }

  /**
   * Gets the image extra thumbs count.
   * 
   * @return the image extra thumbs count
   */
  public int getImageExtraThumbsCount() {
    return imageExtraThumbsCount;
  }

  /**
   * Sets the image extra thumbs count.
   * 
   * @param newValue
   *          the new image extra thumbs count
   */
  public void setImageExtraThumbsCount(int newValue) {
    int oldValue = this.imageExtraThumbsCount;
    this.imageExtraThumbsCount = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_COUNT, oldValue, newValue);
  }

  /**
   * Gets the image extra fanart count.
   * 
   * @return the image extra fanart count
   */
  public int getImageExtraFanartCount() {
    return imageExtraFanartCount;
  }

  /**
   * Sets the image extra fanart count.
   * 
   * @param newValue
   *          the new image extra fanart count
   */
  public void setImageExtraFanartCount(int newValue) {
    int oldValue = this.imageExtraFanartCount;
    this.imageExtraFanartCount = newValue;
    firePropertyChange(IMAGE_EXTRAFANART_COUNT, oldValue, newValue);
  }

  /**
   * Checks if is image extra fanart.
   * 
   * @return true, if is image extra fanart
   */
  public boolean isImageExtraFanart() {
    return imageExtraFanart;
  }

  /**
   * Sets the image extra thumbs.
   * 
   * @param newValue
   *          the new image extra thumbs
   */
  public void setImageExtraThumbs(boolean newValue) {
    boolean oldValue = this.imageExtraThumbs;
    this.imageExtraThumbs = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS, oldValue, newValue);
  }

  /**
   * Sets the image extra fanart.
   * 
   * @param newValue
   *          the new image extra fanart
   */
  public void setImageExtraFanart(boolean newValue) {
    boolean oldValue = this.imageExtraFanart;
    this.imageExtraFanart = newValue;
    firePropertyChange(IMAGE_EXTRAFANART, oldValue, newValue);
  }

  /**
   * Checks if is enable movie set artwork folder.
   * 
   * @return true, if is enable movie set artwork folder
   */
  public boolean isEnableMovieSetArtworkFolder() {
    return enableMovieSetArtworkFolder;
  }

  /**
   * Sets the enable movie set artwork folder.
   * 
   * @param newValue
   *          the new enable movie set artwork folder
   */
  public void setEnableMovieSetArtworkFolder(boolean newValue) {
    boolean oldValue = this.enableMovieSetArtworkFolder;
    this.enableMovieSetArtworkFolder = newValue;
    firePropertyChange(ENABLE_MOVIESET_ARTWORK_FOLDER, oldValue, newValue);
  }

  /**
   * Gets the movie set artwork folder.
   * 
   * @return the movie set artwork folder
   */
  public String getMovieSetArtworkFolder() {
    return movieSetArtworkFolder;
  }

  /**
   * Sets the movie set artwork folder.
   * 
   * @param newValue
   *          the new movie set artwork folder
   */
  public void setMovieSetArtworkFolder(String newValue) {
    String oldValue = this.movieSetArtworkFolder;
    this.movieSetArtworkFolder = newValue;
    firePropertyChange(MOVIESET_ARTWORK_FOLDER, oldValue, newValue);
  }

  /**
   * Gets the scraper tmdb language.
   * 
   * @return the scraper tmdb language
   */
  @XmlElement(name = SCRAPER_TMDB_LANGU)
  public Languages getScraperLanguage() {
    return scraperTmdbLanguage;
  }

  /**
   * Sets the scraper language.
   * 
   * @param newValue
   *          the new scraper language
   */
  public void setScraperLanguage(Languages newValue) {
    Languages oldValue = this.scraperTmdbLanguage;
    this.scraperTmdbLanguage = newValue;
    // setDirty();
    firePropertyChange(SCRAPER_TMDB_LANGU, oldValue, newValue);
  }

  /**
   * Gets the certification country.
   * 
   * @return the certification country
   */
  @XmlElement(name = CERTIFICATION_COUNTRY)
  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  /**
   * Sets the certification country.
   * 
   * @param newValue
   *          the new certification country
   */
  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    // setDirty();
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  /**
   * Gets the movie connector.
   * 
   * @return the movie connector
   */
  @XmlElement(name = MOVIE_CONNECTOR)
  public MovieConnectors getMovieConnector() {
    return movieConnector;
  }

  /**
   * Sets the movie connector.
   * 
   * @param newValue
   *          the new movie connector
   */
  public void setMovieConnector(MovieConnectors newValue) {
    MovieConnectors oldValue = this.movieConnector;
    this.movieConnector = newValue;
    // setDirty();
    firePropertyChange(MOVIE_CONNECTOR, oldValue, newValue);
  }

  /**
   * Gets the movie renamer pathname.
   * 
   * @return the movie renamer pathname
   */
  @XmlElement(name = MOVIE_RENAMER_PATHNAME)
  public String getMovieRenamerPathname() {
    return movieRenamerPathname;
  }

  /**
   * Sets the movie renamer pathname.
   * 
   * @param newValue
   *          the new movie renamer pathname
   */
  public void setMovieRenamerPathname(String newValue) {
    String oldValue = this.movieRenamerPathname;
    this.movieRenamerPathname = newValue;
    // setDirty();
    firePropertyChange(MOVIE_RENAMER_PATHNAME, oldValue, newValue);
  }

  /**
   * Gets the movie renamer filename.
   * 
   * @return the movie renamer filename
   */
  @XmlElement(name = MOVIE_RENAMER_FILENAME)
  public String getMovieRenamerFilename() {
    return movieRenamerFilename;
  }

  /**
   * Sets the movie renamer filename.
   * 
   * @param newValue
   *          the new movie renamer filename
   */
  public void setMovieRenamerFilename(String newValue) {
    String oldValue = this.movieRenamerFilename;
    this.movieRenamerFilename = newValue;
    // setDirty();
    firePropertyChange(MOVIE_RENAMER_FILENAME, oldValue, newValue);
  }

  /**
   * Gets the movie scraper.
   * 
   * @return the movie scraper
   */
  public MovieScrapers getMovieScraper() {
    if (movieScraper == null) {
      return MovieScrapers.TMDB;
    }
    return movieScraper;
  }

  /**
   * Sets the movie scraper.
   * 
   * @param newValue
   *          the new movie scraper
   */
  public void setMovieScraper(MovieScrapers newValue) {
    MovieScrapers oldValue = this.movieScraper;
    this.movieScraper = newValue;
    // setDirty();
    firePropertyChange(MOVIE_SCRAPER, oldValue, newValue);
  }

  /**
   * Checks if is imdb scrape foreign language.
   * 
   * @return true, if is imdb scrape foreign language
   */
  public boolean isImdbScrapeForeignLanguage() {
    return imdbScrapeForeignLanguage;
  }

  /**
   * Sets the imdb scrape foreign language.
   * 
   * @param newValue
   *          the new imdb scrape foreign language
   */
  public void setImdbScrapeForeignLanguage(boolean newValue) {
    boolean oldValue = this.imdbScrapeForeignLanguage;
    this.imdbScrapeForeignLanguage = newValue;
    // setDirty();
    firePropertyChange(IMDB_SCRAPE_FOREIGN_LANGU, oldValue, newValue);
  }

  /**
   * Gets the imdb site.
   * 
   * @return the imdb site
   */
  public ImdbSiteDefinition getImdbSite() {
    return imdbSite;
  }

  /**
   * Sets the imdb site.
   * 
   * @param newValue
   *          the new imdb site
   */
  public void setImdbSite(ImdbSiteDefinition newValue) {
    ImdbSiteDefinition oldValue = this.imdbSite;
    this.imdbSite = newValue;
    // setDirty();
    firePropertyChange(IMDB_SITE, oldValue, newValue);
  }

  /**
   * Checks if is clear cache shutdown.
   * 
   * @return true, if is clear cache shutdown
   */
  public boolean isClearCacheShutdown() {
    return clearCacheShutdown;
  }

  /**
   * Sets the clear cache shutdown.
   * 
   * @param newValue
   *          the new clear cache shutdown
   */
  public void setClearCacheShutdown(boolean newValue) {
    boolean oldValue = this.clearCacheShutdown;
    this.clearCacheShutdown = newValue;
    // setDirty();
    firePropertyChange(CLEAR_CACHE_SHUTDOWN, oldValue, newValue);
  }

  /**
   * Checks if is scrape best image.
   * 
   * @return true, if is scrape best image
   */
  public boolean isScrapeBestImage() {
    return scrapeBestImage;
  }

  /**
   * Sets the scrape best image.
   * 
   * @param newValue
   *          the new scrape best image
   */
  public void setScrapeBestImage(boolean newValue) {
    boolean oldValue = this.scrapeBestImage;
    this.scrapeBestImage = newValue;
    // setDirty();
    firePropertyChange(SCRAPE_BEST_IMAGE, oldValue, newValue);
  }

  /**
   * Gets the scraper metadata config.
   * 
   * @return the scraper metadata config
   */
  public ScraperMetadataConfig getScraperMetadataConfig() {
    return scraperMetadataConfig;
  }

  /**
   * Sets the scraper metadata config.
   * 
   * @param scraperMetadataConfig
   *          the new scraper metadata config
   */
  public void setScraperMetadataConfig(ScraperMetadataConfig scraperMetadataConfig) {
    this.scraperMetadataConfig = scraperMetadataConfig;
    this.scraperMetadataConfig.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Checks if is trailer scraper tmdb.
   * 
   * @return true, if is trailer scraper tmdb
   */
  public boolean isTrailerScraperTmdb() {
    return trailerScraperTmdb;
  }

  /**
   * Checks if is trailer scraper hd trailers.
   * 
   * @return true, if is trailer scraper hd trailers
   */
  public boolean isTrailerScraperHdTrailers() {
    return trailerScraperHdTrailers;
  }

  /**
   * Sets the trailer scraper tmdb.
   * 
   * @param newValue
   *          the new trailer scraper tmdb
   */
  public void setTrailerScraperTmdb(boolean newValue) {
    boolean oldValue = this.trailerScraperTmdb;
    this.trailerScraperTmdb = newValue;
    firePropertyChange(TRAILER_SCRAPER_TMDB, oldValue, newValue);
  }

  /**
   * Sets the trailer scraper hd trailers.
   * 
   * @param newValue
   *          the new trailer scraper hd trailers
   */
  public void setTrailerScraperHdTrailers(boolean newValue) {
    boolean oldValue = this.trailerScraperHdTrailers;
    this.trailerScraperHdTrailers = newValue;
    firePropertyChange(TRAILER_SCRAPER_HD_TRAILERS, oldValue, newValue);
  }

  /**
   * Checks if is trailer scraper ofdb.
   * 
   * @return true, if is trailer scraper ofdb
   */
  public boolean isTrailerScraperOfdb() {
    return trailerScraperOfdb;
  }

  /**
   * Sets the trailer scraper ofdb.
   * 
   * @param newValue
   *          the new trailer scraper ofdb
   */
  public void setTrailerScraperOfdb(boolean newValue) {
    boolean oldValue = this.trailerScraperOfdb;
    this.trailerScraperOfdb = newValue;
    firePropertyChange(TRAILER_SCRAPER_OFDB, oldValue, newValue);
  }

  /**
   * Checks if is write actor images.
   * 
   * @return true, if is write actor images
   */
  public boolean isWriteActorImages() {
    return writeActorImages;
  }

  /**
   * Sets the write actor images.
   * 
   * @param newValue
   *          the new write actor images
   */
  public void setWriteActorImages(boolean newValue) {
    boolean oldValue = this.writeActorImages;
    this.writeActorImages = newValue;
    firePropertyChange(WRITE_ACTOR_IMAGES, oldValue, newValue);
  }

  /**
   * Gets the window config.
   * 
   * @return the window config
   */
  public WindowConfig getWindowConfig() {
    return windowConfig;
  }

  /**
   * Sets the window config.
   * 
   * @param windowConfig
   *          the new window config
   */
  public void setWindowConfig(WindowConfig windowConfig) {
    this.windowConfig = windowConfig;
    this.windowConfig.addPropertyChangeListener(propertyChangeListener);
  }
}