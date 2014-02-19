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
package org.tinymediamanager.ui.tvshows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.UTF8Control;

/**
 * 
 * @author Manuel Laggner
 */
public class TvShowChooserModel extends AbstractModelObject {
  private static final ResourceBundle    BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger            LOGGER           = LoggerFactory.getLogger(TvShowChooserModel.class);
  public static final TvShowChooserModel emptyResult      = new TvShowChooserModel();

  private ITvShowMetadataProvider        metadataProvider = null;
  private List<IMediaArtworkProvider>    artworkProviders = null;
  private MediaSearchResult              result           = null;
  private MediaMetadata                  metadata         = null;
  private String                         name             = "";
  private String                         overview         = "";
  private String                         year             = "";
  private String                         combinedName     = "";
  private String                         posterUrl        = "";
  private String                         tagline          = "";
  private boolean                        scraped          = false;

  // private List<IMediaTrailerProvider> trailerProviders = null;

  /**
   * Instantiates a new tv show chooser model.
   * 
   * @param metadataProvider
   *          the metadata provider
   * @param artworkProviders
   *          the artwork providers
   * @param trailerProviders
   *          the trailer providers
   * @param result
   *          the result
   */
  public TvShowChooserModel(ITvShowMetadataProvider metadataProvider, List<IMediaArtworkProvider> artworkProviders,
      List<IMediaTrailerProvider> trailerProviders, MediaSearchResult result) {
    this.metadataProvider = metadataProvider;
    this.artworkProviders = artworkProviders;
    // this.trailerProviders = trailerProviders;
    this.result = result;

    // name
    setName(result.getTitle());
    // year
    setYear(result.getYear());
    // combined name (name (year))
    setCombinedName();
  }

  /**
   * create the empty search result.
   */
  private TvShowChooserModel() {
    setName(BUNDLE.getString("chooser.nothingfound")); //$NON-NLS-1$
    combinedName = name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public void setName(String name) {
    String oldValue = this.name;
    this.name = name;
    firePropertyChange("name", oldValue, name);
  }

  /**
   * Sets the overview.
   * 
   * @param overview
   *          the new overview
   */
  public void setOverview(String overview) {
    String oldValue = this.overview;
    this.overview = overview;
    firePropertyChange("overview", oldValue, overview);
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the overview.
   * 
   * @return the overview
   */
  public String getOverview() {
    return overview;
  }

  /**
   * Gets the poster url.
   * 
   * @return the poster url
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear() {
    return year;
  }

  /**
   * Sets the year.
   * 
   * @param year
   *          the new year
   */
  public void setYear(String year) {
    String oldValue = this.year;
    this.year = year;
    firePropertyChange("year", oldValue, year);
  }

  /**
   * Sets the combined name.
   */
  public void setCombinedName() {
    String oldValue = this.combinedName;

    if (StringUtils.isNotBlank(getYear())) {
      this.combinedName = getName() + " (" + getYear() + ")";
    }
    else {
      this.combinedName = getName();
    }
    firePropertyChange("combinedName", oldValue, this.combinedName);
  }

  /**
   * Gets the combined name.
   * 
   * @return the combined name
   */
  public String getCombinedName() {
    return combinedName;
  }

  /**
   * Scrape meta data.
   */
  public void scrapeMetaData() {
    try {
      // poster for preview
      setPosterUrl(result.getPosterUrl());

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setResult(result);
      options.setLanguage(Globals.settings.getTvShowSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getTvShowSettings().getCertificationCountry());
      options.setType(MediaType.TV_SHOW);
      metadata = metadataProvider.getTvShowMetadata(options);
      setOverview(metadata.getStringValue(MediaMetadata.PLOT));
      setTagline(metadata.getStringValue(MediaMetadata.TAGLINE));

      if (StringUtils.isBlank(posterUrl) && StringUtils.isNotBlank(metadata.getStringValue(MediaMetadata.POSTER_URL))) {
        setPosterUrl(metadata.getStringValue(MediaMetadata.POSTER_URL));
      }

      scraped = true;

    }
    catch (IOException e) {
      LOGGER.error("scrapeMedia", e);
    }
    catch (Exception e) {
      LOGGER.error("scrapeMedia", e);
    }
  }

  /**
   * Gets the artwork.
   * 
   * @return the artwork
   */
  public List<MediaArtwork> getArtwork() {
    List<MediaArtwork> artwork = null;

    MediaScrapeOptions options = new MediaScrapeOptions();
    options.setType(MediaType.TV_SHOW);
    options.setArtworkType(MediaArtworkType.ALL);
    options.setMetadata(metadata);
    options.setId(MediaMetadata.IMDBID, String.valueOf(metadata.getId(MediaMetadata.IMDBID)));
    options.setLanguage(Globals.settings.getTvShowSettings().getScraperLanguage());
    options.setCountry(Globals.settings.getTvShowSettings().getCertificationCountry());

    // scrape providers till one artwork has been found
    for (IMediaArtworkProvider artworkProvider : artworkProviders) {
      try {
        artwork = artworkProvider.getArtwork(options);
      }
      catch (Exception e) {
        LOGGER.warn("could not get artwork from " + artworkProvider.getProviderInfo().getName() + ": " + e.getMessage());
        artwork = new ArrayList<MediaArtwork>();
      }
      // check if at least one artwork has been found
      if (artwork.size() > 0) {
        break;
      }
    }

    // initialize if null
    if (artwork == null) {
      artwork = new ArrayList<MediaArtwork>();
    }

    return artwork;
  }

  // /**
  // * Gets the trailers.
  // *
  // * @return the trailers
  // */
  // public List<MediaTrailer> getTrailers() {
  // List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();
  //
  // MediaScrapeOptions options = new MediaScrapeOptions();
  // options.setMetadata(metadata);
  // options.setImdbId(metadata.getImdbId());
  // options.setTmdbId(metadata.getTmdbId());
  //
  // // scrape trailers
  // for (IMediaTrailerProvider trailerProvider : trailerProviders) {
  // try {
  // List<MediaTrailer> foundTrailers = trailerProvider.getTrailers(options);
  // trailers.addAll(foundTrailers);
  // }
  // catch (Exception e) {
  // LOGGER.warn(e.getMessage());
  // }
  // }
  //
  // return trailers;
  // }

  /**
   * Gets the metadata.
   * 
   * @return the metadata
   */
  public MediaMetadata getMetadata() {
    return metadata;
  }

  /**
   * Checks if is scraped.
   * 
   * @return true, if is scraped
   */
  public boolean isScraped() {
    return scraped;
  }

  /**
   * Sets the tagline.
   * 
   * @param newValue
   *          the new tagline
   */
  public void setTagline(String newValue) {
    String oldValue = this.tagline;
    this.tagline = newValue;
    firePropertyChange("tagline", oldValue, newValue);
  }

  /**
   * Gets the tagline.
   * 
   * @return the tagline
   */
  public String getTagline() {
    return tagline;
  }

}