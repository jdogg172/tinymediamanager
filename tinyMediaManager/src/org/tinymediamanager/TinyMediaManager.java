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

package org.tinymediamanager;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.io.File;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.MainWindow;

// TODO: Auto-generated Javadoc
/**
 * The Class TinyMediaManager.
 */
public class TinyMediaManager {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(TinyMediaManager.class); ;

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          // set look and feel
          setLookAndFeel();

          // check old version
          File file = new File("lib/beansbinding-1.2.1.jar");
          if (file.exists()) {
            JOptionPane.showMessageDialog(null, "Update from Alpha is not supported. Please download the actual version");
            return;
          }

          // init splash
          SplashScreen splash = SplashScreen.getSplashScreen();
          Graphics2D g2 = null;
          if (splash != null) {
            g2 = splash.createGraphics();
            if (g2 != null) {
              Font font = new Font("Dialog", Font.PLAIN, 14);
              g2.setFont(font);
            }
            else {
              LOGGER.debug("got no graphics from splash");
            }
          }
          else {
            LOGGER.debug("no splash found");
          }
          long timeStart = System.currentTimeMillis();

          // get logger configuration
          if (g2 != null) {
            updateProgress(g2, "loading logger");
            splash.update();
          }
          PropertyConfigurator.configure(TinyMediaManager.class.getResource("log4j.conf"));
          // DOMConfigurator.configure(TinyMediaManager.class.getResource("log4j.xml"));
          LOGGER.debug("starting tinyMediaManager");
          LOGGER.debug("default encoding " + System.getProperty("file.encoding"));

          // initialize database
          if (g2 != null) {
            updateProgress(g2, "initialize database");
            splash.update();
          }

          LOGGER.debug("initialize database");
          Globals.startDatabase();
          LOGGER.debug("database opened");

          // proxy settings
          if (Globals.settings.useProxy()) {
            LOGGER.debug("setting proxy");
            Globals.settings.setProxy();
          }

          // load database
          if (g2 != null) {
            updateProgress(g2, "loading movies");
            splash.update();
          }

          MovieList movieList = MovieList.getInstance();
          movieList.loadMoviesFromDatabase();

          // launch application
          if (g2 != null) {
            updateProgress(g2, "loading ui");
            splash.update();
          }
          long timeEnd = System.currentTimeMillis();
          if ((timeEnd - timeStart) > 3000) {
            try {
              Thread.sleep(3000 - (timeEnd - timeStart));
            }
            catch (Exception e) {
            }
          }
          MainWindow window = new MainWindow("tinyMediaManager " + org.tinymediamanager.ReleaseInfo.getVersion());

          if (g2 != null) {
            updateProgress(g2, "finished starting");
            splash.update();
          }

          window.setVisible(true);

        }
        catch (javax.persistence.PersistenceException e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
        }
        catch (Exception e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
          LOGGER.error("start of tmm", e);
        }
      }

      /**
       * Update progress on splash screen.
       * 
       * @param text
       *          the text
       */
      private void updateProgress(Graphics2D g2, String text) {
        // LOGGER.debug("graphics found");
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(20, 200, 480, 300);
        g2.setPaintMode();
        g2.setColor(Color.WHITE);
        g2.drawString(text + "...", 20, 300);
      }

      /**
       * Sets the look and feel.
       * 
       * @throws Exception
       *           the exception
       */
      private void setLookAndFeel() throws Exception {
        // Get the native look and feel class name
        // String laf = UIManager.getSystemLookAndFeelClassName();
        Properties props = new Properties();
        props.setProperty("controlTextFont", "Dialog 12");
        props.setProperty("systemTextFont", "Dialog 12");
        props.setProperty("userTextFont", "Dialog 12");
        props.setProperty("menuTextFont", "Dialog 12");
        props.setProperty("windowTitleFont", "Dialog bold 12");
        props.setProperty("subTextFont", "Dialog 10");

        // Get the look and feel class name
        com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(props);
        String laf = "com.jtattoo.plaf.luna.LunaLookAndFeel";

        // Install the look and feel
        UIManager.setLookAndFeel(laf);
      }
    });
  }
}
