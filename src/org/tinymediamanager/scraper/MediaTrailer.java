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
package org.tinymediamanager.scraper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.util.Url;
import org.tinymediamanager.scraper.util.UrlUtil;
import org.tinymediamanager.scraper.util.YoutubeLinkExtractor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Trailer.
 * 
 * @author Manuel Laggner
 */
public class MediaTrailer extends AbstractModelObject implements Comparable<MediaTrailer> {
  private static final Logger LOGGER   = LoggerFactory.getLogger(MediaTrailer.class);

  @JsonProperty
  private String              name     = "";
  @JsonProperty
  private String              url      = "";
  @JsonProperty
  private String              quality  = "";
  @JsonProperty
  private String              provider = "";
  @JsonProperty
  private Boolean             inNfo    = Boolean.FALSE;
  @JsonProperty
  private String              size     = "";
  @JsonProperty
  private String              date     = "";

  /**
   * Instantiates a new trailer.
   */
  public MediaTrailer() {
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
   * Sets the name.
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
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
   * @param newValue
   *          the new url
   */
  public void setUrl(String newValue) {
    String oldValue = this.url;
    this.url = newValue;
    firePropertyChange("url", oldValue, newValue);
  }

  /**
   * Gets the quality.
   * 
   * @return the quality
   */
  public String getQuality() {
    return quality;
  }

  /**
   * Sets the quality.
   * 
   * @param newValue
   *          the new quality
   */
  public void setQuality(String newValue) {
    String oldValue = this.quality;
    this.quality = newValue;
    firePropertyChange("quality", oldValue, newValue);
  }

  /**
   * Gets the provider.
   * 
   * @return the provider
   */
  public String getProvider() {
    return provider;
  }

  /**
   * Sets the provider.
   * 
   * @param newValue
   *          the new provider
   */
  public void setProvider(String newValue) {
    String oldValue = this.provider;
    this.provider = newValue;
    firePropertyChange("provider", oldValue, newValue);
  }

  /**
   * Gets the in nfo.
   * 
   * @return the in nfo
   */
  public Boolean getInNfo() {
    return inNfo;
  }

  /**
   * Sets the in nfo.
   * 
   * @param newValue
   *          the new in nfo
   */
  public void setInNfo(Boolean newValue) {
    if (this.url.startsWith("file")) {
      // local trailers never in url
      newValue = false;
    }
    Boolean oldValue = this.inNfo;
    this.inNfo = newValue;
    firePropertyChange("inNfo", oldValue, newValue);
  }

  /**
   * Gets the size.
   * 
   * @return the size
   */
  public String getSize() {
    return size;
  }

  /**
   * Gets the date.
   * 
   * @return the date
   */
  public String getDate() {
    return date;
  }

  /**
   * Sets the size.
   * 
   * @param newValue
   *          the new size
   */
  public void setSize(String newValue) {
    String oldValue = this.size;
    this.size = newValue;
    firePropertyChange("size", oldValue, newValue);
  }

  /**
   * Sets the date.
   * 
   * @param newValue
   *          the new date
   */
  public void setDate(String newValue) {
    String oldValue = this.date;
    this.date = newValue;
    firePropertyChange("date", oldValue, newValue);
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
    return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
      @Override
      protected boolean accept(Field f) {
        return super.accept(f) && !f.getName().equals("propertyChangeSupport");
      }
    }).toString();
  }

  /**
   * Downloads Trailer to specified file.
   * 
   * @param file
   *          the absolute file on file system, overwrites existing trailer!
   * @throws IOException
   *           if url is not valid or network error
   * @throws URISyntaxException
   *           if url is not valid
   * @throws InterruptedException
   */
  public void downloadTo(String file) throws IOException, URISyntaxException, InterruptedException {
    LOGGER.info("Downloading " + this.getUrl() + " to " + file);

    Url u = new Url(UrlUtil.getURIEncoded(this.getDownloadUrl()).toASCIIString());
    if ("apple".equalsIgnoreCase(getProvider())) {
      u.setUserAgent("QuickTime");
    }
    FileOutputStream outputStream = new FileOutputStream(file);
    InputStream is = u.getInputStream();
    IOUtils.copy(is, outputStream);
    outputStream.close();
    is.close();
  }

  /**
   * gets the real download url - provider based implementation
   * 
   * @return real url
   */
  public String getDownloadUrl() {
    String url = getUrl();

    if ("youtube".equalsIgnoreCase(getProvider())) {
      try {
        YoutubeLinkExtractor yt = new YoutubeLinkExtractor(url);
        url = yt.extractVideoUrl();
      }
      catch (IOException e) {
        LOGGER.error("Error extracting Youtube url: " + e.getMessage());
      }
      catch (InterruptedException e) {
      }
    }

    return url;
  }

  @Override
  public boolean equals(Object mt2) {
    if ((mt2 != null) && (mt2 instanceof MediaTrailer)) {
      return compareTo((MediaTrailer) mt2) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(MediaTrailer mt2) {
    return this.getUrl().compareTo(mt2.getUrl());
  }

  @Override
  public int hashCode() {
    return this.getUrl().hashCode();
  }
}
