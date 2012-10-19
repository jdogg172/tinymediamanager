/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.scraper.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;

/**
 * The Class Url.
 */
public class Url {
  /** The log. */
  private static final Logger LOGGER = Logger.getLogger(Url.class);

  /** The url. */
  protected String url = null;

  /** The Constant HTTP_USER_AGENT. */
  protected static final String HTTP_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1";

  /**
   * Instantiates a new url.
   * 
   * @param url
   *          the url
   */
  public Url(String url) {
    this.url = url;
  }

  /**
   * Gets the url.
   * 
   * @return the url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getUrl() throws IOException {
    return new URL(url);
  }

  /**
   * Gets the input stream.
   *
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public InputStream getInputStream() throws IOException {
    DefaultHttpClient httpclient = getHttpClient();
    BasicHttpContext localContext = new BasicHttpContext();

    HttpGet httpget = new HttpGet(url);
    LOGGER.debug("getting " + url);
    HttpResponse response = httpclient.execute(httpget, localContext);
    HttpEntity entity = response.getEntity();

    if (entity != null) {
      return entity.getContent();
    }
    return null;
  }

  /**
   * Gets the bytes.
   *
   * @return the bytes
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public byte[] getBytes() throws IOException {
    InputStream is = getInputStream();
    byte[] bytes = IOUtils.toByteArray(is);
    return bytes;
  }

  /**
   * Gets the http client.
   *
   * @return the http client
   */
  protected DefaultHttpClient getHttpClient() {
    DefaultHttpClient client = new DefaultHttpClient();

    HttpParams params = client.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 5000);
    HttpConnectionParams.setSoTimeout(params, 5000);
    HttpProtocolParams.setUserAgent(params, HTTP_USER_AGENT);

    if ((Globals.settings.useProxy())) {
      setProxy(client);
    }

    return client;
  }

  /**
   * Sets the proxy.
   *
   * @param httpClient the new proxy
   */
  protected void setProxy(DefaultHttpClient httpClient) {
    HttpHost proxyHost = new HttpHost(Globals.settings.getProxyHost(), Integer.parseInt(Globals.settings.getProxyPort()));

    // authenticate
    if (!StringUtils.isEmpty(Globals.settings.getProxyUsername()) && !StringUtils.isEmpty(Globals.settings.getProxyPassword())) {
      if (Globals.settings.getProxyUsername().contains("\\")) {
        // use NTLM
        int offset = Globals.settings.getProxyUsername().indexOf("\\");
        String domain = Globals.settings.getProxyUsername().substring(0, offset);
        String username = Globals.settings.getProxyUsername().substring(offset + 1, Globals.settings.getProxyUsername().length());
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new NTCredentials(username, Globals.settings.getProxyPassword(), "", domain));
      } else {
        httpClient.getCredentialsProvider()
            .setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(Globals.settings.getProxyUsername(), Globals.settings.getProxyPassword()));
      }
    }

    // set proxy
    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
  }
}
