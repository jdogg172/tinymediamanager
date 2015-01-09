/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.fanarttv.FanartTvMetadataProvider;
import org.tinymediamanager.scraper.hdtrailersnet.HDTrailersNet;
import org.tinymediamanager.scraper.imdb.ImdbMetadataProvider;
import org.tinymediamanager.scraper.moviemeternl.MoviemeterMetadataProvider;
import org.tinymediamanager.scraper.ofdb.OfdbMetadataProvider;
import org.tinymediamanager.scraper.rottentomatoes.RottenTomatoesMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.zelluloid.ZelluloidMetadataProvider;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The Class MovieList.
 * 
 * @author Manuel Laggner
 */
public class MovieList extends AbstractModelObject {
  private static final Logger          LOGGER                   = LoggerFactory.getLogger(MovieList.class);
  private static MovieList             instance;

  private ObjectWriter                 movieObjectWriter;
  private PreparedStatement            preparedStatementMovieSave;
  private PreparedStatement            preparedStatementMovieDelete;
  private ObjectWriter                 movieSetObjectWriter;
  private PreparedStatement            preparedStatementMovieSetSave;
  private PreparedStatement            preparedStatementMovieSetDelete;

  private ObservableElementList<Movie> movieList;
  private List<MovieSet>               movieSetList;

  private PropertyChangeListener       tagListener;
  private List<String>                 tagsObservable           = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<String>()));
  private List<String>                 videoCodecsObservable    = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<String>()));
  private List<String>                 audioCodecsObservable    = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<String>()));
  private List<Certification>          certificationsObservable = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<Certification>()));
  private final Comparator<MovieSet>   movieSetComparator       = new MovieSetComparator();

  /**
   * Instantiates a new movie list.
   */
  private MovieList() {
    try {
      preparedStatementMovieSave = MovieModuleManager.getInstance().getConnection().prepareStatement("merge into movie values(?,?)");
      preparedStatementMovieDelete = MovieModuleManager.getInstance().getConnection().prepareStatement("delete movie where id = ?");
      preparedStatementMovieSetSave = MovieModuleManager.getInstance().getConnection().prepareStatement("merge into movieset values(?,?)");
      preparedStatementMovieSetDelete = MovieModuleManager.getInstance().getConnection().prepareStatement("delete movieset where id = ?");
    }
    catch (Exception e) {
      LOGGER.error("failed creating prepared SQL statements: " + e.getMessage());
    }

    // create writer
    movieObjectWriter = MovieModuleManager.getInstance().getObjectMapper().writerWithType(Movie.class);
    movieSetObjectWriter = MovieModuleManager.getInstance().getObjectMapper().writerWithType(MovieSet.class);

    // the tag listener: its used to always have a full list of all tags used in tmm
    tagListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // listen to changes of tags
        if ("tag".equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateTags(movie);
        }
        if (MEDIA_FILES.equals(evt.getPropertyName()) || MEDIA_INFORMATION.equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateMediaInformationLists(movie);
        }
        if (CERTIFICATION.equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateCertifications(movie);
        }
      }
    };
  }

  /**
   * Gets the single instance of MovieList.
   * 
   * @return single instance of MovieList
   */
  public synchronized static MovieList getInstance() {
    if (MovieList.instance == null) {
      MovieList.instance = new MovieList();
    }
    return MovieList.instance;
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    if (!movieList.contains(movie)) {
      int oldValue = movieList.size();
      movieList.add(movie);

      updateTags(movie);
      movie.addPropertyChangeListener(tagListener);
      firePropertyChange("movies", null, movieList);
      firePropertyChange("movieCount", oldValue, movieList.size());
    }
  }

  /**
   * Removes the datasource.
   * 
   * @param path
   *          the path
   */
  public void removeDatasource(String path) {
    if (StringUtils.isEmpty(path)) {
      return;
    }

    List<Movie> moviesToRemove = new ArrayList<Movie>();
    for (int i = movieList.size() - 1; i >= 0; i--) {
      Movie movie = movieList.get(i);
      if (new File(path).equals(new File(movie.getDataSource()))) {
        moviesToRemove.add(movie);
      }
    }

    removeMovies(moviesToRemove);
  }

  /**
   * Gets the unscraped movies.
   * 
   * @return the unscraped movies
   */
  public List<Movie> getUnscrapedMovies() {
    List<Movie> unscrapedMovies = new ArrayList<Movie>();
    for (Movie movie : movieList) {
      if (!movie.isScraped()) {
        unscrapedMovies.add(movie);
      }
    }
    return unscrapedMovies;
  }

  /**
   * Gets the new movies or movies with new files
   * 
   * @return the new movies
   */
  public List<Movie> getNewMovies() {
    List<Movie> newMovies = new ArrayList<Movie>();
    for (Movie movie : movieList) {
      if (movie.isNewlyAdded()) {
        newMovies.add(movie);
      }
    }
    return newMovies;
  }

  /**
   * remove given movies from the database
   * 
   * @param movies
   *          list of movies to remove
   */
  public void removeMovies(List<Movie> movies) {
    if (movies == null || movies.size() == 0) {
      return;
    }
    Set<MovieSet> modifiedMovieSets = new HashSet<MovieSet>();
    int oldValue = movieList.size();

    // remove in inverse order => performance
    for (int i = movies.size() - 1; i >= 0; i--) {
      Movie movie = movies.get(i);
      movieList.remove(movie);
      if (movie.getMovieSet() != null) {
        MovieSet movieSet = movie.getMovieSet();

        movieSet.removeMovie(movie);
        modifiedMovieSets.add(movieSet);
        movie.setMovieSet(null);
      }
      try {
        removeMovieFromDb(movie);
      }
      catch (Exception e) {
        LOGGER.error("problem removing movie from DB: " + e.getMessage());
      }
    }

    // and now check if any of the modified moviesets are worth for deleting
    for (MovieSet movieSet : modifiedMovieSets) {
      if (movieSet.getMovies().isEmpty()) {
        removeMovieSet(movieSet);
      }
    }

    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * delete the given movies from the database and physically
   * 
   * @param movies
   *          list of movies to delete
   */
  public void deleteMovies(List<Movie> movies) {
    if (movies == null || movies.size() == 0) {
      return;
    }
    Set<MovieSet> modifiedMovieSets = new HashSet<MovieSet>();
    int oldValue = movieList.size();
    // remove in inverse order => performance
    for (int i = movies.size() - 1; i >= 0; i--) {
      Movie movie = movies.get(i);
      movie.deleteFilesSafely();
      movieList.remove(movie);
      if (movie.getMovieSet() != null) {
        MovieSet movieSet = movie.getMovieSet();
        movieSet.removeMovie(movie);
        modifiedMovieSets.add(movieSet);
        movie.setMovieSet(null);
      }
      try {
        removeMovieFromDb(movie);
      }
      catch (Exception e) {
        LOGGER.error("problem removing movie from DB: " + e.getMessage());
      }
    }

    // and now check if any of the modified moviesets are worth for deleting
    for (MovieSet movieSet : modifiedMovieSets) {
      removeMovieSet(movieSet);
    }

    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public ObservableElementList<Movie> getMovies() {
    if (movieList == null) {
      movieList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>()), GlazedLists.beanConnector(Movie.class));
    }
    return movieList;
  }

  /**
   * Load movies from database.
   */
  public void loadMoviesFromDatabase(Connection connection) {
    // 1. load all movies
    ObjectReader movieObjectReader = MovieModuleManager.getInstance().getObjectMapper().reader(Movie.class);
    movieList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>()), GlazedLists.beanConnector(Movie.class));
    try {
      Statement select = MovieModuleManager.getInstance().getConnection().createStatement();
      ResultSet result = select.executeQuery("SELECT * FROM movie");
      while (result.next()) { // process results one row at a time
        UUID uuid = UUID.fromString(result.getString(1));
        String json = result.getString(2);
        try {
          Movie movie = movieObjectReader.readValue(json);
          movie.setDbId(uuid);
          movieList.add(movie);
        }
        catch (Exception e) {
          LOGGER.warn("problem decoding movie json string: ", e);
        }
      }
      LOGGER.info("found " + movieList.size() + " movies in database");
    }
    catch (Exception e) {
      LOGGER.error("failed retrieving movies: " + e.getMessage());
    }

    // 2. load all movie sets
    ObjectReader movieSetObjectReader = MovieModuleManager.getInstance().getObjectMapper().reader(MovieSet.class);
    movieSetList = ObservableCollections.observableList(Collections.synchronizedList(new ArrayList<MovieSet>()));
    try {
      Statement select = MovieModuleManager.getInstance().getConnection().createStatement();
      ResultSet result = select.executeQuery("SELECT * FROM movieset");
      while (result.next()) { // process results one row at a time
        UUID uuid = UUID.fromString(result.getString(1));
        String json = result.getString(2);

        try {
          MovieSet movieSet = movieSetObjectReader.readValue(json);
          movieSet.setDbId(uuid);
          movieSetList.add(movieSet);
        }
        catch (Exception e) {
          LOGGER.warn("problem decoding movie set json string: ", e);
        }
      }
      LOGGER.info("found " + movieSetList.size() + " movie sets in database");
    }
    catch (Exception e) {
      LOGGER.error("failed retrieving movie sets: " + e.getMessage());
    }

    // 3. initialize movies/movie sets (e.g. link with each others)
    for (Movie movie : movieList) {
      movie.initializeAfterLoading();
      updateTags(movie);
      updateMediaInformationLists(movie);
      updateCertifications(movie);
      movie.addPropertyChangeListener(tagListener);
    }

    for (MovieSet movieSet : movieSetList) {
      movieSet.initializeAfterLoading();
    }
  }

  public MovieSet lookupMovieSet(UUID uuid) {
    for (MovieSet movieSet : movieSetList) {
      if (movieSet.getDbId().equals(uuid)) {
        return movieSet;
      }
    }
    return null;
  }

  public Movie lookupMovie(UUID uuid) {
    for (Movie movie : movieList) {
      if (movie.getDbId().equals(uuid)) {
        return movie;
      }
    }
    return null;
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  public synchronized Movie getMovieByPath(File path) {

    for (Movie movie : movieList) {
      if (new File(movie.getPath()).compareTo(path) == 0) {
        LOGGER.debug("Ok, found already existing movie '" + movie.getTitle() + "' in DB (path: " + path + ")");
        return movie;
      }
    }

    return null;
  }

  /**
   * Gets a list of movies by same path.
   * 
   * @param path
   *          the path
   * @return the movie list
   */
  public synchronized List<Movie> getMoviesByPath(File path) {
    ArrayList<Movie> movies = new ArrayList<Movie>();
    for (Movie movie : movieList) {
      if (new File(movie.getPath()).compareTo(path) == 0) {
        movies.add(movie);
      }
    }
    return movies;
  }

  /**
   * Search for a movie with the default settings.
   * 
   * @param searchTerm
   *          the search term
   * @param movie
   *          the movie
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, Movie movie, IMediaMetadataProvider metadataProvider) {
    return searchMovie(searchTerm, movie, metadataProvider, MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
  }

  /**
   * Search movie with the chosen language.
   * 
   * @param searchTerm
   *          the search term
   * @param movie
   *          the movie
   * @param metadataProvider
   *          the metadata provider
   * @param language
   *          the language to search with
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, Movie movie, IMediaMetadataProvider metadataProvider, MediaLanguages langu) {
    List<MediaSearchResult> sr = null;

    try {
      IMediaMetadataProvider provider = metadataProvider;
      // get a new metadataprovider if nothing is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      boolean idFound = false;
      // set what we have, so the provider could chose from all :)
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);
      options.set(SearchParam.LANGUAGE, langu.name());
      options.set(SearchParam.COUNTRY, MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry().getAlpha2());
      options.set(SearchParam.COLLECTION_INFO, Boolean.toString(Globals.settings.getMovieScraperMetadataConfig().isCollection()));
      options.set(SearchParam.IMDB_FOREIGN_LANGUAGE, Boolean.toString(MovieModuleManager.MOVIE_SETTINGS.isImdbScrapeForeignLanguage()));
      if (movie != null) {
        if (Utils.isValidImdbId(movie.getImdbId())) {
          options.set(SearchParam.IMDBID, movie.getImdbId());
          idFound = true;
        }
        if (movie.getTmdbId() != 0) {
          options.set(SearchParam.TMDBID, String.valueOf(movie.getTmdbId()));
          idFound = true;
        }
        options.set(SearchParam.TITLE, movie.getTitle());
        if (!movie.getYear().isEmpty()) {
          options.set(SearchParam.YEAR, movie.getYear());
        }
      }
      if (!searchTerm.isEmpty()) {
        if (idFound) {
          // id found, so search for it
          // except when searchTerm differs from movie title (we entered something to search for)
          if (!searchTerm.equals(movie.getTitle())) {
            options.set(SearchParam.QUERY, searchTerm);
          }
        }
        else {
          options.set(SearchParam.QUERY, searchTerm);
        }
      }

      sr = provider.search(options);
      // if result is empty, try all scrapers
      if (sr.isEmpty() && MovieModuleManager.MOVIE_SETTINGS.isScraperFallback()) {
        LOGGER.debug("no result yet - trying alternate scrapers");

        for (MovieScrapers ms : MovieScrapers.values()) {
          IMediaMetadataProvider provider2 = getMetadataProvider(ms);
          if (provider.getProviderInfo().equals(provider2.getProviderInfo())) {
            continue;
          }
          sr = provider2.search(options);
          if (!sr.isEmpty()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("searchMovie", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.movie.searcherror", new String[] { ":",
          e.getLocalizedMessage() }));
    }

    return sr;
  }

  // /**
  // * Search movie.
  // *
  // * @param searchTerm
  // * the search term
  // * @param ImdbId
  // * the imdb id
  // * @param metadataProvider
  // * the metadata provider
  // * @return the list
  // */
  // @Deprecated
  // public List<MediaSearchResult> searchMovie(String searchTerm, String year, String ImdbId, IMediaMetadataProvider metadataProvider) {
  // List<MediaSearchResult> sr = null;
  // if (ImdbId != null && !ImdbId.isEmpty()) {
  // sr = searchMovieByImdbId(ImdbId, metadataProvider);
  // }
  // if (sr == null || sr.size() == 0) {
  // sr = searchMovie(searchTerm, year, metadataProvider);
  // }
  //
  // return sr;
  // }

  // /**
  // * Search movie.
  // *
  // * @param searchTerm
  // * the search term
  // * @param metadataProvider
  // * the metadata provider
  // * @return the list
  // */
  // @Deprecated
  // private List<MediaSearchResult> searchMovie(String searchTerm, String year, IMediaMetadataProvider metadataProvider) {
  // // format searchstring
  // // searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);
  //
  // List<MediaSearchResult> searchResult = null;
  // try {
  // IMediaMetadataProvider provider = metadataProvider;
  // // get a new metadataprovider if nothing is set
  // if (provider == null) {
  // provider = getMetadataProvider();
  // }
  // MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, searchTerm);
  // options.set(MediaSearchOptions.SearchParam.YEAR, year);
  // searchResult = provider.search(options);
  // }
  // catch (Exception e) {
  // LOGGER.error("searchMovie", e);
  // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "", "message.movie.searcherror", new String[] { ":",
  // e.getLocalizedMessage() }));
  // }
  //
  // return searchResult;
  // }

  // /**
  // * Search movie.
  // *
  // * @param imdbId
  // * the imdb id
  // * @param metadataProvider
  // * the metadata provider
  // * @return the list
  // */
  // @Deprecated
  // private List<MediaSearchResult> searchMovieByImdbId(String imdbId, IMediaMetadataProvider metadataProvider) {
  //
  // List<MediaSearchResult> searchResult = null;
  // MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);
  // options.setMediaType(MediaType.MOVIE);
  // options.set(SearchParam.IMDBID, imdbId);
  //
  // try {
  // IMediaMetadataProvider provider = metadataProvider;
  // // get a new metadataProvider if no one is set
  // if (provider == null) {
  // provider = getMetadataProvider();
  // }
  // searchResult = provider.search(options);
  // }
  // catch (Exception e) {
  // LOGGER.warn("failed to search movie with imdbid", e);
  // searchResult = new ArrayList<MediaSearchResult>();
  // }
  //
  // return searchResult;
  // }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider() {
    MovieScrapers scraper = MovieModuleManager.MOVIE_SETTINGS.getMovieScraper();
    return getMetadataProvider(scraper);
  }

  /**
   * Gets the metadata provider.
   * 
   * @param scraper
   *          the scraper
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider(MovieScrapers scraper) {
    IMediaMetadataProvider metadataProvider = null;
    switch (scraper) {
      case OFDB:
        LOGGER.debug("get instance of OfdbMetadataProvider");
        metadataProvider = new OfdbMetadataProvider();
        break;

      case ZELLULOID:
        LOGGER.debug("get instance of ZelluloidMetadataProvider");
        metadataProvider = new ZelluloidMetadataProvider();
        break;

      case MOVIEMETER:
        LOGGER.debug("get instance of MoviemeterMetadataProvider");
        try {
          metadataProvider = new MoviemeterMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of MoviemeterMetadataProvider", e);
        }
        break;

      case IMDB:
        LOGGER.debug("get instance of ImdbMetadataProvider");
        metadataProvider = new ImdbMetadataProvider();
        break;

      case ROTTENTOMATOES:
        LOGGER.debug("get instance of RottenTomatoesMetadataProvider");
        try {
          metadataProvider = new RottenTomatoesMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of RottenTomatoesMetadataProvider", e);
        }
        break;

      case TMDB:
      default:
        LOGGER.debug("get instance of TmdbMetadataProvider");
        try {
          metadataProvider = new TmdbMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
        }
    }

    //
    // try {
    // metadataProvider = new XbmcMetadataProvider(new
    // XbmcScraperParser().parseScraper(new
    // File("xbmc_scraper/metadata.imdb.com/imdb.xml")));
    // metadataProvider = new XbmcMetadataProvider(new
    // XbmcScraperParser().parseScraper(new
    // File("xbmc_scraper/metadata.imdb.de/imdb_de.xml")));
    // } catch (Exception e) {
    // LOGGER.error("tried to get xmbc scraper", e);
    // }

    // }

    return metadataProvider;
  }

  /**
   * Gets the metadata provider from a searchresult's providerId.
   * 
   * @param providerId
   *          the scraper
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider(String providerId) {
    // FIXME: rework scrapers/providerInfo to contain Movie(Tv)Scrapers enums
    if (providerId == null || providerId.isEmpty()) {
      // default
      return getMetadataProvider(MovieScrapers.TMDB);
    }
    if (providerId.equals(Constants.TMDBID)) {
      return getMetadataProvider(MovieScrapers.TMDB);
    }
    else if (providerId.equals(Constants.IMDBID)) {
      return getMetadataProvider(MovieScrapers.IMDB);
    }
    else if (providerId.equals(Constants.MOVIEMETERID)) {
      return getMetadataProvider(MovieScrapers.MOVIEMETER);
    }
    else if (providerId.equals(Constants.OFDBID)) {
      return getMetadataProvider(MovieScrapers.OFDB);
    }
    else if (providerId.equals(Constants.ZELLULOIDID)) {
      return getMetadataProvider(MovieScrapers.ZELLULOID);
    }
    else if (providerId.equals(Constants.ROTTENTOMATOESID)) {
      return getMetadataProvider(MovieScrapers.ROTTENTOMATOES);
    }
    else {
      // default
      return getMetadataProvider(MovieScrapers.TMDB);
    }
  }

  /**
   * Gets the artwork provider.
   * 
   * @return the artwork provider
   */
  public List<IMediaArtworkProvider> getArtworkProviders() {
    List<MovieArtworkScrapers> scrapers = new ArrayList<MovieArtworkScrapers>();
    if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperTmdb()) {
      scrapers.add(MovieArtworkScrapers.TMDB);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperFanartTv()) {
      scrapers.add(MovieArtworkScrapers.FANART_TV);
    }

    return getArtworkProviders(scrapers);
  }

  /**
   * Gets the artwork providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the artwork providers
   */
  public List<IMediaArtworkProvider> getArtworkProviders(List<MovieArtworkScrapers> scrapers) {
    List<IMediaArtworkProvider> artworkProviders = new ArrayList<IMediaArtworkProvider>();

    IMediaArtworkProvider artworkProvider = null;

    // tmdb
    if (scrapers.contains(MovieArtworkScrapers.TMDB)) {
      try {
        if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperTmdb()) {
          LOGGER.debug("get instance of TmdbMetadataProvider");
          artworkProvider = new TmdbMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
      }
    }

    // fanart.tv
    if (scrapers.contains(MovieArtworkScrapers.FANART_TV)) {
      try {
        if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperFanartTv()) {
          LOGGER.debug("get instance of FanartTvMetadataProvider");
          artworkProvider = new FanartTvMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of FanartTvMetadataProvider", e);
      }
    }

    return artworkProviders;
  }

  /**
   * Gets the trailer providers.
   * 
   * @return the trailer providers
   */
  public List<IMediaTrailerProvider> getTrailerProviders() {
    List<MovieTrailerScrapers> scrapers = new ArrayList<MovieTrailerScrapers>();

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperTmdb()) {
      scrapers.add(MovieTrailerScrapers.TMDB);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperHdTrailers()) {
      scrapers.add(MovieTrailerScrapers.HDTRAILERS);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperOfdb()) {
      scrapers.add(MovieTrailerScrapers.OFDB);
    }

    return getTrailerProviders(scrapers);
  }

  /**
   * Gets the trailer providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the trailer providers
   */
  public List<IMediaTrailerProvider> getTrailerProviders(List<MovieTrailerScrapers> scrapers) {
    List<IMediaTrailerProvider> trailerProviders = new ArrayList<IMediaTrailerProvider>();

    // tmdb
    if (scrapers.contains(MovieTrailerScrapers.TMDB)) {
      try {
        IMediaTrailerProvider trailerProvider = new TmdbMetadataProvider();
        trailerProviders.add(trailerProvider);
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
      }
    }

    // hd-trailer.net
    if (scrapers.contains(MovieTrailerScrapers.HDTRAILERS)) {
      IMediaTrailerProvider trailerProvider = new HDTrailersNet();
      trailerProviders.add(trailerProvider);
    }

    // ofdb.de
    if (scrapers.contains(MovieTrailerScrapers.OFDB)) {
      IMediaTrailerProvider trailerProvider = new OfdbMetadataProvider();
      trailerProviders.add(trailerProvider);
    }

    return trailerProviders;
  }

  /**
   * Gets the movie count.
   * 
   * @return the movie count
   */
  public int getMovieCount() {
    int size = movieList.size();
    return size;
  }

  /**
   * Gets the movie set count.
   * 
   * @return the movie set count
   */
  public int getMovieSetCount() {
    int size = movieSetList.size();
    return size;
  }

  /**
   * Gets the tags in movies.
   * 
   * @return the tags in movies
   */
  public List<String> getTagsInMovies() {
    return tagsObservable;
  }

  /**
   * Update tags used in movies.
   * 
   * @param movie
   *          the movie
   */
  private void updateTags(Movie movie) {
    for (String tagInMovie : movie.getTags()) {
      boolean tagFound = false;
      for (String tag : tagsObservable) {
        if (tagInMovie.equals(tag)) {
          tagFound = true;
          break;
        }
      }
      if (!tagFound) {
        addTag(tagInMovie);
      }
    }
  }

  /**
   * Update media information used in movies.
   * 
   * @param movie
   *          the movie
   */
  private void updateMediaInformationLists(Movie movie) {
    // video codec
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      String codec = mf.getVideoCodec();
      boolean codecFound = false;

      for (String mfCodec : videoCodecsObservable) {
        if (mfCodec.equals(codec)) {
          codecFound = true;
          break;
        }
      }

      if (!codecFound) {
        addVideoCodec(codec);
      }
    }

    // audio codec
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      for (MediaFileAudioStream audio : mf.getAudioStreams()) {
        String codec = audio.getCodec();
        boolean codecFound = false;
        for (String mfCodec : audioCodecsObservable) {
          if (mfCodec.equals(codec)) {
            codecFound = true;
            break;
          }
        }

        if (!codecFound) {
          addAudioCodec(codec);
        }
      }
    }
  }

  private void updateCertifications(Movie movie) {
    if (!certificationsObservable.contains(movie.getCertification())) {
      addCertification(movie.getCertification());
    }
  }

  public List<String> getVideoCodecsInMovies() {
    return videoCodecsObservable;
  }

  public List<String> getAudioCodecsInMovies() {
    return audioCodecsObservable;
  }

  public List<Certification> getCertificationsInMovies() {
    return certificationsObservable;
  }

  /**
   * Adds the tag.
   * 
   * @param newTag
   *          the new tag
   */
  private void addTag(String newTag) {
    if (StringUtils.isBlank(newTag)) {
      return;
    }

    for (String tag : tagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tagsObservable.add(newTag);
    firePropertyChange("tag", null, tagsObservable);
  }

  private void addVideoCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    for (String codec : videoCodecsObservable) {
      if (codec.equals(newCodec)) {
        return;
      }
    }

    videoCodecsObservable.add(newCodec);
    firePropertyChange("videoCodec", null, videoCodecsObservable);
  }

  private void addAudioCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    for (String codec : audioCodecsObservable) {
      if (codec.equals(newCodec)) {
        return;
      }
    }

    audioCodecsObservable.add(newCodec);
    firePropertyChange("audioCodec", null, audioCodecsObservable);
  }

  private void addCertification(Certification newCert) {
    if (newCert == null) {
      return;
    }

    if (!certificationsObservable.contains(newCert)) {
      certificationsObservable.add(newCert);
      firePropertyChange("certification", null, certificationsObservable);
    }
  }

  /**
   * Search duplicates.
   */
  public void searchDuplicates() {
    Map<String, Movie> imdbDuplicates = new HashMap<String, Movie>();
    Map<Integer, Movie> tmdbDuplicates = new HashMap<Integer, Movie>();

    for (Movie movie : movieList) {
      movie.clearDuplicate();

      // imdb duplicate search only works with given imdbid
      if (StringUtils.isNotEmpty(movie.getImdbId())) {
        // is there a movie with this imdbid sotred?
        if (imdbDuplicates.containsKey(movie.getImdbId())) {
          // yes - set duplicate flag on both movies
          movie.setDuplicate();
          Movie movie2 = imdbDuplicates.get(movie.getImdbId());
          movie2.setDuplicate();
        }
        else {
          // no, store movie
          imdbDuplicates.put(movie.getImdbId(), movie);
        }
      }

      // tmdb duplicate search only works with with given tmdb id
      if (movie.getTmdbId() > 0) {
        // is there a movie with this tmdbid sotred?
        if (tmdbDuplicates.containsKey(movie.getTmdbId())) {
          // yes - set duplicate flag on both movies
          movie.setDuplicate();
          Movie movie2 = tmdbDuplicates.get(movie.getTmdbId());
          movie2.setDuplicate();
        }
        else {
          // no, store movie
          tmdbDuplicates.put(movie.getTmdbId(), movie);
        }
      }
    }
  }

  /**
   * Gets the movie set list.
   * 
   * @return the movieSetList
   */
  public List<MovieSet> getMovieSetList() {
    if (movieSetList == null) {
      movieSetList = ObservableCollections.observableList(Collections.synchronizedList(new ArrayList<MovieSet>()));
    }
    return movieSetList;
  }

  /**
   * get the movie set list in a sorted order
   * 
   * @return the movie set list (sorted)
   */
  public List<MovieSet> getSortedMovieSetList() {
    List<MovieSet> sortedMovieSets = new ArrayList<MovieSet>(getMovieSetList());
    Collections.sort(sortedMovieSets, movieSetComparator);
    return sortedMovieSets;
  }

  /**
   * Sets the movie set list.
   * 
   * @param movieSetList
   *          the movieSetList to set
   */
  public void setMovieSetList(ObservableElementList<MovieSet> movieSetList) {
    this.movieSetList = movieSetList;
  }

  /**
   * Adds the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void addMovieSet(MovieSet movieSet) {
    int oldValue = movieSetList.size();
    this.movieSetList.add(movieSet);
    firePropertyChange("addedMovieSet", null, movieSet);
    firePropertyChange("movieSetCount", oldValue, movieSetList.size());
  }

  /**
   * Removes the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void removeMovieSet(MovieSet movieSet) {
    int oldValue = movieSetList.size();
    movieSet.removeAllMovies();

    movieSetList.remove(movieSet);
    try {
      removeMovieSetFromDb(movieSet);
    }
    catch (Exception e) {
      LOGGER.error("problem removing movie set from DB: " + e.getMessage());
    }

    firePropertyChange("removedMovieSet", null, movieSet);
    firePropertyChange("movieSetCount", oldValue, movieSetList.size());
  }

  private MovieSet findMovieSet(String title, int tmdbId) {
    // first search by tmdbId
    if (tmdbId > 0) {
      for (MovieSet movieSet : movieSetList) {
        if (movieSet.getTmdbId() == tmdbId) {
          return movieSet;
        }
      }
    }

    // search for the movieset by name
    for (MovieSet movieSet : movieSetList) {
      if (movieSet.getTitle().equals(title)) {
        return movieSet;
      }
    }

    return null;
  }

  public synchronized MovieSet getMovieSet(String title, int tmdbId) {
    MovieSet movieSet = findMovieSet(title, tmdbId);

    if (movieSet == null && StringUtils.isNotBlank(title)) {
      movieSet = new MovieSet(title);
      movieSet.saveToDb();
      addMovieSet(movieSet);
    }

    return movieSet;
  }

  /**
   * Sort movies in movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void sortMoviesInMovieSet(MovieSet movieSet) {
    if (movieSet.getMovies().size() > 1) {
      movieSet.sortMovies();
    }
    firePropertyChange("sortedMovieSets", null, movieSetList);
  }

  /**
   * invalidate the title sortable upon changes to the sortable prefixes
   */
  public void invalidateTitleSortable() {
    for (Movie movie : new ArrayList<Movie>(movieList)) {
      movie.clearTitleSortable();
    }
  }

  public void persistMovie(Movie movie) throws Exception {
    String json = movieObjectWriter.writeValueAsString(movie);

    synchronized (preparedStatementMovieSave) {
      preparedStatementMovieSave.setString(1, movie.getDbId().toString());
      preparedStatementMovieSave.setString(2, json);
      preparedStatementMovieSave.executeUpdate();
    }
  }

  public void removeMovieFromDb(Movie movie) throws Exception {
    synchronized (preparedStatementMovieDelete) {
      preparedStatementMovieDelete.setString(1, movie.getDbId().toString());
      preparedStatementMovieDelete.executeUpdate();
    }
  }

  public void persistMovieSet(MovieSet movieSet) throws Exception {
    String json = movieSetObjectWriter.writeValueAsString(movieSet);

    synchronized (preparedStatementMovieSetSave) {
      preparedStatementMovieSetSave.setString(1, movieSet.getDbId().toString());
      preparedStatementMovieSetSave.setString(2, json);
      preparedStatementMovieSetSave.executeUpdate();
    }
  }

  public void removeMovieSetFromDb(MovieSet movieSet) throws Exception {
    synchronized (preparedStatementMovieSetDelete) {
      preparedStatementMovieSetDelete.setString(1, movieSet.getDbId().toString());
      preparedStatementMovieSetDelete.executeUpdate();
    }
  }

  /*
   * helper classes
   */
  private class MovieSetComparator implements Comparator<MovieSet> {
    @Override
    public int compare(MovieSet o1, MovieSet o2) {
      if (o1 == null || o2 == null || o1.getTitleSortable() == null || o2.getTitleSortable() == null) {
        return 0;
      }
      return o1.getTitleSortable().compareToIgnoreCase(o2.getTitleSortable());
    }
  }
}
