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
package org.tinymediamanager.core.movie.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector.Actor;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector.Producer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaTrailer;

/**
 * The Class MovieToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso({ Actor.class, Producer.class })
@XmlType(propOrder = { "title", "originaltitle", "set", "sorttitle", "rating", "epbookmark", "year", "top250", "votes", "outline", "plot", "tagline",
    "runtime", "thumb", "fanart", "mpaa", "certifications", "id", "ids", "tmdbId", "trailer", "country", "premiered", "status", "code", "aired",
    "fileinfo", "watched", "playcount", "genres", "studio", "credits", "director", "tags", "actors", "producers", "resume", "lastplayed",
    "dateadded", "keywords", "poster", "url", "languages", "unsupportedElements" })
public class MovieToXbmcNfoConnector {
  private static final Logger LOGGER         = LoggerFactory.getLogger(MovieToXbmcNfoConnector.class);
  private static JAXBContext  context        = initContext();

  @XmlElement
  private String              title          = "";

  @XmlElement
  private String              originaltitle  = "";

  @XmlElement
  private String              year           = "";

  @XmlElement
  private String              outline        = "";

  @XmlElement
  private String              plot           = "";

  @XmlElement
  private String              tagline        = "";

  @XmlElement
  private String              runtime        = "";

  @XmlElement
  private String              thumb          = "";

  @XmlElement
  private String              fanart         = "";

  @XmlElement
  private String              id             = "";

  @XmlElementWrapper(name = "ids")
  private Map<String, Object> ids;

  @XmlElement
  private String              studio         = "";

  @XmlElement
  private String              country        = "";

  @XmlElement
  private String              mpaa           = "";

  @XmlElement(name = "certification")
  private String              certifications = "";

  @XmlElement
  private String              trailer        = "";

  @XmlElement
  private String              set            = "";

  @XmlElement
  private String              sorttitle      = "";

  @XmlElement
  private boolean             watched        = false;

  @XmlElement
  private float               rating         = 0;

  @XmlElement
  private int                 votes          = 0;

  @XmlElement
  private int                 playcount      = 0;

  @XmlElement
  private int                 tmdbId         = 0;

  @XmlElement
  private Fileinfo            fileinfo;

  @XmlElement
  private String              premiered      = "";

  @XmlElement(name = "director")
  private List<String>        director;

  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  @XmlAnyElement(lax = true)
  private List<Object>        producers;

  @XmlElement(name = "genre")
  private List<String>        genres;

  @XmlElement(name = "credits")
  private List<String>        credits;

  @XmlElement(name = "tag")
  private List<String>        tags;

  @XmlElement
  private String              top250;

  @XmlElement
  private String              languages;

  @XmlAnyElement(lax = true)
  private List<Object>        unsupportedElements;

  /** not supported tags, but used to retrain in NFO. */
  @XmlElement
  private String              epbookmark;

  @XmlElement
  private String              lastplayed;

  @XmlElement
  private String              status;

  @XmlElement
  private String              code;

  @XmlElement
  private String              aired;

  @XmlElement
  private Object              resume;

  @XmlElement
  private String              dateadded;

  @XmlElement
  private Object              keywords;

  @XmlElement
  private Object              poster;

  @XmlElement
  private Object              url;

  // @XmlElement(name = "rotten-tomatoes")
  // private Object rottentomatoes;

  /*
   * inits the context for faster marshalling/unmarshalling
   */
  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
    }
    catch (JAXBException e) {
      LOGGER.error("Error instantiating JaxB", e);
    }
    return null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public MovieToXbmcNfoConnector() {
    actors = new ArrayList();
    genres = new ArrayList<String>();
    tags = new ArrayList<String>();
    director = new ArrayList<String>();
    credits = new ArrayList<String>();
    producers = new ArrayList();
    ids = new HashMap<String, Object>();
    unsupportedElements = new ArrayList<Object>();
  }

  /**
   * Sets the data.
   * 
   * @param movie
   *          the movie
   */
  public static void setData(Movie movie) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return;
    }

    MovieToXbmcNfoConnector xbmc = null;
    List<Object> unsupportedTags = new ArrayList<Object>();

    // load existing NFO if possible
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.NFO)) {
      File file = mf.getFile();
      if (file.exists()) {
        try {
          xbmc = parseNFO(file);
        }
        catch (Exception e) {
          LOGGER.error("failed to parse " + mf.getFilename(), e);
        }
      }
      if (xbmc != null) {
        break;
      }
    }

    // create new
    if (xbmc == null) {
      xbmc = new MovieToXbmcNfoConnector();
    }
    else {
      // store all unsupported tags
      for (Object obj : xbmc.actors) { // ugly hack for invalid xml structure
        if (!(obj instanceof Producer) && !(obj instanceof Actor)) {
          unsupportedTags.add(obj);
        }
      }
    }

    // set data
    xbmc.title = movie.getTitle();
    xbmc.originaltitle = movie.getOriginalTitle();
    xbmc.rating = movie.getRating();
    xbmc.votes = movie.getVotes();
    if (movie.getTop250() == 0) {
      xbmc.top250 = "";
    }
    else {
      xbmc.top250 = String.valueOf(movie.getTop250());
    }
    xbmc.year = movie.getYear();
    xbmc.premiered = movie.getReleaseDateFormatted();
    xbmc.plot = movie.getPlot();

    // outline is only the first 200 characters of the plot
    int spaceIndex = 0;
    if (!StringUtils.isEmpty(xbmc.plot) && xbmc.plot.length() > 200) {
      spaceIndex = xbmc.plot.indexOf(" ", 200);
      if (spaceIndex > 0) {
        xbmc.outline = xbmc.plot.substring(0, spaceIndex);
      }
      else {
        xbmc.outline = xbmc.plot;
      }
    }
    else if (!StringUtils.isEmpty(xbmc.plot)) {
      spaceIndex = xbmc.plot.length();
      xbmc.outline = xbmc.plot.substring(0, spaceIndex);
    }

    xbmc.tagline = movie.getTagline();
    xbmc.runtime = String.valueOf(movie.getRuntime());
    xbmc.thumb = movie.getArtworkUrl(MediaFileType.POSTER);
    xbmc.fanart = movie.getArtworkUrl(MediaFileType.FANART);
    if (StringUtils.isNotBlank(movie.getArtworkFilename(MediaFileType.POSTER))) {
      xbmc.thumb = "";
    }
    else {
      xbmc.thumb = movie.getArtworkUrl(MediaFileType.POSTER);
    }

    if (StringUtils.isNotBlank(movie.getArtworkFilename(MediaFileType.FANART))) {
      xbmc.fanart = "";
    }
    else {
      xbmc.fanart = movie.getArtworkUrl(MediaFileType.FANART);
    }
    xbmc.id = movie.getImdbId();
    xbmc.tmdbId = movie.getTmdbId();

    xbmc.ids.putAll(movie.getIds());

    // only write first studio
    if (StringUtils.isNotEmpty(movie.getProductionCompany())) {
      String[] studio = movie.getProductionCompany().split(", ");
      if (studio.length > 0) {
        xbmc.studio = studio[0];
      }
    }

    xbmc.country = movie.getCountry();
    xbmc.watched = movie.isWatched();
    if (xbmc.watched) {
      xbmc.playcount = 1;
    }

    xbmc.languages = movie.getSpokenLanguages();

    // certifications
    if (movie.getCertification() != null) {
      if (MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry() == CountryCode.US) {
        // if we have US verts, write correct "Rated XX" String
        xbmc.mpaa = Certification.getMPAAString(movie.getCertification());
      }
      else {
        xbmc.mpaa = Certification.generateCertificationStringWithAlternateNames(movie.getCertification());
      }
      xbmc.certifications = Certification.generateCertificationStringWithAlternateNames(movie.getCertification());
    }

    // // filename and path
    // if (movie.getMediaFiles().size() > 0) {
    // xbmc.setFilenameandpath(movie.getPath() + File.separator +
    // movie.getMediaFiles().get(0).getFilename());
    // }

    // support of frodo director tags
    xbmc.director.clear();
    if (StringUtils.isNotEmpty(movie.getDirector())) {
      String directors[] = movie.getDirector().split(", ");
      for (String director : directors) {
        xbmc.director.add(director);
      }
    }

    // support of frodo credits tags
    xbmc.credits.clear();
    if (StringUtils.isNotEmpty(movie.getWriter())) {
      String writers[] = movie.getWriter().split(", ");
      for (String writer : writers) {
        xbmc.credits.add(writer);
      }
    }

    xbmc.actors.clear();
    for (MovieActor cast : new ArrayList<MovieActor>(movie.getActors())) {
      xbmc.addActor(cast.getName(), cast.getCharacter(), cast.getThumbUrl());
    }

    xbmc.producers.clear();
    for (MovieProducer producer : new ArrayList<MovieProducer>(movie.getProducers())) {
      xbmc.addProducer(producer.getName(), producer.getRole(), producer.getThumbUrl());
    }

    xbmc.genres.clear();
    for (MediaGenres genre : new ArrayList<MediaGenres>(movie.getGenres())) {
      xbmc.genres.add(genre.toString());
    }

    xbmc.trailer = "";
    for (MediaTrailer trailer : new ArrayList<MediaTrailer>(movie.getTrailers())) {
      if (trailer.getInNfo() && !trailer.getUrl().startsWith("file")) {
        // parse internet trailer url for nfo (do not add local one)
        xbmc.trailer = prepareTrailerForXbmc(trailer);
        break;
      }
    }
    // keep trailer already in NFO, remove tag only when empty
    if (xbmc.trailer.isEmpty()) {
      xbmc.trailer = null;
    }

    xbmc.tags.clear();
    for (String tag : new ArrayList<String>(movie.getTags())) {
      xbmc.tags.add(tag);
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      xbmc.set = movieSet.getTitle();
    }
    else {
      xbmc.set = "";
    }

    xbmc.sorttitle = movie.getSortTitle();

    // fileinfo
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.VIDEO)) {
      if (StringUtils.isEmpty(mediaFile.getVideoCodec())) {
        break;
      }

      // if (xbmc.fileinfo == null) {
      Fileinfo info = new Fileinfo();
      info.streamdetails.video.codec = mediaFile.getVideoCodec();
      info.streamdetails.video.aspect = String.valueOf(mediaFile.getAspectRatio());
      info.streamdetails.video.width = mediaFile.getVideoWidth();
      info.streamdetails.video.height = mediaFile.getVideoHeight();
      info.streamdetails.video.durationinseconds = movie.getRuntimeFromMediaFiles();

      for (MediaFileAudioStream as : mediaFile.getAudioStreams()) {
        Audio audio = new Audio();
        audio.codec = as.getCodec();
        audio.language = as.getLanguage();
        audio.channels = String.valueOf(as.getChannelsAsInt());
        info.streamdetails.audio.add(audio);
      }
      xbmc.fileinfo = info;
      break;
      // }
    }

    // add all unsupported tags again
    xbmc.unsupportedElements.addAll(unsupportedTags);

    // and marshall it
    String nfoFilename = "";
    List<MediaFile> newNfos = new ArrayList<MediaFile>(1);

    List<MovieNfoNaming> nfonames = new ArrayList<MovieNfoNaming>();
    if (movie.isMultiMovieDir()) {
      // Fixate the name regardless of setting
      nfonames.add(MovieNfoNaming.FILENAME_NFO);
    }
    else {
      nfonames = MovieModuleManager.MOVIE_SETTINGS.getMovieNfoFilenames();
      if (movie.isDisc()) {
        nfonames.add(MovieNfoNaming.DISC_NFO); // add additionally the NFO at disc style location
      }
    }
    for (MovieNfoNaming name : nfonames) {
      try {
        nfoFilename = movie.getNfoFilename(name);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dat = formatter.format(new Date());
        String comment = "<!-- created on " + dat + " - tinyMediaManager " + Globals.settings.getVersion() + " -->\n";
        m.setProperty("com.sun.xml.internal.bind.xmlHeaders", comment);

        // w = new FileWriter(nfoFilename);
        Writer w = new StringWriter();
        m.marshal(xbmc, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }
        File f = new File(movie.getPath(), nfoFilename);
        FileUtils.write(f, sb, "UTF-8");
        newNfos.add(new MediaFile(f));
      }
      catch (Exception e) {
        LOGGER.error("setData", e.getMessage());
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":",
            e.getLocalizedMessage() }));
      }
    }

    if (newNfos.size() > 0) {
      movie.removeAllMediaFiles(MediaFileType.NFO);
      movie.addToMediaFiles(newNfos);
    }
  }

  /**
   * Gets the data.
   * 
   * @param nfoFile
   *          the nfo filename
   * @return the data
   */
  public static Movie getData(File nfoFile) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      return null;
    }

    // try to parse XML
    Movie movie = null;
    try {
      MovieToXbmcNfoConnector xbmc = parseNFO(nfoFile);
      movie = new Movie();
      movie.setTitle(xbmc.title);
      movie.setOriginalTitle(xbmc.originaltitle);
      movie.setRating(xbmc.rating);
      movie.setVotes(xbmc.votes);
      movie.setYear(xbmc.year);
      if (StringUtils.isNotBlank(xbmc.top250)) {
        try {
          movie.setTop250(Integer.parseInt(xbmc.top250));
        }
        catch (NumberFormatException e) {
          movie.setTop250(0);
        }
      }
      else {
        movie.setTop250(0);
      }
      try {
        movie.setReleaseDate(xbmc.premiered);
      }
      catch (ParseException e) {
      }
      movie.setPlot(xbmc.plot);
      movie.setTagline(xbmc.tagline);
      try {
        String rt = xbmc.runtime.replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + xbmc.runtime + "; Movie: " + movie.getPath());
      }

      if (StringUtils.isNotBlank(xbmc.thumb)) {
        if (xbmc.thumb.contains("http://")) {
          movie.setArtworkUrl(xbmc.thumb, MediaFileType.POSTER);
        }
      }

      if (StringUtils.isNotBlank(xbmc.fanart)) {
        if (xbmc.fanart.contains("http://")) {
          movie.setArtworkUrl(xbmc.fanart, MediaFileType.FANART);
        }
      }

      for (Entry<String, Object> entry : xbmc.ids.entrySet()) {
        try {
          movie.setId(entry.getKey(), entry.getValue());
        }
        catch (Exception e) {
          LOGGER.warn("could not set ID: " + entry.getKey() + " ; " + entry.getValue());
        }
      }

      if (StringUtils.isBlank(movie.getImdbId())) {
        movie.setImdbId(xbmc.id);
      }
      if (movie.getTmdbId() == 0) {
        movie.setTmdbId(xbmc.tmdbId);
      }

      // convert director to internal format
      String director = "";
      for (String dir : xbmc.director) {
        if (!StringUtils.isEmpty(director)) {
          director += ", ";
        }
        director += dir;
      }
      movie.setDirector(director);

      // convert writer to internal format
      String writer = "";
      for (String wri : xbmc.credits) {
        if (StringUtils.isNotEmpty(writer)) {
          writer += ", ";
        }
        writer += wri;
      }
      movie.setWriter(writer);

      movie.setProductionCompany(xbmc.studio);
      movie.setCountry(xbmc.country);
      if (!StringUtils.isEmpty(xbmc.certifications)) {
        movie.setCertification(Certification.parseCertificationStringForMovieSetupCountry(xbmc.certifications));
      }
      if (!StringUtils.isEmpty(xbmc.mpaa) && movie.getCertification() == Certification.NOT_RATED) {
        movie.setCertification(Certification.parseCertificationStringForMovieSetupCountry(xbmc.mpaa));
      }
      movie.setWatched(xbmc.watched);
      if (xbmc.playcount > 0) {
        movie.setWatched(true);
      }
      movie.setSpokenLanguages(xbmc.languages);

      // movieset
      if (StringUtils.isNotEmpty(xbmc.set)) {
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.getMovieSet(xbmc.set, 0);

        // add movie to movieset
        if (movieSet != null) {
          movie.setMovieSet(movieSet);
        }
      }

      // be aware of the sorttitle - set an empty string if nothing has been
      // found
      if (StringUtils.isEmpty(xbmc.sorttitle)) {
        movie.setSortTitle("");
      }
      else {
        movie.setSortTitle(xbmc.sorttitle);
      }

      for (Actor actor : xbmc.getActors()) {
        MovieActor cast = new MovieActor(actor.name, actor.role);
        cast.setThumbUrl(actor.thumb);
        movie.addActor(cast);
      }

      for (Producer producer : xbmc.getProducers()) {
        MovieProducer cast = new MovieProducer(producer.name, producer.role);
        cast.setThumbUrl(producer.thumb);
        movie.addProducer(cast);
      }

      for (String genre : xbmc.genres) {
        String[] genres = genre.split("/");
        for (String g : genres) {
          MediaGenres genreFound = MediaGenres.getGenre(g.trim());
          if (genreFound != null) {
            movie.addGenre(genreFound);
          }
        }
      }

      if (StringUtils.isNotEmpty(xbmc.trailer)) {
        String urlFromNfo = parseTrailerUrl(xbmc.trailer);
        if (!urlFromNfo.startsWith("file")) {
          // only add new MT when not a local file
          MediaTrailer trailer = new MediaTrailer();
          trailer.setName("fromNFO");
          trailer.setProvider("from NFO");
          trailer.setQuality("unknown");
          trailer.setUrl(urlFromNfo);
          trailer.setInNfo(true);
          movie.addTrailer(trailer);
        }
      }

      for (String tag : xbmc.tags) {
        movie.addToTags(tag);
      }

    }
    catch (UnmarshalException e) {
      LOGGER.error("getData " + e.getMessage());
      return null;
    }
    catch (Exception e) {
      LOGGER.error("getData", e);
      return null;
    }

    // only return if a movie name has been found
    if (StringUtils.isEmpty(movie.getTitle())) {
      return null;
    }

    return movie;
  }

  private static MovieToXbmcNfoConnector parseNFO(File nfoFile) throws Exception {
    Unmarshaller um = context.createUnmarshaller();
    if (um == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      throw new Exception("could not create unmarshaller");
    }

    try {
      Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
      return (MovieToXbmcNfoConnector) um.unmarshal(in);
    }
    catch (UnmarshalException e) {
      LOGGER.error("tried to unmarshal; now trying to clean xml stream");
    }

    // now trying to parse it via string
    String completeNFO = FileUtils.readFileToString(nfoFile, "UTF-8").trim().replaceFirst("^([\\W]+)<", "<");
    Reader in = new StringReader(completeNFO);
    return (MovieToXbmcNfoConnector) um.unmarshal(in);
  }

  private void addActor(String name, String role, String thumb) {
    Actor actor = new Actor(name, role, thumb);
    actors.add(actor);
  }

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

  private void addProducer(String name, String role, String thumb) {
    Producer producer = new Producer(name, role, thumb);
    producers.add(producer);
  }

  public List<Producer> getProducers() {
    // @XmlAnyElement(lax = true) causes all unsupported tags to be in producers;
    // filter producers out
    List<Producer> pureProducers = new ArrayList<Producer>();
    // for (Object obj : producers) {
    for (Object obj : actors) { // ugly hack for invalid xml structure
      if (obj instanceof Producer) {
        Producer producer = (Producer) obj;
        pureProducers.add(producer);
      }
    }
    return pureProducers;
  }

  private static String prepareTrailerForXbmc(MediaTrailer trailer) {
    // youtube trailer are stored in a special notation: plugin://plugin.video.youtube/?action=play_video&videoid=<ID>
    // parse out the ID from the url and store it in the right notation
    Pattern pattern = Pattern.compile("https{0,1}://.*youtube..*/watch\\?v=(.*)$");
    Matcher matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      return "plugin://plugin.video.youtube/?action=play_video&videoid=" + matcher.group(1);
    }

    // other urls are handled by the hd-trailers.net plugin
    pattern = Pattern.compile("https{0,1}://.*(apple.com|yahoo-redir|yahoo.com|youtube.com|moviefone.com|ign.com|hd-trailers.net|aol.com).*");
    matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      try {
        return "plugin://plugin.video.hdtrailers_net/video/" + matcher.group(1) + "/" + URLEncoder.encode(trailer.getUrl(), "UTF-8");
      }
      catch (Exception e) {
        LOGGER.error("failed to escape " + trailer.getUrl());
      }
    }
    // everything else is stored directly
    return trailer.getUrl();
  }

  private static String parseTrailerUrl(String nfoTrailerUrl) {
    // try to parse out youtube trailer plugin
    Pattern pattern = Pattern.compile("plugin://plugin.video.youtube/\\?action=play_video&videoid=(.*)$");
    Matcher matcher = pattern.matcher(nfoTrailerUrl);
    if (matcher.matches()) {
      return "http://www.youtube.com/watch?v=" + matcher.group(1);
    }

    pattern = Pattern.compile("plugin://plugin.video.hdtrailers_net/video/.*\\?/(.*)$");
    matcher = pattern.matcher(nfoTrailerUrl);
    if (matcher.matches()) {
      try {
        return URLDecoder.decode(matcher.group(1), "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
        LOGGER.error("failed to unescape " + nfoTrailerUrl);
      }
    }

    return nfoTrailerUrl;
  }

  /*
   * inner class actor to represent actors
   */
  @XmlRootElement(name = "actor")
  public static class Actor {
    @XmlElement
    private String name;

    @XmlElement
    private String role;

    @XmlElement
    private String thumb;

    public Actor() {
    }

    public Actor(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }
  }

  /*
   * inner class to represent producers
   */
  @XmlRootElement(name = "producer")
  public static class Producer {
    @XmlElement
    private String name;

    @XmlElement
    private String role;

    @XmlElement
    private String thumb;

    public Producer() {
    }

    public Producer(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }
  }

  /*
   * inner class holding file informations
   */
  static class Fileinfo {
    @XmlElement
    Streamdetails streamdetails;

    public Fileinfo() {
      streamdetails = new Streamdetails();
    }
  }

  /*
   * inner class holding details of audio and video stream
   */
  static class Streamdetails {
    @XmlElement
    private Video       video;

    @XmlElement
    private List<Audio> audio;

    public Streamdetails() {
      video = new Video();
      audio = new ArrayList<Audio>();
    }
  }

  /*
   * inner class holding details of the video stream
   */
  static class Video {
    @XmlElement
    private String codec;

    @XmlElement
    private String aspect;

    @XmlElement
    private int    width;

    @XmlElement
    private int    height;

    @XmlElement
    private int    durationinseconds;
  }

  /*
   * inner class holding details of the audio stream
   */
  static class Audio {
    @XmlElement
    private String codec;

    @XmlElement
    private String language;

    @XmlElement
    private String channels;
  }
}
