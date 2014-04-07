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

import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

import com.omertron.themoviedbapi.model.CollectionInfo;

/**
 * The class MovieAssignMovieSetTask. A task to assign the movie set to the given movies
 * 
 * @author Manuel Laggner
 */
public class MovieAssignMovieSetTask extends TmmThreadPool {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieAssignMovieSetTask.class);

  private List<Movie>         moviesToScrape;

  public MovieAssignMovieSetTask(List<Movie> moviesToScrape) {
    this.moviesToScrape = moviesToScrape;
  }

  @Override
  protected Void doInBackground() throws Exception {
    initThreadPool(1, "scrape");
    startProgressBar("assigning movie sets", 0);

    for (int i = 0; i < moviesToScrape.size(); i++) {
      Movie movie = moviesToScrape.get(i);
      submitTask(new Worker(movie));
    }
    waitForCompletionOrCancel();
    if (cancel) {
      cancel(false);// swing cancel
    }
    LOGGER.info("Done assigning movies to movie sets");

    return null;
  }

  /**
   * Cancel the task.
   */
  @Override
  public void cancel() {
    cancel = true;
  }

  @Override
  public void done() {
    stopProgressBar();
  }

  private class Worker implements Runnable {
    private MovieList movieList = MovieList.getInstance();
    private Movie     movie;

    public Worker(Movie movie) {
      this.movie = movie;
    }

    @Override
    public void run() {
      if (movie.getMovieSet() != null) {
        return;
      }
      try {
        TmdbMetadataProvider mp = new TmdbMetadataProvider();
        MediaScrapeOptions options = new MediaScrapeOptions();
        options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
        options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());
        options.setScrapeImdbForeignLanguage(Globals.settings.getMovieSettings().isImdbScrapeForeignLanguage());
        options.setScrapeCollectionInfo(true);
        for (Entry<String, Object> entry : movie.getIds().entrySet()) {
          options.setId(entry.getKey(), entry.getValue().toString());
        }

        MediaMetadata md = mp.getMetadata(options);
        int collectionId = md.getIntegerValue(MediaMetadata.TMDBID_SET);
        if (collectionId > 0) {
          String collectionName = md.getStringValue(MediaMetadata.COLLECTION_NAME);
          MovieSet movieSet = movieList.getMovieSet(collectionName, collectionId);
          if (movieSet.getTmdbId() == 0) {
            movieSet.setTmdbId(collectionId);
            // get movieset metadata
            try {
              options = new MediaScrapeOptions();
              options.setTmdbId(collectionId);
              options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
              options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());
              options.setScrapeImdbForeignLanguage(Globals.settings.getMovieSettings().isImdbScrapeForeignLanguage());

              CollectionInfo info = mp.getMovieSetMetadata(options);
              if (info != null) {
                movieSet.setTitle(info.getName());
                movieSet.setPlot(info.getOverview());
                movieSet.setPosterUrl(info.getPosterPath());
                movieSet.setFanartUrl(info.getBackdropPath());
              }
            }
            catch (Exception e) {
            }
          }

          // add movie to movieset
          if (movieSet != null) {
            // first remove from "old" movieset
            movie.setMovieSet(null);

            // add to new movieset
            movie.setMovieSet(movieSet);
            movieSet.insertMovie(movie);
            movieSet.updateMovieSorttitle();
            movie.saveToDb();
          }
        }

      }
      catch (Exception e) {
        LOGGER.error("error getting metadata: " + e.getMessage());
      }
    }
  }

  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }
}