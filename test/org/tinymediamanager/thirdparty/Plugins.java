package org.tinymediamanager.thirdparty;

import org.junit.Test;
import org.tinymediamanager.core.PluginManager;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.IXBMC;

public class Plugins {

  @Test
  public void load() {
    PluginManager pm = PluginManager.getInstance();

    System.out.println("------------------");
    System.out.println("classes implementing our base impl:");
    for (IMediaProvider p : pm.getPlugins()) {
      System.out.println("  " + p.getProviderInfo() + "   " + (p instanceof IXBMC));
    }

    System.out.println("------------------");
    System.out.println("classes implementing metadata scraping:");
    for (IMediaMetadataProvider p : pm.getMetadataPlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing artwork scraping:");
    for (IMediaArtworkProvider p : pm.getArtworkPlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing trailer scraping:");
    for (IMediaTrailerProvider p : pm.getTrailerPlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }

  }

}
