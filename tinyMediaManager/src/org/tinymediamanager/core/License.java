/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.util.Url;

/**
 * The class License. Used for the generation/validation of the license file (for the donator version)
 * 
 * @author Myron Boyle
 */
public class License {
  private static final Logger LOGGER       = LoggerFactory.getLogger(License.class);
  private static final String LICENSE_FILE = "tmm.lic";
  private static final String UNKNOWN_MAC  = "UNKNOWN_MAC";

  /**
   * returns the MAC address of this instance
   * 
   * @return MAC or empty string
   */
  public static String getMac() {
    try {
      InetAddress ip = InetAddress.getLocalHost();
      if (ip != null) {
        // we are connected to Internet/router and have an IP
        NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
        String macAddress = formatMac(ni.getHardwareAddress());
        if (macAddress != null && !macAddress.isEmpty()) {
          return macAddress;
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("Error getting MAC from LocalHost IP - not connected to internet/router?");
    }

    try {
      for (Enumeration<NetworkInterface> nif = NetworkInterface.getNetworkInterfaces(); nif.hasMoreElements();) {
        NetworkInterface ni = null;
        try {
          ni = nif.nextElement();
          String macAddress = formatMac(ni.getHardwareAddress());
          if (macAddress != null && !macAddress.isEmpty()) {
            // get first
            return macAddress;
          }
        }
        catch (Exception e2) {
          LOGGER.warn("Error getting MAC of " + ni);
        }
      }
      return UNKNOWN_MAC;
    }
    catch (Exception e) {
      LOGGER.warn("I/O Error on getting network interfaces");
      return UNKNOWN_MAC;
    }
  }

  /**
   * returns ALL found MAC address of this instance
   * 
   * @return MAC or empty string
   */
  private static List<String> getAllMacAddresses() {
    List<String> m = new ArrayList<String>();
    m.add(UNKNOWN_MAC); // lic generated with empty mac, but java cannot handle this :/ use fake mac for further checks
    try {
      for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
        NetworkInterface ni = e.nextElement();
        String macAddress = formatMac(ni.getHardwareAddress());
        if (macAddress != null && !macAddress.isEmpty()) {
          m.add(macAddress);
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("Error getting MAC of all interfaces");
    }
    return m;
  }

  private static String formatMac(byte[] mac) {
    if (mac == null)
      return "";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }
    return sb.toString();
  }

  /**
   * checks if license is valid<br>
   * <b>DO NOT USE THIS - use Globals.isDonator()</b>
   * 
   * @return true or false (false also if not existent)
   */
  @Deprecated
  public static boolean isValid() {
    if (getLicenseFile() != null) {
      Properties lic = decrypt();
      if (lic != null) {
        // null only when not decryptable with "key"
        @SuppressWarnings("unused")
        String pleaseDoNotCrack = "";
        pleaseDoNotCrack += "Ok, you found it.";
        pleaseDoNotCrack += "This is the real deal.";
        pleaseDoNotCrack += "One char away from 'the full thing'.";
        pleaseDoNotCrack += "[...]";
        pleaseDoNotCrack += "If you are reading this, you're probably a java developer.";
        pleaseDoNotCrack += "If so, you know how much work can be in such a project like TMM.";
        pleaseDoNotCrack += "So please, be kind and support the developers for your free License :)";
        pleaseDoNotCrack += "http://www.tinymediamanager.org/index.php/donate/";
        pleaseDoNotCrack += "It gives you a warm and fuzzy feeling - i swear ;)";
        return true;
      }
    }
    return false;
  }

  /**
   * checks if license file exists
   * 
   * @return true or false
   */
  public static boolean exists() {
    if (getLicenseFile() != null) {
      return true;
    }
    return false;
  }

  /**
   * gets the license file (from possible locations)
   * 
   * @return
   */
  private static File getLicenseFile() {
    File f = new File(LICENSE_FILE); // app dir
    if (!f.exists()) {
      f = new File(System.getProperty("user.home") + File.separator + ".tmm", LICENSE_FILE);
      if (!f.exists()) {
        f = new File(System.getProperty("user.home"), LICENSE_FILE);
        if (!f.exists()) {
          return null;
        }
      }
    }
    return f;
  }

  public static Properties decrypt() {
    try {
      FileInputStream input = new FileInputStream(getLicenseFile());
      byte[] fileData = new byte[input.available()];
      input.read(fileData);
      input.close();
      String lic = new String(fileData, "UTF-8");

      String iv = "F27D5C9927726BCEFE7510B1BDD3D137";
      String salt = "3FF2EC019C627B945225DEBAD71A01B6985FE84C95A70EB132882F88C0A59A55";
      AesUtil util = new AesUtil(128, 100);

      Properties prop = new Properties();
      try {
        // try to decrypt with new/correct MAC implementation
        String decrypt = util.decrypt(salt, iv, getMac(), lic);
        StringReader reader = new StringReader(decrypt);
        prop.load(reader);
      }
      catch (Exception e) {
        // didn't work? try it with all our found MACs (+ an empty one of an old impl)
        for (String mac : getAllMacAddresses()) {
          try {
            String decrypt = util.decrypt(salt, iv, mac, lic);
            StringReader reader = new StringReader(decrypt);
            prop.load(reader);
          }
          catch (Exception e2) {
          }
        }
      }
      return prop.size() > 0 ? prop : null; // return NULL when properties are empty
    }
    catch (Exception e) {
      // file not found or whatever
      LOGGER.error("Error decrypting license file", e);
      return null;
    }
  }

  public static boolean encrypt(Properties props) {
    try {
      if (props == null || props.size() == 0) {
        return false;
      }

      String request = "https://script.google.com/macros/s/AKfycbz7gu6I046KesXCHJJe6OEPX2tx18RcfiMS5Id-7NXsNYYMnLvK/exec";
      String urlParameters = "mac=" + getMac();
      for (String key : props.stringPropertyNames()) {
        String value = props.getProperty(key);
        urlParameters += "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
      }
      Url url = new Url(request);

      HttpURLConnection connection = (HttpURLConnection) url.getUrl().openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setInstanceFollowRedirects(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("charset", "utf-8");
      connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
      connection.setUseCaches(false);

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
      writer.write(urlParameters);
      writer.flush();
      String response = IOUtils.toString(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      writer.close();
      if (response != null && response.isEmpty()) {
        return false;
      }

      // GET method
      // StringWriter writer = new StringWriter();
      // IOUtils.copy(url.getInputStream(), writer, "UTF-8");
      // String response = writer.toString();

      File f = new File(LICENSE_FILE);
      if (Globals.isRunningJavaWebStart()) {
        // when in webstart, put it in user home
        f = new File(System.getProperty("user.home") + File.separator + ".tmm", LICENSE_FILE);
        if (!f.getParentFile().exists()) {
          f.getParentFile().mkdir();
        }
      }
      FileUtils.writeStringToFile(f, response);

      return true;
    }
    catch (Exception e) {
      LOGGER.error("Error generating license", e);
      return false;
    }
  }

}
