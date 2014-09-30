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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowEditorDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowEpisodeEditorDialog;

/**
 * The Class TvShowEditAction. To edit TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowEditAction extends AbstractAction {
  private static final long           serialVersionUID = -3911290901017607679L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowEditAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, IconManager.EDIT);
    putValue(SMALL_ICON, IconManager.EDIT);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();

    for (Object obj : selectedObjects) {
      // display tv show editor
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        TvShowEditorDialog editor = new TvShowEditorDialog(tvShow, selectedObjects.size() > 1 ? true : false);
        if (!editor.showDialog()) {
          break;
        }
      }

      // change season poster
      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        ImageLabel imageLabel = new ImageLabel();
        ImageChooserDialog dialog = new ImageChooserDialog(season.getTvShow().getIds(), ImageType.SEASON, TvShowList.getInstance()
            .getArtworkProviders(), imageLabel, null, null, MediaType.TV_SHOW);
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);

        if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
          season.setPosterUrl(imageLabel.getImageUrl());
          season.getTvShow().downloadSeasonPoster(season.getSeason());
        }
      }

      // display tv episode editor
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
        TvShowEpisodeEditorDialog editor = new TvShowEpisodeEditorDialog(tvShowEpisode, selectedObjects.size() > 1 ? true : false);
        if (!editor.showDialog()) {
          break;
        }
      }
    }
  }
}
