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
package org.tinymediamanager.ui.settings;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieConnectors;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.ui.TmmUIHelper;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSettingsPanel.
 */
public class MovieSettingsPanel extends JPanel {

  /** The settings. */
  private Settings   settings = Settings.getInstance();

  /** The table movie sources. */
  private JTable     tableMovieSources;

  /** The cb nfo format. */
  private JComboBox  cbNfoFormat;

  /** The cb movie nfo filename1. */
  private JCheckBox  cbMovieNfoFilename1;

  /** The cb movie nfo filename2. */
  private JCheckBox  cbMovieNfoFilename2;

  /** The tf movie path. */
  private JTextField tfMoviePath;

  /** The tf movie filename. */
  private JTextField tfMovieFilename;

  /** The tf sort prefix. */
  private JTextField tfSortPrefix;

  /** The list sort prefixes. */
  private JList      listSortPrefixes;

  /** The tf filetype. */
  private JTextField tfFiletype;

  /** The list filetypes. */
  private JList      listFiletypes;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelMovieDataSources = new JPanel();

    panelMovieDataSources.setBorder(new TitledBorder(null, "Data Sources", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMovieDataSources, "2, 2, fill, top");
    panelMovieDataSources.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(72dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(66dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(44dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("100px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPane = new JScrollPane();
    panelMovieDataSources.add(scrollPane, "2, 2, 5, 1, fill, fill");

    tableMovieSources = new JTable();
    scrollPane.setViewportView(tableMovieSources);

    JPanel panelMovieSourcesButtons = new JPanel();
    panelMovieDataSources.add(panelMovieSourcesButtons, "8, 2, default, top");
    panelMovieSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton("Add");
    btnAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        File file = TmmUIHelper.selectDirectory("add datasource");
        if (file != null && file.exists() && file.isDirectory()) {
          settings.addMovieDataSources(file.getAbsolutePath());
        }
      }
    });

    panelMovieSourcesButtons.add(btnAdd, "2, 1, fill, top");

    JButton btnRemove = new JButton("Remove");
    btnRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int row = tableMovieSources.convertRowIndexToModel(tableMovieSources.getSelectedRow());
        if (row != -1) { // nothing selected
          String path = Globals.settings.getMovieDataSource().get(row);
          String[] choices = { "Continue", "Abort" };
          int decision = JOptionPane.showOptionDialog(null, "If you remove " + path
              + " from your data sources, all movies inside this path will also be removed. Continue?", "Remove datasource",
              JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, "Abort");
          if (decision == 0) {
            Globals.settings.removeMovieDataSources(path);
          }
        }
      }
    });
    panelMovieSourcesButtons.add(btnRemove, "2, 3, fill, top");

    JLabel lblNfoFormat = new JLabel("NFO format");
    panelMovieDataSources.add(lblNfoFormat, "2, 4, right, default");

    cbNfoFormat = new JComboBox(MovieConnectors.values());
    panelMovieDataSources.add(cbNfoFormat, "4, 4, fill, default");

    JLabel lblNfoFileNaming = new JLabel("NFO file naming");
    panelMovieDataSources.add(lblNfoFileNaming, "2, 6, right, default");

    cbMovieNfoFilename1 = new JCheckBox("<movie filename>.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename1, "4, 6");

    cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename2, "4, 7");

    JPanel panel = new JPanel();
    panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Filetypes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panel, "4, 2, fill, fill");
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPane_1 = new JScrollPane();
    panel.add(scrollPane_1, "2, 2, fill, fill");

    listFiletypes = new JList();
    scrollPane_1.setViewportView(listFiletypes);

    JButton btnRemoveFiletype = new JButton("Remove");
    btnRemoveFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listFiletypes.getSelectedIndex();
        String prefix = Globals.settings.getVideoFileType().get(row);
        Globals.settings.removeVideoFileType(prefix);
      }
    });
    panel.add(btnRemoveFiletype, "4, 2, default, bottom");

    tfFiletype = new JTextField();
    panel.add(tfFiletype, "2, 4, fill, default");
    tfFiletype.setColumns(10);

    JButton btnAddFiletype = new JButton("Add");
    btnAddFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfFiletype.getText())) {
          Globals.settings.addVideoFileTypes(tfFiletype.getText());
          tfFiletype.setText("");
        }
      }
    });
    panel.add(btnAddFiletype, "4, 4");

    JPanel panelSortOptions = new JPanel();
    panelSortOptions.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Movielist sorting", TitledBorder.LEADING,
        TitledBorder.TOP, null, null));
    add(panelSortOptions, "4, 4, fill, fill");
    panelSortOptions.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblSortingPrefixes = new JLabel("Title prefixes");
    panelSortOptions.add(lblSortingPrefixes, "2, 2");

    JScrollPane scrollPaneSortPrefixes = new JScrollPane();
    panelSortOptions.add(scrollPaneSortPrefixes, "2, 4, fill, fill");

    listSortPrefixes = new JList();
    scrollPaneSortPrefixes.setViewportView(listSortPrefixes);

    JButton btnRemoveSortPrefix = new JButton("Remove");
    btnRemoveSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listSortPrefixes.getSelectedIndex();
        String prefix = Globals.settings.getTitlePrefix().get(row);
        Globals.settings.removeTitlePrefix(prefix);
      }
    });
    panelSortOptions.add(btnRemoveSortPrefix, "4, 4, default, bottom");

    tfSortPrefix = new JTextField();
    panelSortOptions.add(tfSortPrefix, "2, 6, fill, default");
    tfSortPrefix.setColumns(10);

    JButton btnAddSortPrefix = new JButton("Add");
    btnAddSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfSortPrefix.getText())) {
          Globals.settings.addTitlePrefix(tfSortPrefix.getText());
          tfSortPrefix.setText("");
        }
      }
    });
    panelSortOptions.add(btnAddSortPrefix, "4, 6");

    JTextPane tpSortingHints = new JTextPane();
    tpSortingHints.setFont(new Font("Dialog", Font.PLAIN, 10));
    tpSortingHints
        .setText("Choose prefixes, which will affect the sort order of the movielist.\n\nExample:    The\nThe Bourne Identity   will be shown as    Bourne Identity, The\n\nIf you want to disable this feature, just clear the list");
    tpSortingHints.setBackground(UIManager.getColor("Panel.background"));
    panelSortOptions.add(tpSortingHints, "2, 8, 3, 1, fill, fill");

    // the panel renamer
    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null, "Renamer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelRenamer, "2, 4, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    JLabel lblMoviePath = new JLabel("Folder name");
    panelRenamer.add(lblMoviePath, "2, 2, right, default");

    tfMoviePath = new JTextField();
    panelRenamer.add(tfMoviePath, "4, 2, fill, default");
    tfMoviePath.setColumns(10);

    JTextPane txtpntTitle = new JTextPane();
    txtpntTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtpntTitle.setBackground(UIManager.getColor("Panel.background"));
    txtpntTitle
        .setText("Available pattern:\r\n$T = Title\r\n$O = OriginalTitle\r\n$1 = first letter of the title\r\n$Y = Year\r\n$I = IMDB number\r\n$E = Sort title\r\n$R = Video resolution\r\n$A = audio code + channels\r\n$V = video code + format");
    txtpntTitle.setEditable(false);
    panelRenamer.add(txtpntTitle, "6, 2, 1, 5, fill, fill");

    JLabel lblMovieFilename = new JLabel("File name");
    panelRenamer.add(lblMovieFilename, "2, 4, right, fill");

    tfMovieFilename = new JTextField();
    lblMovieFilename.setLabelFor(tfMovieFilename);
    panelRenamer.add(tfMovieFilename, "4, 4, fill, default");
    tfMovieFilename.setColumns(10);

    JTextPane txtrChooseAFolder = new JTextPane();
    txtrChooseAFolder.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtrChooseAFolder
        .setText("Choose a folder and file renaming pattern.\nExample:\nDatasource = /media/movies\nFolder name = $1/$T [$Y]\nFile name = $T\n\nResult:\nFolder name = /media/movies/A/Aladdin [1992]/\nFile name = Aladdin.avi\n\n\n\nIf fields are empty, the renaming will be skipped!");
    txtrChooseAFolder.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtrChooseAFolder, "2, 6, 3, 1, fill, fill");

    initDataBindings();

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }

    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }

    // item listener
    cbMovieNfoFilename1.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMovieNfoFilename2.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

  }

  // check changes of checkboxes
  /**
   * Check changes.
   */
  private void checkChanges() {
    // set NFO filenames
    settings.clearMovieNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<Settings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieDataSource");
    JTableBinding<String, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, settings, settingsBeanProperty_4,
        tableMovieSources);
    //
    ObjectProperty<String> stringObjectProperty = ObjectProperty.create();
    jTableBinding.addColumnBinding(stringObjectProperty).setColumnName("Source");
    //
    jTableBinding.bind();
    //
    BeanProperty<Settings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieConnector");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, MovieConnectors, JComboBox, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_11 = BeanProperty.create("movieRenamerPathname");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, tfMoviePath, jTextFieldBeanProperty_3);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_12 = BeanProperty.create("movieRenamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieFilename, jTextFieldBeanProperty_4);
    autoBinding_11.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty = BeanProperty.create("titlePrefix");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty,
        listSortPrefixes);
    jListBinding.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_1 = BeanProperty.create("videoFileType");
    JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, listFiletypes);
    jListBinding_1.bind();
  }
}