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
package org.tinymediamanager.core.tvshow.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowActor;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

/**
 * The Class TvShowToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "tvshow")
@XmlType(propOrder = { "title", "sorttitle", "year", "rating", "votes", "plot", "mpaa", "id", "genres", "premiered", "status", "studio", "thumb",
    "actors" })
public class TvShowToXbmcNfoConnector {

  private static final Logger LOGGER    = LoggerFactory.getLogger(TvShowToXbmcNfoConnector.class);
  private static JAXBContext  context   = initContext();

  private String              id        = "";
  private String              title     = "";
  private String              sorttitle = "";
  private float               rating    = 0;
  private int                 votes     = 0;
  private String              year      = "";
  private String              plot      = "";
  private String              mpaa      = "";
  private String              premiered = "";
  private String              studio    = "";
  private String              status    = "";

  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  @XmlElement(name = "genre")
  private List<String>        genres;

  /** not supported tags, but used to retrain in NFO. */

  @XmlElement
  List<Thumb>                 thumb;

  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(TvShowToXbmcNfoConnector.class, Actor.class);
    }
    catch (JAXBException e) {
      LOGGER.error(e.getMessage());
    }
    return null;
  }

  /**
   * Instantiates a new tv show to xbmc nfo connector.
   */
  public TvShowToXbmcNfoConnector() {
    genres = new ArrayList<String>();
    actors = new ArrayList<Object>();
  }

  /**
   * Sets the data.
   * 
   * @param tvShow
   *          the tv show
   * @return the string
   */
  public static void setData(TvShow tvShow) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return;
    }

    TvShowToXbmcNfoConnector xbmc = null;
    String nfoFilename = "tvshow.nfo";
    File nfoFile = new File(tvShow.getPath(), nfoFilename);

    // load existing NFO if possible
    if (nfoFile.exists()) {
      try {
        Unmarshaller um = context.createUnmarshaller();
        Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
        xbmc = (TvShowToXbmcNfoConnector) um.unmarshal(in);
      }
      catch (Exception e) {
        LOGGER.error("failed to parse " + nfoFilename + "; " + e.getMessage());
      }
    }

    // create new
    if (xbmc == null) {
      xbmc = new TvShowToXbmcNfoConnector();
    }

    // set data
    if (tvShow.getId("tvdb") != null) {
      xbmc.setId(tvShow.getId("tvdb").toString());
    }
    xbmc.setTitle(tvShow.getTitle());
    xbmc.setSorttitle(tvShow.getSortTitle());
    xbmc.setRating(tvShow.getRating());
    xbmc.setVotes(tvShow.getVotes());
    xbmc.setPlot(tvShow.getPlot());
    xbmc.setYear(tvShow.getYear());
    if (tvShow.getCertification() != null) {
      xbmc.setMpaa(tvShow.getCertification().getName());
    }
    xbmc.setPremiered(tvShow.getFirstAiredFormatted());
    xbmc.setStudio(tvShow.getStudio());
    xbmc.setStatus(tvShow.getStatus());

    xbmc.genres.clear();
    for (MediaGenres genre : tvShow.getGenres()) {
      xbmc.addGenre(genre.toString());
    }

    xbmc.actors.clear();
    for (TvShowActor actor : tvShow.getActors()) {
      xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumb());
    }

    // and marshall it
    try {
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      Writer w = new StringWriter();
      m.marshal(xbmc, w);
      StringBuilder sb = new StringBuilder(w.toString());
      w.close();

      // on windows make windows conform linebreaks
      if (SystemUtils.IS_OS_WINDOWS) {
        sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
      }

      FileUtils.write(nfoFile, sb, "UTF-8");
      tvShow.removeAllMediaFiles(MediaFileType.NFO);
      tvShow.addToMediaFiles(new MediaFile(nfoFile));
    }
    catch (Exception e) {
      LOGGER.error(nfoFilename + " " + e.getMessage());
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "message.nfo.writeerror", new String[] { ":",
          e.getLocalizedMessage() }));
    }
  }

  /**
   * Gets the data.
   * 
   * @param nfo
   *          the nfo filename
   * @return the data
   */
  public static TvShow getData(File nfo) {
    if (context == null) {
      return null;
    }

    // try to parse XML
    TvShow tvShow = null;
    try {
      TvShowToXbmcNfoConnector xbmc = parseNFO(nfo);
      tvShow = new TvShow();
      if (StringUtils.isNotBlank(xbmc.getId())) {
        tvShow.setId("tvdb", xbmc.getId());
      }
      tvShow.setTitle(xbmc.getTitle());
      tvShow.setSortTitle(xbmc.getSorttitle());
      tvShow.setRating(xbmc.getRating());
      tvShow.setVotes(xbmc.getVotes());
      tvShow.setYear(xbmc.getYear());
      tvShow.setPlot(xbmc.getPlot());
      tvShow.setCertification(Certification.findCertification(xbmc.getMpaa()));
      tvShow.setFirstAired(xbmc.getPremiered());
      tvShow.setStudio(xbmc.getStudio());
      tvShow.setStatus(xbmc.getStatus());

      for (String genre : xbmc.getGenres()) {
        String[] genres = genre.split("/");
        for (String g : genres) {
          MediaGenres genreFound = MediaGenres.getGenre(g.trim());
          if (genreFound != null) {
            tvShow.addGenre(genreFound);
          }
        }
      }

      for (Actor actor : xbmc.getActors()) {
        TvShowActor tvShowActor = new TvShowActor(actor.getName(), actor.getRole());
        tvShowActor.setThumb(actor.getThumb());
        tvShow.addActor(tvShowActor);
      }

      tvShow.addToMediaFiles(new MediaFile(nfo, MediaFileType.NFO));
    }
    catch (UnmarshalException e) {
      LOGGER.error("failed to parse " + nfo + " " + e.getMessage());
      // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }
    catch (Exception e) {
      LOGGER.error(nfo + " " + e.getMessage());
      // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }

    // only return if a movie name has been found
    if (StringUtils.isEmpty(tvShow.getTitle())) {
      return null;
    }
    return tvShow;
  }

  private static TvShowToXbmcNfoConnector parseNFO(File nfoFile) throws Exception {
    Unmarshaller um = context.createUnmarshaller();
    if (um == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      throw new Exception("could not create unmarshaller");
    }

    try {
      Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
      return (TvShowToXbmcNfoConnector) um.unmarshal(in);
    }
    catch (UnmarshalException e) {
      LOGGER.error("tried to unmarshal; now trying to clean xml stream");
    }

    // now trying to parse it via string
    String completeNFO = FileUtils.readFileToString(nfoFile, "UTF-8").trim().replaceFirst("^([\\W]+)<", "<");
    Reader in = new StringReader(completeNFO);
    return (TvShowToXbmcNfoConnector) um.unmarshal(in);
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  @XmlElement(name = "title")
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

  @XmlElement(name = "sorttitle")
  public String getSorttitle() {
    return sorttitle;
  }

  public void setSorttitle(String sorttitle) {
    this.sorttitle = sorttitle;
  }

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  @XmlElement(name = "rating")
  public float getRating() {
    return rating;
  }

  /**
   * Gets the plot.
   * 
   * @return the plot
   */
  @XmlElement(name = "plot")
  public String getPlot() {
    return plot;
  }

  /**
   * Sets the rating.
   * 
   * @param rating
   *          the new rating
   */
  public void setRating(float rating) {
    this.rating = rating;
  }

  /**
   * Gets the votes.
   * 
   * @return the votes
   */
  @XmlElement(name = "votes")
  public int getVotes() {
    return votes;
  }

  /**
   * Sets the votes.
   * 
   * @param votes
   *          the new votes
   */
  public void setVotes(int votes) {
    this.votes = votes;
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  @XmlElement(name = "year")
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
   * Gets the premiered.
   * 
   * @return the premiered
   */
  @XmlElement(name = "premiered")
  public String getPremiered() {
    return premiered;
  }

  /**
   * Sets the premiered.
   * 
   * @param premiered
   *          the new premiered
   */
  public void setPremiered(String premiered) {
    this.premiered = premiered;
  }

  /**
   * Sets the plot.
   * 
   * @param plot
   *          the new plot
   */
  public void setPlot(String plot) {
    this.plot = plot;
  }

  /**
   * Adds the genre.
   * 
   * @param genre
   *          the genre
   */
  public void addGenre(String genre) {
    genres.add(genre);
  }

  /**
   * Gets the genres.
   * 
   * @return the genres
   */
  public List<String> getGenres() {
    return this.genres;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  @XmlElement(name = "id")
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
   * Gets the mpaa.
   * 
   * @return the mpaa
   */
  @XmlElement(name = "mpaa")
  public String getMpaa() {
    return this.mpaa;
  }

  /**
   * Sets the mpaa.
   * 
   * @param mpaa
   *          the new mpaa
   */
  public void setMpaa(String mpaa) {
    this.mpaa = mpaa;
  }

  /**
   * Gets the studio.
   * 
   * @return the studio
   */
  @XmlElement(name = "studio")
  public String getStudio() {
    return studio;
  }

  /**
   * Sets the studio.
   * 
   * @param studio
   *          the new studio
   */
  public void setStudio(String studio) {
    this.studio = studio;
  }

  /**
   * Gets the status.
   * 
   * @return the status
   */
  @XmlElement(name = "status")
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   * 
   * @param status
   *          the new status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Adds the actor.
   * 
   * @param name
   *          the name
   * @param role
   *          the role
   * @param thumb
   *          the thumb
   */
  public void addActor(String name, String role, String thumb) {
    Actor actor = new Actor(name, role, thumb);
    actors.add(actor);
  }

  /**
   * Gets the actors.
   * 
   * @return the actors
   */
  public List<Actor> getActors() {
    // @XmlAnyElement(lax = true) causes all unsupported tags to be in actors;
    // filter Actors out
    List<Actor> pureActors = new ArrayList<Actor>();
    for (Object obj : actors) {
      if (obj instanceof Actor) {
        Actor actor = (Actor) obj;
        pureActors.add(actor);
      }
    }
    return pureActors;
  }

  // inner class actor to represent actors
  /**
   * The Class Actor.
   * 
   * @author Manuel Laggner
   */
  @XmlRootElement(name = "actor")
  public static class Actor {

    /** The name. */
    private String name;

    /** The role. */
    private String role;

    /** The thumb. */
    private String thumb;

    /**
     * Instantiates a new actor.
     */
    public Actor() {
    }

    /**
     * Instantiates a new actor.
     * 
     * @param name
     *          the name
     * @param role
     *          the role
     * @param thumb
     *          the thumb
     */
    public Actor(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    @XmlElement(name = "name")
    public String getName() {
      return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *          the new name
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Gets the role.
     * 
     * @return the role
     */
    @XmlElement(name = "role")
    public String getRole() {
      return role;
    }

    /**
     * Sets the role.
     * 
     * @param role
     *          the new role
     */
    public void setRole(String role) {
      this.role = role;
    }

    /**
     * Gets the thumb.
     * 
     * @return the thumb
     */
    @XmlElement(name = "thumb")
    public String getThumb() {
      return thumb;
    }

    /**
     * Sets the thumb.
     * 
     * @param thumb
     *          the new thumb
     */
    public void setThumb(String thumb) {
      this.thumb = thumb;
    }
  }

  /**
   * The Class Thumb.
   */
  public static class Thumb {

    /** The type. */
    @XmlAttribute
    String type;

    /** The season. */
    @XmlAttribute
    String season;

    /** The thumb. */
    @XmlValue
    String thumb;
  }
}