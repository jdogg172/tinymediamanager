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
package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.core.movie.MovieCertification;
import org.tinymediamanager.scraper.MediaMetadata.Genres;
import org.tinymediamanager.ui.ImageChooser.ImageType;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieEditor.
 */
public class MovieEditor extends JDialog {

  /** The content panel. */
  private final JPanel             contentPanel      = new JPanel();

  /** The movie to edit. */
  private Movie                    movieToEdit;

  /** The tf title. */
  private JTextField               tfTitle;

  /** The tf original title. */
  private JTextField               tfOriginalTitle;

  /** The tf year. */
  private JTextField               tfYear;

  /** The tp plot. */
  private JTextPane                tpPlot;

  /** The tf director. */
  private JTextField               tfDirector;

  /** The table. */
  private JTable                   tableActors;

  /** The lbl movie path. */
  private JLabel                   lblMoviePath;

  /** The lbl poster. */
  private ImageLabel               lblPoster;

  /** The lbl fanart. */
  private ImageLabel               lblFanart;

  /** The cast. */
  private List<MovieCast>          cast              = ObservableCollections.observableList(new ArrayList<MovieCast>());

  /** The genres. */
  private List<Genres>             genres            = ObservableCollections.observableList(new ArrayList<Genres>());

  /** The certifications */
  private List<MovieCertification> certifications    = ObservableCollections.observableList(new ArrayList<MovieCertification>());

  /** The action ok. */
  private final Action             actionOK          = new SwingAction();

  /** The action cancel. */
  private final Action             actionCancel      = new SwingAction_1();

  /** The action add actor. */
  private final Action             actionAddActor    = new SwingAction_4();

  /** The action remove actor. */
  private final Action             actionRemoveActor = new SwingAction_5();
  private JTextField               tfWriter;
  private JTextField               tfRuntime;
  private JTextField               tfProductionCompanies;
  private JList                    listGenres;
  private final Action             actionAddGenre    = new SwingAction_2();
  private final Action             actionRemoveGenre = new SwingAction_3();
  private JComboBox                cbGenres;
  private JTable                   tableCertification;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   */
  public MovieEditor(Movie movie) {
    setModal(true);
    setTitle("Change Movie");
    movieToEdit = movie;
    setBounds(100, 100, 944, 660);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("150px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("40px"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("right:300px:grow"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(150px;default)"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default"),
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow(2)"), }));
    {
      lblMoviePath = new JLabel("");
      contentPanel.add(lblMoviePath, "2, 2, 11, 1");
    }
    {
      JLabel lblTitle = new JLabel("Title");
      contentPanel.add(lblTitle, "2, 4, right, default");
    }
    {
      tfTitle = new JTextField();
      contentPanel.add(tfTitle, "4, 4, 7, 1, fill, default");
      tfTitle.setColumns(10);
    }
    {
      // JLabel lblPoster = new JLabel("");
      lblPoster = new ImageLabel();
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooser dialog = new ImageChooser(movieToEdit.getImdbId(), movieToEdit.getTmdbId(), ImageType.POSTER, lblPoster);
          dialog.setVisible(true);
        }
      });
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      contentPanel.add(lblPoster, "12, 4, 1, 9, fill, fill");
    }
    {
      JLabel lblOriginalTitle = new JLabel("Originaltitle");
      contentPanel.add(lblOriginalTitle, "2, 6, right, default");
    }
    {
      tfOriginalTitle = new JTextField();
      contentPanel.add(tfOriginalTitle, "4, 6, 7, 1, fill, top");
      tfOriginalTitle.setColumns(10);
    }
    {
      JLabel lblYear = new JLabel("Year");
      contentPanel.add(lblYear, "2, 8, right, default");
    }
    {
      tfYear = new JTextField();
      contentPanel.add(tfYear, "4, 8, fill, top");
      tfYear.setColumns(10);
    }
    {
      JLabel lblRuntime = new JLabel("Runtime");
      contentPanel.add(lblRuntime, "6, 8, right, default");
    }
    {
      tfRuntime = new JTextField();
      contentPanel.add(tfRuntime, "8, 8, fill, default");
      tfRuntime.setColumns(10);
    }
    {
      JLabel lblMin = new JLabel("min");
      contentPanel.add(lblMin, "10, 8");
    }
    {
      JLabel lblPlot = new JLabel("Plot");
      contentPanel.add(lblPlot, "2, 10, right, top");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 10, 7, 1, fill, fill");
      {
        tpPlot = new JTextPane();
        scrollPane.setViewportView(tpPlot);
      }
    }
    {
      JLabel lblDirector = new JLabel("Director");
      contentPanel.add(lblDirector, "2, 12, right, default");
    }
    {
      tfDirector = new JTextField();
      contentPanel.add(tfDirector, "4, 12, 7, 1, fill, top");
      tfDirector.setColumns(10);
    }
    {
      JLabel lblWriter = new JLabel("Writer");
      contentPanel.add(lblWriter, "2, 14, right, default");
    }
    {
      tfWriter = new JTextField();
      contentPanel.add(tfWriter, "4, 14, 7, 1, fill, top");
      tfWriter.setColumns(10);
    }
    {
      JLabel lblCompany = new JLabel("Production");
      contentPanel.add(lblCompany, "2, 16, right, default");
    }
    {
      tfProductionCompanies = new JTextField();
      contentPanel.add(tfProductionCompanies, "4, 16, 7, 1, fill, default");
      tfProductionCompanies.setColumns(10);
    }
    {
      JLabel lblActors = new JLabel("Actors");
      contentPanel.add(lblActors, "2, 18, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 18, 3, 7, fill, fill");
      {
        tableActors = new JTable();
        scrollPane.setViewportView(tableActors);
      }
    }
    {
      // JLabel lblFanart = new JLabel("");
      lblFanart = new ImageLabel();
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooser dialog = new ImageChooser(movieToEdit.getImdbId(), movieToEdit.getTmdbId(), ImageType.FANART, lblFanart);
          dialog.setVisible(true);
        }
      });
      contentPanel.add(lblFanart, "12, 14, 1, 11, fill, fill");
    }
    {
      JLabel lblGenres = new JLabel("Genres");
      contentPanel.add(lblGenres, "8, 18, right, default");
    }
    {
      JButton btnAddActor = new JButton("Add Actor");
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(actionAddActor);
      btnAddActor.setIcon(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Add-User.png")));
      contentPanel.add(btnAddActor, "2, 20, right, top");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "10, 18, 1, 5, fill, fill");
      {
        listGenres = new JList();
        scrollPane.setViewportView(listGenres);
      }
    }
    {
      JButton btnAddGenre = new JButton("");
      btnAddGenre.setAction(actionAddGenre);
      btnAddGenre.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      contentPanel.add(btnAddGenre, "8, 20, right, top");
    }
    {
      JButton btnRemoveActor = new JButton("Remove Actor");
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(actionRemoveActor);
      btnRemoveActor.setIcon(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Remove-User.png")));
      contentPanel.add(btnRemoveActor, "2, 22, right, top");
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane
          .setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
              ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"),
              FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JButton okButton = new JButton("OK");
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "2, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "4, 2, fill, top");
      }
    }

    {
      lblMoviePath.setText(movie.getPath() + File.separator + movie.getMovieFiles().get(0));
      tfTitle.setText(movie.getName());
      tfOriginalTitle.setText(movie.getOriginalName());
      tfYear.setText(movie.getYear());
      tfRuntime.setText(String.valueOf(movie.getRuntime()));
      tpPlot.setText(movie.getOverview());
      tfDirector.setText(movie.getDirector());
      tfWriter.setText(movie.getWriter());
      lblPoster.setImagePath(movie.getPoster());
      lblFanart.setImagePath(movie.getFanart());
      tfProductionCompanies.setText(movie.getProductionCompany());
      {
        JButton btnRemoveGenre = new JButton("");
        btnRemoveGenre.setAction(actionRemoveGenre);
        btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
        btnRemoveGenre.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
        contentPanel.add(btnRemoveGenre, "8, 22, right, top");
      }
      {
        cbGenres = new JComboBox(Genres.values());
        contentPanel.add(cbGenres, "10, 24");
      }
      {
        JLabel lblCertification = new JLabel("Certification");
        contentPanel.add(lblCertification, "2, 26");
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        contentPanel.add(scrollPane, "4, 26, 3, 3, fill, fill");
        {
          tableCertification = new JTable();
          scrollPane.setViewportView(tableCertification);
        }
      }

      for (MovieCast origCast : movie.getActors()) {
        MovieCast actor = new MovieCast();
        actor.setName(origCast.getName());
        actor.setType(origCast.getType());
        actor.setCharacter(origCast.getCharacter());
        cast.add(actor);
      }

      for (Genres genre : movie.getGenres()) {
        genres.add(genre);
      }

      for (MovieCertification origCertification : movie.getCertifications()) {
        MovieCertification certification = new MovieCertification(origCertification.getCountry(), origCertification.getCertification());
        certifications.add(certification);
      }
    }
    initDataBindings();
  }

  /**
   * The Class SwingAction.
   */
  private class SwingAction extends AbstractAction {

    /**
     * Instantiates a new swing action.
     */
    public SwingAction() {
      putValue(NAME, "Ok");
      putValue(SHORT_DESCRIPTION, "Change movie");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      movieToEdit.setName(tfTitle.getText());
      movieToEdit.setOriginalName(tfOriginalTitle.getText());
      movieToEdit.setYear(tfYear.getText());
      movieToEdit.setRuntime(Integer.parseInt(tfRuntime.getText()));

      if (!StringUtils.isEmpty(lblPoster.getImageUrl()) && lblPoster.getImageUrl() != movieToEdit.getPosterUrl()) {
        movieToEdit.setPosterUrl(lblPoster.getImageUrl());
        movieToEdit.writeImages(true, false);
      }

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && lblFanart.getImageUrl() != movieToEdit.getFanartUrl()) {
        movieToEdit.setFanartUrl(lblFanart.getImageUrl());
        movieToEdit.writeImages(false, true);
      }

      movieToEdit.setDirector(tfDirector.getText());
      movieToEdit.setWriter(tfWriter.getText());
      movieToEdit.setProductionCompany(tfProductionCompanies.getText());

      movieToEdit.removeAllActors();
      for (MovieCast actor : cast) {
        movieToEdit.addToCast(actor);
      }

      movieToEdit.removeAllGenres();
      for (Genres genre : genres) {
        movieToEdit.addGenre(genre);
      }

      movieToEdit.removeAllCertifications();
      for (MovieCertification certification : certifications) {
        movieToEdit.addCertification(certification);
      }

      movieToEdit.saveToDb();
      movieToEdit.writeNFO();

      setVisible(false);
    }
  }

  /**
   * The Class SwingAction_1.
   */
  private class SwingAction_1 extends AbstractAction {

    /**
     * Instantiates a new swing action_1.
     */
    public SwingAction_1() {
      putValue(NAME, "Cancel");
      putValue(SHORT_DESCRIPTION, "Discard changes");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  /**
   * The Class SwingAction_4.
   */
  private class SwingAction_4 extends AbstractAction {

    /**
     * Instantiates a new swing action_4.
     */
    public SwingAction_4() {
      putValue(SHORT_DESCRIPTION, "Add a new Actor");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MovieCast actor = new MovieCast("unknown actor", "unknown role");
      cast.add(0, actor);
    }
  }

  /**
   * The Class SwingAction_5.
   */
  private class SwingAction_5 extends AbstractAction {

    /**
     * Instantiates a new swing action_5.
     */
    public SwingAction_5() {
      putValue(SHORT_DESCRIPTION, "Remove actor");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      row = tableActors.convertRowIndexToModel(row);
      cast.remove(row);
    }
  }

  private class SwingAction_2 extends AbstractAction {
    public SwingAction_2() {
      // putValue(NAME, "SwingAction_2");
      putValue(SHORT_DESCRIPTION, "Add genre");
    }

    public void actionPerformed(ActionEvent e) {
      Genres newGenre = (Genres) cbGenres.getSelectedItem();
      // add genre if it is not already in the list
      if (!genres.contains(newGenre)) {
        genres.add(newGenre);
      }
    }
  }

  private class SwingAction_3 extends AbstractAction {
    public SwingAction_3() {
      // putValue(NAME, "SwingAction_3");
      putValue(SHORT_DESCRIPTION, "Remove genre");
    }

    public void actionPerformed(ActionEvent e) {
      Genres newGenre = (Genres) listGenres.getSelectedValue();
      // remove genre
      if (newGenre != null) {
        genres.remove(newGenre);
      }
    }
  }

  protected void initDataBindings() {
    JTableBinding<MovieCast, List<MovieCast>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, cast, tableActors);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty).setColumnName("Name");
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1).setColumnName("Role");
    //
    jTableBinding.bind();
    //
    JListBinding<Genres, List<Genres>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    jListBinding.bind();
    //
    JTableBinding<MovieCertification, List<MovieCertification>, JTable> jTableBinding_1 = SwingBindings
        .createJTableBinding(UpdateStrategy.READ, certifications, tableCertification);
    //
    BeanProperty<MovieCertification, String> movieCertificationBeanProperty = BeanProperty.create("country");
    jTableBinding_1.addColumnBinding(movieCertificationBeanProperty).setColumnName("Country");
    //
    BeanProperty<MovieCertification, String> movieCertificationBeanProperty_1 = BeanProperty.create("certification");
    jTableBinding_1.addColumnBinding(movieCertificationBeanProperty_1).setColumnName("Certification");
    //
    jTableBinding_1.bind();
  }
}
