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
package org.tinymediamanager.ui.tvshows;

import java.awt.CardLayout;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.tvshows.actions.TvShowBulkEditAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowChangeSeasonPosterAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowClearImageCacheAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowDeleteAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowEditAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowExportAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowMediaInformationAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRemoveAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRenameAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRewriteEpisodeNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRewriteNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeEpisodesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeNewItemsAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSelectedScrapeAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSetWatchedFlagAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSingleScrapeAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncWatchedTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateDatasourcesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateSingleDatasourceAction;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeMediaInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.season.TvShowSeasonInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.season.TvShowSeasonMediaFilesPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowArtworkPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowExtendedSearchPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowMediaInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowTreePanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowSettingsContainerPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * the class TvShowUIModule - to handle the TV shows in the UI
 * 
 * @author Manuel Laggner
 */
public class TvShowUIModule implements ITmmUIModule {
  private final static ResourceBundle     BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static String             ID       = "tvShows";
  private static TvShowUIModule           instance = null;

  final TvShowSelectionModel              tvShowSelectionModel;
  final TvShowSeasonSelectionModel        tvShowSeasonSelectionModel;
  final TvShowEpisodeSelectionModel       tvShowEpisodeSelectionModel;

  private final JPanel                    settingsPanel;
  private final TvShowTreePanel           listPanel;
  private final JPanel                    detailPanel;
  private final JTabbedPane               tvShowDetailPanel;
  private final JTabbedPane               tvShowSeasonDetailPanel;
  private final JTabbedPane               tvShowEpisodeDetailPanel;
  private final JPanel                    dataPanel;
  private final TvShowExtendedSearchPanel filterPanel;

  private Action                          searchAction;
  private Action                          editAction;
  private Action                          updateAction;
  private Action                          exportAction;

  private JPopupMenu                      popupMenu;
  private JPopupMenu                      updatePopupMenu;
  private JPopupMenu                      scrapePopupMenu;
  private JPopupMenu                      editPopupMenu;

  private TvShowUIModule() {
    tvShowSelectionModel = new TvShowSelectionModel();
    tvShowSeasonSelectionModel = new TvShowSeasonSelectionModel();
    tvShowEpisodeSelectionModel = new TvShowEpisodeSelectionModel();

    listPanel = new TvShowTreePanel(tvShowSelectionModel);

    detailPanel = new JPanel();
    detailPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow") }, new RowSpec[] { RowSpec.decode("default:grow") }));

    // layeredpane for displaying the filter dialog at the top
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default"), ColumnSpec.decode("default:grow") }, new RowSpec[] {
        RowSpec.decode("default"), RowSpec.decode("default:grow") }));
    detailPanel.add(layeredPane, "1, 1, fill, fill");

    dataPanel = new JPanel();
    dataPanel.setLayout(new CardLayout());

    // panel for TV shows
    tvShowDetailPanel = new MainTabbedPane();
    tvShowDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowInformationPanel(tvShowSelectionModel));//$NON-NLS-1$
    tvShowDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowMediaInformationPanel(tvShowSelectionModel));//$NON-NLS-1$
    tvShowDetailPanel.add(BUNDLE.getString("metatag.artwork"), new TvShowArtworkPanel(tvShowSelectionModel)); //$NON-NLS-1$
    dataPanel.add(tvShowDetailPanel, "tvShow");

    // panel for seasons
    tvShowSeasonDetailPanel = new MainTabbedPane();
    tvShowSeasonDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowSeasonInformationPanel(tvShowSeasonSelectionModel));//$NON-NLS-1$
    tvShowSeasonDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowSeasonMediaFilesPanel(tvShowSeasonSelectionModel)); //$NON-NLS-1$
    // tvShowSeasonDetailPanel.add("Media Information", new TvShowSeasonMediaInformationPanel(tvShowSeasonSelectionModel));
    dataPanel.add(tvShowSeasonDetailPanel, "tvShowSeason");

    // panel for episodes
    tvShowEpisodeDetailPanel = new MainTabbedPane();
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowEpisodeInformationPanel(tvShowEpisodeSelectionModel));//$NON-NLS-1$
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowEpisodeMediaInformationPanel(tvShowEpisodeSelectionModel));//$NON-NLS-1$
    dataPanel.add(tvShowEpisodeDetailPanel, "tvShowEpisode");

    layeredPane.add(dataPanel, "1, 1, 2, 2, fill, fill");
    layeredPane.setLayer(dataPanel, 0);

    // glass pane for searching/filtering
    filterPanel = new TvShowExtendedSearchPanel(listPanel.getTree());
    filterPanel.setVisible(false);
    layeredPane.add(filterPanel, "1, 1, fill, fill");
    layeredPane.setLayer(filterPanel, 1);

    // create actions and menus
    createActions();
    createPopupMenu();

    settingsPanel = new TvShowSettingsContainerPanel();
  }

  public static TvShowUIModule getInstance() {
    if (instance == null) {
      instance = new TvShowUIModule();
    }
    return instance;
  }

  public void setFilterMenuVisible(boolean visible) {
    filterPanel.setVisible(visible);
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    return listPanel;
  }

  @Override
  public String getTabTitle() {
    return BUNDLE.getString("tmm.tvshows"); //$NON-NLS-1$
  }

  @Override
  public JComponent getDetailPanel() {
    return detailPanel;
  }

  @Override
  public Action getSearchAction() {
    return searchAction;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    return scrapePopupMenu;
  }

  @Override
  public Action getEditAction() {
    return editAction;
  }

  @Override
  public JPopupMenu getEditMenu() {
    return editPopupMenu;
  }

  @Override
  public Action getUpdateAction() {
    return updateAction;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    return updatePopupMenu;
  }

  @Override
  public Action getExportAction() {
    return exportAction;
  }

  @Override
  public JPopupMenu getExportMenu() {
    return null;
  }

  public TvShowSelectionModel getSelectionModel() {
    return tvShowSelectionModel;
  }

  @Override
  public JPanel getSettingsPanel() {
    return settingsPanel;
  }

  private void createActions() {
    searchAction = new TvShowSingleScrapeAction(false);
    editAction = new TvShowEditAction(false);
    updateAction = new TvShowUpdateDatasourcesAction(false);
    exportAction = new TvShowExportAction();
  }

  private void createPopupMenu() {
    // popup menu
    popupMenu = new JPopupMenu();
    popupMenu.add(new TvShowSingleScrapeAction(true));
    popupMenu.add(new TvShowSelectedScrapeAction());
    popupMenu.add(new TvShowScrapeEpisodesAction(true));
    popupMenu.add(new TvShowScrapeEpisodesAction(false));
    popupMenu.add(new TvShowScrapeNewItemsAction());
    // popupMenu.add(actionScrapeMetadataSelected);
    popupMenu.addSeparator();
    popupMenu.add(new TvShowUpdateAction());
    popupMenu.addSeparator();
    popupMenu.add(new TvShowEditAction(true));
    popupMenu.add(new TvShowChangeSeasonPosterAction(true));
    popupMenu.add(new TvShowBulkEditAction());
    popupMenu.add(new TvShowSetWatchedFlagAction());
    popupMenu.add(new TvShowRewriteNfoAction());
    popupMenu.add(new TvShowRewriteEpisodeNfoAction());
    // popupMenu.add(actionBatchEdit);
    popupMenu.add(new TvShowRenameAction());
    popupMenu.add(new TvShowMediaInformationAction(true));
    popupMenu.add(new TvShowExportAction());
    popupMenu.add(new TvShowClearImageCacheAction());
    popupMenu.addSeparator();
    popupMenu.add(new TvShowSyncTraktTvAction());
    popupMenu.add(new TvShowSyncWatchedTraktTvAction());
    popupMenu.addSeparator();
    popupMenu.add(new TvShowRemoveAction(true));
    popupMenu.add(new TvShowDeleteAction(true));

    listPanel.setPopupMenu(popupMenu);

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(new TvShowUpdateDatasourcesAction(true));
        updatePopupMenu.addSeparator();
        for (String ds : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
          updatePopupMenu.add(new TvShowUpdateSingleDatasourceAction(ds));
        }
        updatePopupMenu.addSeparator();
        updatePopupMenu.add(new TvShowUpdateAction());
        updatePopupMenu.pack();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    // scrape popup menu
    scrapePopupMenu = new JPopupMenu();
    scrapePopupMenu.add(new TvShowSingleScrapeAction(true));
    scrapePopupMenu.add(new TvShowSelectedScrapeAction());
    scrapePopupMenu.add(new TvShowScrapeNewItemsAction());

    // edit popupmenu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(new TvShowEditAction(true));
    editPopupMenu.add(new TvShowChangeSeasonPosterAction(true));
    editPopupMenu.add(new TvShowBulkEditAction());
    editPopupMenu.add(new TvShowSetWatchedFlagAction());
    editPopupMenu.add(new TvShowRewriteNfoAction());
    editPopupMenu.add(new TvShowRewriteEpisodeNfoAction());
  }

  /**
   * set the selected TV shows. This causes the right sided panel to switch to the TV show information panel
   * 
   * @param tvShow
   *          the selected TV show
   */
  public void setSelectedTvShow(TvShow tvShow) {
    tvShowSelectionModel.setSelectedTvShow(tvShow);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShow");
  }

  /**
   * set the selected TV show season. This causes the right sided panel to switch to the season information panel
   * 
   * @param tvShowSeason
   *          the selected season
   */
  public void setSelectedTvShowSeason(TvShowSeason tvShowSeason) {
    tvShowSeasonSelectionModel.setSelectedTvShowSeason(tvShowSeason);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShowSeason");
  }

  /**
   * set the selected TV show episode. This cases the right sided panel to switch to the episode information panel
   * 
   * @param tvShowEpisode
   *          the selected episode
   */
  public void setSelectedTvShowEpisode(TvShowEpisode tvShowEpisode) {
    tvShowEpisodeSelectionModel.setSelectedTvShowEpisode(tvShowEpisode);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShowEpisode");
  }
}
