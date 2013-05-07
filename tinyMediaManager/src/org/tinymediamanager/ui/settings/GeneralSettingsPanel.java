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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
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
import org.tinymediamanager.core.Settings;
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

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The settings. */
  private Settings                    settings         = Settings.getInstance();

  /** The panel proxy settings. */
  private JPanel                      panelProxySettings;

  /** The tf proxy host. */
  private JTextField                  tfProxyHost;

  /** The tf proxy port. */
  private JTextField                  tfProxyPort;

  /** The tf proxy username. */
  private JTextField                  tfProxyUsername;

  /** The tf proxy password. */
  private JPasswordField              tfProxyPassword;

  /** The chckbx clear cache shutdown. */
  private JCheckBox                   chckbxClearCacheShutdown;

  /** The lbl loglevel. */
  private JLabel                      lblLoglevel;

  /** The combo box. */
  private JComboBox                   comboBox;
  private JPanel                      panelCache;
  private JPanel                      panelLogger;
  private JPanel                      panelVideoFiletypes;

  private JTextField                  tfVideoFiletype;

  private JList                       listVideoFiletypes;
  private JPanel                      panelSubtitleFiletypes;
  private JTextField                  tfSubtitleFiletype;

  private JList                       listSubtitleFiletypes;

  /**
   * Instantiates a new general settings panel.
   */
  public GeneralSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(300px;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(300px;default)"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), }));

    panelVideoFiletypes = new JPanel();
    panelVideoFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.filetypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
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
    panelVideoFiletypes.add(btnAddVideoFiletype, "4, 4");
    add(panelVideoFiletypes, "2, 2, fill, fill");

    panelSubtitleFiletypes = new JPanel();
    add(panelSubtitleFiletypes, "4, 2, fill, fill");
    panelSubtitleFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.filetypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
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

    panelCache = new JPanel();
    add(panelCache, "2, 4, fill, fill");
    panelCache.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxClearCacheShutdown = new JCheckBox(BUNDLE.getString("Settings.clearCache"));
    panelCache.add(chckbxClearCacheShutdown, "2, 2");

    panelLogger = new JPanel();
    add(panelLogger, "2, 6, fill, fill");
    panelLogger.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    lblLoglevel = new JLabel("Loglevel");
    panelLogger.add(lblLoglevel, "2, 2");
    // listen to changes of the combo box
    ItemListener listener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    };

    Level[] levels = new Level[] { Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
    Level actualLevel = Level.toLevel(Globals.settings.getLogLevel());
    comboBox = new JComboBox(levels);
    panelLogger.add(comboBox, "4, 2");
    comboBox.addItemListener(listener);
    comboBox.setSelectedItem(actualLevel);

    panelProxySettings = new JPanel();
    panelProxySettings.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.proxy"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelProxySettings, "2, 8, fill, top");
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

    initDataBindings();
  }

  private void checkChanges() {
    Level level = (Level) comboBox.getSelectedItem();
    int actualLevel = Globals.settings.getLogLevel();
    if (actualLevel != level.levelInt) {
      Globals.settings.setLogLevel(level.levelInt);
    }
  }

  /**
   * Inits the data bindings.
   */
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
  }
}
