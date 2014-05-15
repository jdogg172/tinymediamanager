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
package org.tinymediamanager.core.movie.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MovieScrapeTask.
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeTask extends TmmThreadPool {
  private final static Logger         LOGGER = LoggerFactory.getLogger(MovieScrapeTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<Movie>                 moviesToScrape;
  private boolean                     doSearch;
  private MovieSearchAndScrapeOptions options;

  /**
   * Instantiates a new movie scrape task.
   * 
   * @param moviesToScrape
   *          the movies to scrape
   * @param doSearch
   *          the do search
   * @param options
   *          the options
   */
  public MovieScrapeTask(List<Movie> moviesToScrape, boolean doSearch, MovieSearchAndScrapeOptions options) {
    super(BUNDLE.getString("movie.scraping"));
    this.moviesToScrape = moviesToScrape;
    this.doSearch = doSearch;
    this.options = options;
  }

  @Override
  protected void doInBackground() {
    initThreadPool(3, "scrape");
    start();

    for (int i = 0; i < moviesToScrape.size(); i++) {
      Movie movie = moviesToScrape.get(i);
      submitTask(new Worker(movie));
    }
    waitForCompletionOrCancel();

    LOGGER.info("Done scraping movies)");
  }

  /**
   * The Class Worker.
   */
  private class Worker implements Runnable {
    private MovieList movieList;
    private Movie     movie;

    public Worker(Movie movie) {
      this.movie = movie;
    }

    @Override
    public void run() {
      try {
        movieList = MovieList.getInstance();
        // set up scrapers
        MovieScraperMetadataConfig scraperMetadataConfig = options.getScraperMetadataConfig();
        IMediaMetadataProvider mediaMetadataProvider = movieList.getMetadataProvider(options.getMetadataScraper());
        List<IMediaArtworkProvider> artworkProviders = movieList.getArtworkProviders(options.getArtworkScrapers());
        List<IMediaTrailerProvider> trailerProviders = movieList.getTrailerProviders(options.getTrailerScrapers());

        // search movie
        MediaSearchResult result1 = null;
        if (doSearch) {
          List<MediaSearchResult> results = movieList.searchMovie(movie.getTitle(), movie, mediaMetadataProvider);
          if (results != null && !results.isEmpty()) {
            result1 = results.get(0);
            // check if there is an other result with 100% score
            if (results.size() > 1) {
              MediaSearchResult result2 = results.get(1);
              // if both results have 100% score - do not take any result
              if (result1.getScore() == 1 && result2.getScore() == 1) {
                LOGGER.info("two 100% results, can't decide whitch to take - ignore result");
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "movie.scrape.toosimilar"));
                return;
              }
            }

            // if there is only one result - we assume it is THE right movie
            // else: get treshold from settings (default 0.75) - to minimize false positives
            if (results.size() > 1) {
              final double scraperTreshold = Globals.settings.getMovieSettings().getScraperThreshold();
              LOGGER.info("using treshold from settings of {}", scraperTreshold);
              if (result1.getScore() < scraperTreshold) {
                LOGGER.info("score is lower than " + scraperTreshold + " (" + result1.getScore() + ") - ignore result");
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "movie.scrape.toolowscore", new String[] { String.format(
                    "%.2f", scraperTreshold) }));
                return;
              }
            }
          }
          else {
            LOGGER.info("no result found for " + movie.getTitle());
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "movie.scrape.nomatchfound"));
          }
        }

        // get metadata, artwork and trailers
        if ((doSearch && result1 != null) || !doSearch) {
          try {
            MediaScrapeOptions options = new MediaScrapeOptions();
            options.setResult(result1);
            options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
            options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());
            options.setScrapeImdbForeignLanguage(Globals.settings.getMovieSettings().isImdbScrapeForeignLanguage());
            options.setScrapeCollectionInfo(scraperMetadataConfig.isCollection());

            // we didn't do a search - pass imdbid and tmdbid from movie
            // object
            if (!doSearch) {
              for (Entry<String, Object> entry : movie.getIds().entrySet()) {
                options.setId(entry.getKey(), entry.getValue().toString());
              }
            }
            else {
              // override scraper with one from search result
              mediaMetadataProvider = movieList.getMetadataProvider(result1.getProviderId());
            }

            // scrape metadata if wanted
            MediaMetadata md = null;

            md = mediaMetadataProvider.getMetadata(options);

            if (scraperMetadataConfig.isMetadata()) {
              movie.setMetadata(md, scraperMetadataConfig);
            }

            // scrape artwork if wanted
            if (scraperMetadataConfig.isArtwork()) {
              movie.setArtwork(getArtwork(movie, md, artworkProviders), scraperMetadataConfig);
            }

            // scrape trailer if wanted
            if (scraperMetadataConfig.isTrailer()) {
              movie.setTrailers(getTrailers(movie, md, trailerProviders));
            }
            movie.writeNFO();
          }
          catch (Exception e) {
            LOGGER.error("movie.setMetadata", e);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.metadatamoviefailed"));
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "MovieScraper", "message.scrape.threadcrashed"));
      }
    }

    /**
     * Gets the artwork.
     * 
     * @param movie
     *          the movie
     * @param metadata
     *          the metadata
     * @param artworkProviders
     *          the artwork providers
     * @return the artwork
     */
    public List<MediaArtwork> getArtwork(Movie movie, MediaMetadata metadata, List<IMediaArtworkProvider> artworkProviders) {
      List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setType(MediaType.MOVIE);
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);
      options.setImdbId(movie.getImdbId());
      options.setTmdbId(movie.getTmdbId());
      options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());
      options.setScrapeImdbForeignLanguage(Globals.settings.getMovieSettings().isImdbScrapeForeignLanguage());

      // scrape providers till one artwork has been found
      for (IMediaArtworkProvider artworkProvider : artworkProviders) {
        try {
          artwork.addAll(artworkProvider.getArtwork(options));
        }
        catch (Exception e) {
          LOGGER.error("getArtwork", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.movieartworkfailed"));
        }
      }

      return artwork;
    }

    /**
     * Gets the trailers.
     * 
     * @param movie
     *          the movie
     * @param metadata
     *          the metadata
     * @param trailerProviders
     *          the trailer providers
     * @return the trailers
     */
    private List<MediaTrailer> getTrailers(Movie movie, MediaMetadata metadata, List<IMediaTrailerProvider> trailerProviders) {
      List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();

      // add local trailers!
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.TRAILER)) {
        LOGGER.debug("adding local trailer " + mf.getFilename());
        MediaTrailer mt = new MediaTrailer();
        mt.setName(mf.getFilename());
        mt.setProvider("downloaded");
        mt.setQuality(mf.getVideoFormat());
        mt.setInNfo(false);
        mt.setUrl(mf.getFile().toURI().toString());
        trailers.add(mt);
      }

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setMetadata(metadata);
      options.setImdbId(movie.getImdbId());
      options.setTmdbId(movie.getTmdbId());
      options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());
      options.setScrapeImdbForeignLanguage(Globals.settings.getMovieSettings().isImdbScrapeForeignLanguage());

      // scrape trailers
      for (IMediaTrailerProvider trailerProvider : trailerProviders) {
        try {
          List<MediaTrailer> foundTrailers = trailerProvider.getTrailers(options);
          trailers.addAll(foundTrailers);
        }
        catch (Exception e) {
          LOGGER.error("getTrailers", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.movietrailerfailed"));
        }
      }

      return trailers;
    }
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
  }
}
