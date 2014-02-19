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
package org.tinymediamanager.ui.settings;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageCache.CacheType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.ui.UTF8Control;

import ch.qos.logback.classic.Level;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class GeneralSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class GeneralSettingsPanel extends JPanel {

  private static final long           serialVersionUID = 500841588272296493L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();
  private List<LocaleComboBox>        locales          = new ArrayList<LocaleComboBox>();

  private JPanel                      panelProxySettings;
  private JTextField                  tfProxyHost;
  private JTextField                  tfProxyPort;
  private JTextField                  tfProxyUsername;
  private JPasswordField              tfProxyPassword;
  private JCheckBox                   chckbxClearCacheShutdown;
  private JLabel                      lblLoglevel;
  private JComboBox                   cbLogLevel;
  private JPanel                      panelCache;
  private JPanel                      panelLogger;
  private JPanel                      panelVideoFiletypes;
  private JTextField                  tfVideoFiletype;
  private JList                       listVideoFiletypes;
  private JPanel                      panelSubtitleFiletypes;
  private JTextField                  tfSubtitleFiletype;
  private JList                       listSubtitleFiletypes;
  private JLabel                      lblImageCacheQuality;
  private JComboBox                   cbImageCacheQuality;
  private JCheckBox                   chckbxBuildImageCache;
  private JCheckBox                   chckbxImageCache;
  private JList                       listSortPrefixes;
  private JTextField                  tfSortPrefix;
  private JPanel                      panelAudioFiletypes;
  private JList                       listAudioFiletypes;
  private JTextField                  tfAudioFiletype;
  private JPanel                      panelLanguage;
  private JLabel                      lblUiLanguage;
  private JComboBox                   cbLanguage;
  private JLabel                      lblLanguageHint;

  /**
   * Instantiates a new general settings panel.
   */
  public GeneralSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(200px;min)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(200px;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(200px;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(200px;default)"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    panelVideoFiletypes = new JPanel();
    panelVideoFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.videofiletypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    panelVideoFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPaneVideoFiletypes = new JScrollPane();
    panelVideoFiletypes.add(scrollPaneVideoFiletypes, "2, 2, fill, fill");

    listVideoFiletypes = new JList();
    scrollPaneVideoFiletypes.setViewportView(listVideoFiletypes);

    JButton btnRemoveVideoFiletype = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveVideoFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listVideoFiletypes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getVideoFileType().get(row);
          Globals.settings.removeVideoFileType(prefix);
        }
      }
    });
    panelVideoFiletypes.add(btnRemoveVideoFiletype, "4, 2, default, bottom");

    tfVideoFiletype = new JTextField();
    panelVideoFiletypes.add(tfVideoFiletype, "2, 4, fill, default");
    tfVideoFiletype.setColumns(10);

    JButton btnAddVideoFiletype = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddVideoFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfVideoFiletype.getText())) {
          Globals.settings.addVideoFileTypes(tfVideoFiletype.getText());
          tfVideoFiletype.setText("");
        }
      }
    });

    panelLanguage = new JPanel();
    add(panelLanguage, "2, 2, 3, 1, fill, fill");
    panelLanguage
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    lblUiLanguage = new JLabel(BUNDLE.getString("Settings.language")); //$NON-NLS-1$
    panelLanguage.add(lblUiLanguage, "2, 2, right, default");

    // listen to changes of the combo box
    ItemListener listener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    };

    LocaleComboBox actualLocale = null;

    // cbLanguage = new JComboBox(Utils.getLanguages().toArray());
    Locale settingsLang = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    for (Locale l : Utils.getLanguages()) {
      LocaleComboBox localeComboBox = new LocaleComboBox();
      localeComboBox.loc = l;
      locales.add(localeComboBox);
      if (l.equals(settingsLang)) {
        actualLocale = localeComboBox;
      }
    }
    cbLanguage = new JComboBox(locales.toArray());
    if (actualLocale != null) {
      cbLanguage.setSelectedItem(actualLocale);
    }
    panelLanguage.add(cbLanguage, "4, 2, fill, default");

    lblLanguageHint = new JLabel("");
    lblLanguageHint.setFont(lblLanguageHint.getFont().deriveFont(Font.BOLD));
    panelLanguage.add(lblLanguageHint, "2, 4, 5, 1");
    cbLanguage.addItemListener(listener);
    panelVideoFiletypes.add(btnAddVideoFiletype, "4, 4");
    add(panelVideoFiletypes, "2, 4, fill, fill");

    panelSubtitleFiletypes = new JPanel();
    add(panelSubtitleFiletypes, "4, 4, fill, fill");
    panelSubtitleFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.extrafiletypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    panelSubtitleFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    JScrollPane scrollPaneSubtitleFiletypes = new JScrollPane();
    panelSubtitleFiletypes.add(scrollPaneSubtitleFiletypes, "2, 2, fill, fill");

    listSubtitleFiletypes = new JList();
    scrollPaneSubtitleFiletypes.setViewportView(listSubtitleFiletypes);

    JButton btnRemoveSubtitleFiletype = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveSubtitleFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listSubtitleFiletypes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getSubtitleFileType().get(row);
          Globals.settings.removeSubtitleFileType(prefix);
        }
      }
    });
    panelSubtitleFiletypes.add(btnRemoveSubtitleFiletype, "4, 2, default, bottom");

    tfSubtitleFiletype = new JTextField();
    panelSubtitleFiletypes.add(tfSubtitleFiletype, "2, 4, fill, default");
    tfSubtitleFiletype.setColumns(10);

    JButton btnAddSubtitleFiletype = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddSubtitleFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfSubtitleFiletype.getText())) {
          Globals.settings.addSubtitleFileTypes(tfSubtitleFiletype.getText());
          tfSubtitleFiletype.setText("");
        }
      }
    });
    panelSubtitleFiletypes.add(btnAddSubtitleFiletype, "4, 4");

    panelAudioFiletypes = new JPanel();
    add(panelAudioFiletypes, "6, 4, fill, fill");
    panelAudioFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.audiofiletypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    panelAudioFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    JScrollPane scrollPaneAudioFiletypes = new JScrollPane();
    panelAudioFiletypes.add(scrollPaneAudioFiletypes, "2, 2, fill, fill");

    listAudioFiletypes = new JList();
    scrollPaneAudioFiletypes.setViewportView(listAudioFiletypes);

    JButton btnRemoveAudioFiletype = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveAudioFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listAudioFiletypes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getAudioFileType().get(row);
          Globals.settings.removeAudioFileType(prefix);
        }
      }
    });
    panelAudioFiletypes.add(btnRemoveAudioFiletype, "4, 2, default, bottom");

    tfAudioFiletype = new JTextField();
    panelAudioFiletypes.add(tfAudioFiletype, "2, 4, fill, default");
    tfAudioFiletype.setColumns(10);

    JButton btnAddAudioFiletype = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddAudioFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfAudioFiletype.getText())) {
          Globals.settings.addAudioFileTypes(tfAudioFiletype.getText());
          tfAudioFiletype.setText("");
        }
      }
    });
    panelAudioFiletypes.add(btnAddAudioFiletype, "4, 4");

    JPanel panelSortOptions = new JPanel();
    panelSortOptions.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.sorting"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelSortOptions, "8, 4, fill, fill");
    panelSortOptions.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPaneSortPrefixes = new JScrollPane();
    panelSortOptions.add(scrollPaneSortPrefixes, "2, 2, fill, fill");

    listSortPrefixes = new JList();
    scrollPaneSortPrefixes.setViewportView(listSortPrefixes);

    JButton btnRemoveSortPrefix = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listSortPrefixes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getTitlePrefix().get(row);
          Globals.settings.removeTitlePrefix(prefix);
          MovieList.getInstance().invalidateTitleSortable();
          TvShowList.getInstance().invalidateTitleSortable();
        }
      }
    });
    panelSortOptions.add(btnRemoveSortPrefix, "4, 2, default, bottom");

    tfSortPrefix = new JTextField();
    panelSortOptions.add(tfSortPrefix, "2, 4, fill, default");
    tfSortPrefix.setColumns(10);

    JButton btnAddSortPrefix = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfSortPrefix.getText())) {
          Globals.settings.addTitlePrefix(tfSortPrefix.getText());
          tfSortPrefix.setText("");
          MovieList.getInstance().invalidateTitleSortable();
          TvShowList.getInstance().invalidateTitleSortable();
        }
      }
    });
    panelSortOptions.add(btnAddSortPrefix, "4, 4");

    JTextPane tpSortingHints = new JTextPane();
    tpSortingHints.setFont(new Font("Dialog", Font.PLAIN, 10));
    tpSortingHints.setText(BUNDLE.getString("Settings.sorting.info")); //$NON-NLS-1$
    tpSortingHints.setBackground(UIManager.getColor("Panel.background"));
    panelSortOptions.add(tpSortingHints, "2, 6, 3, 1, fill, fill");

    panelCache = new JPanel();
    panelCache.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.cache"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelCache, "2, 6, 3, 1, fill, fill");
    panelCache.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxClearCacheShutdown = new JCheckBox(BUNDLE.getString("Settings.clearhttpcache"));//$NON-NLS-1$
    panelCache.add(chckbxClearCacheShutdown, "2, 2, 3, 1");

    chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecache"));//$NON-NLS-1$
    panelCache.add(chckbxImageCache, "2, 4, 3, 1");

    lblImageCacheQuality = new JLabel(BUNDLE.getString("Settings.imagecachetype"));//$NON-NLS-1$
    panelCache.add(lblImageCacheQuality, "2, 6, right, default");

    cbImageCacheQuality = new JComboBox(ImageCache.CacheType.values());
    panelCache.add(cbImageCacheQuality, "4, 6, fill, default");

    chckbxBuildImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecachebackground"));//$NON-NLS-1$
    chckbxBuildImageCache.setVisible(false);
    panelCache.add(chckbxBuildImageCache, "2, 8, 3, 1");

    panelProxySettings = new JPanel();
    panelProxySettings.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.proxy"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelProxySettings, "6, 6, 3, 1, fill, top");
    panelProxySettings.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblProxyHost = new JLabel(BUNDLE.getString("Settings.proxyhost")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyHost, "2, 2, right, default");

    tfProxyHost = new JTextField();
    lblProxyHost.setLabelFor(tfProxyHost);
    panelProxySettings.add(tfProxyHost, "4, 2, fill, default");
    tfProxyHost.setColumns(10);

    JLabel lblProxyPort = new JLabel(BUNDLE.getString("Settings.proxyport")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyPort, "2, 4, right, default");

    tfProxyPort = new JTextField();
    lblProxyPort.setLabelFor(tfProxyPort);
    panelProxySettings.add(tfProxyPort, "4, 4, fill, default");
    tfProxyPort.setColumns(10);

    JLabel lblProxyUser = new JLabel(BUNDLE.getString("Settings.proxyuser")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyUser, "2, 6, right, default");

    tfProxyUsername = new JTextField();
    lblProxyUser.setLabelFor(tfProxyUsername);
    panelProxySettings.add(tfProxyUsername, "4, 6, fill, default");
    tfProxyUsername.setColumns(10);

    JLabel lblProxyPassword = new JLabel(BUNDLE.getString("Settings.proxypass")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyPassword, "2, 8, right, default");

    tfProxyPassword = new JPasswordField();
    lblProxyPassword.setLabelFor(tfProxyPassword);
    panelProxySettings.add(tfProxyPassword, "4, 8, fill, default");

    panelLogger = new JPanel();
    add(panelLogger, "2, 8, 3, 1, fill, fill");
    panelLogger.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    lblLoglevel = new JLabel(BUNDLE.getString("Settings.loglevel"));//$NON-NLS-1$
    panelLogger.add(lblLoglevel, "2, 2");

    Level[] levels = new Level[] { Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
    Level actualLevel = Level.toLevel(Globals.settings.getLogLevel());
    cbLogLevel = new JComboBox(levels);
    panelLogger.add(cbLogLevel, "4, 2");
    cbLogLevel.addItemListener(listener);
    cbLogLevel.setSelectedItem(actualLevel);

    initDataBindings();
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    Level level = (Level) cbLogLevel.getSelectedItem();
    int actualLevel = Globals.settings.getLogLevel();
    if (actualLevel != level.levelInt) {
      Globals.settings.setLogLevel(level.levelInt);
    }

    LocaleComboBox loc = (LocaleComboBox) cbLanguage.getSelectedItem();
    Locale locale = loc.loc;
    Locale actualLocale = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    if (!locale.equals(actualLocale)) {
      Globals.settings.setLanguage(locale.getLanguage());
      lblLanguageHint.setText(BUNDLE.getString("Settings.languagehint")); //$NON-NLS-1$
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, String> settingsBeanProperty = BeanProperty.create("proxyHost");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, tfProxyHost, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_1 = BeanProperty.create("proxyPort");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, tfProxyPort, jTextFieldBeanProperty_1);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_2 = BeanProperty.create("proxyUsername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, tfProxyUsername, jTextFieldBeanProperty_2);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_3 = BeanProperty.create("proxyPassword");
    BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JPasswordField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfProxyPassword, jPasswordFieldBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("clearCacheShutdown");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxClearCacheShutdown, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_5 = BeanProperty.create("videoFileType");
    JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, listVideoFiletypes);
    jListBinding_1.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_6 = BeanProperty.create("subtitleFileType");
    JListBinding<String, Settings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, listSubtitleFiletypes);
    jListBinding_2.bind();
    //
    BeanProperty<Settings, CacheType> settingsBeanProperty_7 = BeanProperty.create("imageCacheType");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, CacheType, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbImageCacheQuality, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_8 = BeanProperty.create("imageCacheBackground");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, chckbxBuildImageCache, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("imageCache");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_10 = BeanProperty.create("titlePrefix");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, listSortPrefixes);
    jListBinding.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_11 = BeanProperty.create("audioFileType");
    JListBinding<String, Settings, JList> jListBinding_3 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, listAudioFiletypes);
    jListBinding_3.bind();
  }

  /**
   * Helper class for customized toString() method, to get the Name in localized language.
   */
  private class LocaleComboBox {
    private Locale loc;

    @Override
    public String toString() {
      return loc.getDisplayLanguage(loc);
    }
  }
}