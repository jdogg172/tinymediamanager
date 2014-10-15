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
package org.tinymediamanager;

import org.tinymediamanager.core.License;
import org.tinymediamanager.core.Settings;

/**
 * The Class Globals. used to hold global information/fields for the whole application
 * 
 * @author Manuel Laggner
 */
public class Globals {
  public static final Settings settings = Settings.getInstance();
  private static final boolean DONATOR  = License.isValid();

  /**
   * Have we donated?
   * 
   * @return true/false
   */
  public static boolean isDonator() {
    return DONATOR;
  }

  /**
   * Are we running from a webstart instance?
   * 
   * @return true/false
   */
  public static boolean isRunningJavaWebStart() {
    boolean hasJNLP = false;
    try {
      Class.forName("javax.jnlp.ServiceManager");
      hasJNLP = true;
    }
    catch (ClassNotFoundException ex) {
      hasJNLP = false;
    }
    return hasJNLP;
  }
}
