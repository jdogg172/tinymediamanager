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
package org.tinymediamanager.scraper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The Class MediaSearchResult.
 * 
 * @author Manuel Laggner
 */
public class MediaSearchResult implements Comparable<MediaSearchResult> {
  /** The provider id. */
  private String              providerId;

  /** The url. */
  private String              url;

  /** The title. */
  private String              title;

  /** The year. */
  private String              year;

  /** The original title. */
  private String              originalTitle;

  /** The id. */
  private String              id;

  /** The score. */
  private float               score;

  /** The extra args. */
  private Map<String, String> extraArgs = new HashMap<String, String>();

  /** The imdb id. */
  private String              imdbId;

  /** The metadata. */
  private MediaMetadata       metadata  = null;

  /** The type. */
  private MediaType           type;

  /** The poster url. */
  private String              posterUrl;

  /**
   * Instantiates a new media search result.
   * 
   * @param providerId
   *          the provider id
   */
  public MediaSearchResult(String providerId) {
    this.providerId = providerId;
  }

  /**
   * Gets the original title.
   * 
   * @return the original title
   */
  public String getOriginalTitle() {
    return originalTitle;
  }

  /**
   * Sets the original title.
   * 
   * @param originalTitle
   *          the new original title
   */
  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  /**
   * Instantiates a new media search result.
   * 
   * @param providerId
   *          the provider id
   * @param type
   *          the type
   * @param score
   *          the score
   */
  public MediaSearchResult(String providerId, MediaType type, float score) {
    this.providerId = providerId;
    this.type = type;
    this.score = score;
  }

  /**
   * Instantiates a new media search result.
   * 
   * @param providerId
   *          the provider id
   * @param id
   *          the id
   * @param title
   *          the title
   * @param year
   *          the year
   * @param score
   *          the score
   */
  public MediaSearchResult(String providerId, String id, String title, String year, float score) {
    super();
    this.providerId = providerId;
    this.id = id;
    this.title = title;
    this.year = year;
    this.score = score;
  }

  /**
   * Gets the provider id.
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Sets the provider id.
   * 
   * @param providerId
   *          the new provider id
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   * 
   * @param title
   *          the new title
   */
  public void setTitle(String title) {
    this.title = title;
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
    this.year = year;
  }

  /**
   * Gets the score.
   * 
   * @return the score
   */
  public float getScore() {
    return score;
  }

  /**
   * Sets the score.
   * 
   * @param score
   *          the new score
   */
  public void setScore(float score) {
    this.score = score;
  }

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   * 
   * @param url
   *          the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Adds the extra arg.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void addExtraArg(String key, String value) {
    this.extraArgs.put(key, value);
  }

  /**
   * Gets the media type.
   * 
   * @return the media type
   */
  public MediaType getMediaType() {
    return type;
  }

  /**
   * Sets the media type.
   * 
   * @param type
   *          the new media type
   */
  public void setMediaType(MediaType type) {
    this.type = type;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id
   *          the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the iMDB id.
   * 
   * @return the iMDB id
   */
  public String getIMDBId() {
    return imdbId;
  }

  /**
   * Sets the iMDB id.
   * 
   * @param imdbid
   *          the new iMDB id
   */
  public void setIMDBId(String imdbid) {
    this.imdbId = imdbid;
  }

  /**
   * Gets the extra.
   * 
   * @return the extra
   */
  public Map<String, String> getExtra() {
    return extraArgs;
  }

  /**
   * Gets the metadata.
   * 
   * @return the metadata
   */
  public MediaMetadata getMetadata() {
    return metadata;
  }

  /**
   * Sets the metadata.
   * 
   * @param md
   *          the new metadata
   */
  public void setMetadata(MediaMetadata md) {
    this.metadata = md;
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
   * @param posterUrl
   *          the new poster url
   */
  public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(MediaSearchResult arg0) {
    if (getScore() < arg0.getScore()) {
      return -1;
    }
    else if (getScore() == arg0.getScore()) {
      // same score - rank on year
      try {
        int y1 = Integer.valueOf(getYear());
        int y2 = Integer.valueOf(arg0.getYear());
        if (y1 > y2) {
          return 1;
        }
        else {
          return -1;
        }
      }
      catch (Exception e) {
        return 0;
      }
    }
    else {
      return 1;
    }
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}