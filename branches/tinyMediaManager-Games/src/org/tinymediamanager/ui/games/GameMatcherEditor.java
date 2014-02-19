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
package org.tinymediamanager.ui.games;

import java.util.HashMap;

import org.tinymediamanager.core.game.Game;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * The Class GameMatcherEditor.
 * 
 * @author Manuel Laggner
 */
public class GameMatcherEditor extends AbstractMatcherEditor<Game> {

  /**
   * Instantiates a new game matcher editor.
   */
  public GameMatcherEditor() {
  }

  /**
   * Filter games.
   * 
   * @param filter
   *          the filter
   */
  public void filterGames(HashMap<GamesExtendedMatcher.SearchOptions, Object> filter) {
    Matcher<Game> matcher = new GamesExtendedMatcher(filter);
    fireChanged(matcher);
  }
}