package org.tinymediamanager.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaProvider;
import org.tinymediamanager.scraper.IMediaSubtitleProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;

public class PluginManager {
  private final static Logger                              LOGGER = LoggerFactory.getLogger(PluginManager.class);
  private static final net.xeoh.plugins.base.PluginManager pm     = PluginManagerFactory.createPluginManager();
  private static final PluginManagerUtil                   pmu    = new PluginManagerUtil(pm);
  private static PluginManager                             instance;

  public PluginManager() {
  }

  public synchronized static PluginManager getInstance() {
    if (instance == null) {
      instance = new PluginManager();

      long start = System.currentTimeMillis();
      LOGGER.debug("loading inline plugins...");
      // pm.addPluginsFrom(ClassURI.CLASSPATH); // sloooow
      pm.addPluginsFrom(ClassURI.CLASSPATH("org.tinymediamanager.scraper.**"));
      // pm.addPluginsFrom(ClassURI.PLUGIN(AniDBMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(FanartTvMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(HDTrailersNet.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(ImdbMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(MoviemeterMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(OfdbMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(OpensubtitlesMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(TheSubDbMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(TheTvDbMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(TmdbMetadataProvider.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(TraktTv.class));
      // pm.addPluginsFrom(ClassURI.PLUGIN(ZelluloidMetadataProvider.class));
      long end = System.currentTimeMillis();
      LOGGER.info("Done loading plugins - took " + (end - start) + " - " + Utils.MSECtoHHMMSS(end - start));

      // dedicated folder just for plugins
      LOGGER.debug("loading external plugins...");
      if (LOGGER.isTraceEnabled()) {
        pm.addPluginsFrom(new File("plugins/").toURI(), new OptionReportAfter());
      }
      else {
        pm.addPluginsFrom(new File("plugins/").toURI());
      }
    }
    return instance;
  }

  /**
   * All plugins implementing the IMediaProvider
   */
  public List<IMediaProvider> getPlugins() {
    ArrayList<IMediaProvider> plugins = new ArrayList<IMediaProvider>();
    for (Plugin p : pmu.getPlugins(IMediaProvider.class)) {
      plugins.add((IMediaProvider) p);
    }
    return plugins;
  }

  /**
   * All plugins implementing the IMediaMetadataProvider
   */
  public List<IMediaMetadataProvider> getMetadataPlugins() {
    ArrayList<IMediaMetadataProvider> plugins = new ArrayList<IMediaMetadataProvider>();
    for (Plugin p : pmu.getPlugins(IMediaMetadataProvider.class)) {
      plugins.add((IMediaMetadataProvider) p);
    }
    return plugins;
  }

  /**
   * All plugins implementing the IMediaArtworkProvider
   */
  public List<IMediaArtworkProvider> getArtworkPlugins() {
    ArrayList<IMediaArtworkProvider> plugins = new ArrayList<IMediaArtworkProvider>();
    for (Plugin p : pmu.getPlugins(IMediaArtworkProvider.class)) {
      plugins.add((IMediaArtworkProvider) p);
    }
    return plugins;
  }

  /**
   * All plugins implementing the IMediaTrailerProvider
   */
  public List<IMediaTrailerProvider> getTrailerPlugins() {
    ArrayList<IMediaTrailerProvider> plugins = new ArrayList<IMediaTrailerProvider>();
    for (Plugin p : pmu.getPlugins(IMediaTrailerProvider.class)) {
      plugins.add((IMediaTrailerProvider) p);
    }
    return plugins;
  }

  /**
   * All plugins implementing the IMediaSubtitleProvider
   */
  public List<IMediaSubtitleProvider> getSubtitlePlugins() {
    ArrayList<IMediaSubtitleProvider> plugins = new ArrayList<IMediaSubtitleProvider>();
    for (Plugin p : pmu.getPlugins(IMediaSubtitleProvider.class)) {
      plugins.add((IMediaSubtitleProvider) p);
    }
    return plugins;
  }

  /**
   * All plugins implementing the ITvShowMetadataProvider
   */
  public List<ITvShowMetadataProvider> getTvShowPlugins() {
    ArrayList<ITvShowMetadataProvider> plugins = new ArrayList<ITvShowMetadataProvider>();
    for (Plugin p : pmu.getPlugins(ITvShowMetadataProvider.class)) {
      plugins.add((ITvShowMetadataProvider) p);
    }
    return plugins;
  }

}
