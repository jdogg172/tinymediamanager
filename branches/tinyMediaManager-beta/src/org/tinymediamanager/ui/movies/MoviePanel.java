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
package org.tinymediamanager.ui.movies;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;
import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieRenameTask;
import org.tinymediamanager.core.movie.MovieScrapeTask;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.MovieUpdateDatasourceTask;
import org.tinymediamanager.ui.BorderCellRenderer;
import org.tinymediamanager.ui.IconRenderer;
import org.tinymediamanager.ui.JSearchTextField;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.MyTable;
import org.tinymediamanager.ui.TmmSwingWorker;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MoviePanel.
 */
public class MoviePanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long      serialVersionUID             = 1L;

  /** The logger. */
  private final static Logger    LOGGER                       = Logger.getLogger(MoviePanel.class);

  /** The movie list. */
  private MovieList              movieList;

  /** The text field. */
  private JTextField             textField;

  /** The table. */
  private JTable                 table;

  /** The action update data sources. */
  private final Action           actionUpdateDataSources      = new UpdateDataSourcesAction(false);

  /** The action update data sources. */
  private final Action           actionUpdateDataSources2     = new UpdateDataSourcesAction(true);

  /** The action scrape. */
  private final Action           actionScrape                 = new SingleScrapeAction(false);

  /** The action scrape. */
  private final Action           actionScrape2                = new SingleScrapeAction(true);

  /** The action edit movie. */
  private final Action           actionEditMovie              = new EditAction(false);

  /** The action edit movie. */
  private final Action           actionEditMovie2             = new EditAction(true);

  /** The action scrape unscraped movies. */
  private final Action           actionScrapeUnscraped        = new UnscrapedScrapeAction();

  /** The action scrape selected movies. */
  private final Action           actionScrapeSelected         = new SelectedScrapeAction();

  /** The action scrape metadata selected. */
  private final Action           actionScrapeMetadataSelected = new SelectedScrapeMetadataAction();

  /** The action rename. */
  private final Action           actionRename                 = new RenameAction(false);

  /** The action rename2. */
  private final Action           actionRename2                = new RenameAction(true);

  /** The action remove2. */
  private final Action           actionRemove2                = new RemoveAction(true);

  /** The action export. */
  private final Action           actionExport                 = new ExportAction(true);

  /** The panel movie count. */
  private JPanel                 panelMovieCount;

  /** The lbl movie count. */
  private JLabel                 lblMovieCount;

  /** The lbl movie count int. */
  private JLabel                 lblMovieCountTotal;

  /** The btn ren. */
  private JButton                btnRen;

  /** The menu. */
  private JMenu                  menu;

  /** The movie table model. */
  private EventTableModel<Movie> movieTableModel;

  /** The movie selection model. */
  private MovieSelectionModel    movieSelectionModel;

  /** The sorted movies. */
  private SortedList<Movie>      sortedMovies;

  /** The text filtered movies. */
  private FilterList<Movie>      textFilteredMovies;

  /** The panel extended search. */
  private JPanel                 panelExtendedSearch;

  /** The lbl movie count of. */
  private JLabel                 lblMovieCountOf;

  /** The lbl movie count filtered. */
  private JLabel                 lblMovieCountFiltered;

  /** The split pane horizontal. */
  private JSplitPane             splitPaneHorizontal;

  /** The panel right. */
  private MovieInformationPanel  panelRight;

  /** The btn media information. */
  private JButton                btnMediaInformation;
  // private final Action action = new SwingAction();
  /** The action media information. */
  private final Action           actionMediaInformation       = new MediaInformationAction(false);

  /** The action media information2. */
  private final Action           actionMediaInformation2      = new MediaInformationAction(true);

  /** The action batch edit. */
  private final Action           actionBatchEdit              = new BatchEditAction();

  // /** The window config. */
  // private WindowConfig windowConfig;

  /**
   * Create the panel.
   */
  public MoviePanel() {
    super();
    // load movielist
    LOGGER.debug("loading MovieList");
    movieList = MovieList.getInstance();
    sortedMovies = new SortedList<Movie>(movieList.getMovies(), new MovieComparator());
    sortedMovies.setMode(SortedList.AVOID_MOVING_ELEMENTS);
    // movieSelectionModel = new MovieSelectionModel(sortedMovies);
    // windowConfig = Globals.settings.getWindowConfig();

    // build menu
    menu = new JMenu("Movies");
    JFrame mainFrame = MainWindow.getFrame();
    JMenuBar menuBar = mainFrame.getJMenuBar();
    menuBar.add(menu);

    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieList = new JPanel();
    splitPaneHorizontal.setLeftComponent(panelMovieList);
    panelMovieList
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow"),
            ColumnSpec.decode("150px:grow"), }, new RowSpec[] { RowSpec.decode("26px"), FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("fill:max(200px;default):grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelMovieList.add(toolBar, "2, 1, left, fill");

    JButton buttonUpdateDataSources = toolBar.add(actionUpdateDataSources);
    JSplitButton buttonScrape = new JSplitButton(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    // temp fix for size of the button
    buttonScrape.setText("   ");
    buttonScrape.setHorizontalAlignment(JButton.LEFT);
    // buttonScrape.setMargin(new Insets(2, 2, 2, 24));
    buttonScrape.setSplitWidth(18);

    // register for listener
    buttonScrape.addSplitButtonActionListener(new SplitButtonActionListener() {
      public void buttonClicked(ActionEvent e) {
        actionScrape.actionPerformed(e);
      }

      public void splitButtonClicked(ActionEvent e) {
      }
    });

    JPopupMenu popup = new JPopupMenu("popup");
    JMenuItem item = new JMenuItem(actionScrape2);
    popup.add(item);
    item = new JMenuItem(actionScrapeUnscraped);
    popup.add(item);
    item = new JMenuItem(actionScrapeSelected);
    popup.add(item);
    buttonScrape.setPopupMenu(popup);
    toolBar.add(buttonScrape);

    JButton buttonEdit = toolBar.add(actionEditMovie);

    btnRen = new JButton("REN");
    btnRen.setAction(actionRename);
    toolBar.add(btnRen);

    btnMediaInformation = new JButton("MI");
    btnMediaInformation.setAction(actionMediaInformation);
    toolBar.add(btnMediaInformation);

    // textField = new JTextField();
    textField = new JSearchTextField();
    panelMovieList.add(textField, "3, 1, right, bottom");
    textField.setColumns(10);

    // table = new JTable();
    // build JTable

    MatcherEditor<Movie> textMatcherEditor = new TextComponentMatcherEditor<Movie>(textField, new MovieFilterator());
    MovieMatcherEditor movieMatcherEditor = new MovieMatcherEditor();
    FilterList<Movie> extendedFilteredMovies = new FilterList<Movie>(sortedMovies, movieMatcherEditor);
    textFilteredMovies = new FilterList<Movie>(extendedFilteredMovies, textMatcherEditor);
    movieSelectionModel = new MovieSelectionModel(sortedMovies, textFilteredMovies, movieMatcherEditor);
    movieTableModel = new EventTableModel<Movie>(textFilteredMovies, new MovieTableFormat());
    table = new MyTable(movieTableModel);

    movieTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
        // select first movie if nothing is selected
        ListSelectionModel selectionModel = table.getSelectionModel();
        if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
          selectionModel.setSelectionInterval(0, 0);
        }
      }
    });

    // install and save the comparator on the Table
    movieSelectionModel.setTableComparatorChooser(TableComparatorChooser.install(table, sortedMovies, TableComparatorChooser.SINGLE_COLUMN));

    // table = new MyTable();
    table.setFont(new Font("Dialog", Font.PLAIN, 11));
    // scrollPane.setViewportView(table);

    // JScrollPane scrollPane = new JScrollPane(table);
    JScrollPane scrollPane = MyTable.createStripedJScrollPane(table);
    panelMovieList.add(scrollPane, "2, 3, 2, 1, fill, fill");

    panelExtendedSearch = new MovieExtendedSearchPanel(movieSelectionModel);
    panelMovieList.add(panelExtendedSearch, "2, 5, 2, 1, fill, fill");

    JPanel panelStatus = new JPanel();
    panelMovieList.add(panelStatus, "2, 6, 2, 1");
    panelStatus.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("1px"),
        ColumnSpec.decode("146px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec
        .decode("fill:default:grow"), }));

    panelMovieCount = new JPanel();
    panelStatus.add(panelMovieCount, "3, 1, left, fill");

    lblMovieCount = new JLabel("Movies:");
    panelMovieCount.add(lblMovieCount);

    lblMovieCountFiltered = new JLabel("");
    panelMovieCount.add(lblMovieCountFiltered);

    lblMovieCountOf = new JLabel("of");
    panelMovieCount.add(lblMovieCountOf);

    lblMovieCountTotal = new JLabel("");
    panelMovieCount.add(lblMovieCountTotal);

    panelRight = new MovieInformationPanel(movieSelectionModel);
    splitPaneHorizontal.setRightComponent(panelRight);
    splitPaneHorizontal.setContinuousLayout(true);

    // beansbinding init
    initDataBindings();

    // menu items
    menu.add(actionUpdateDataSources2);
    menu.addSeparator();
    menu.add(actionScrape2);
    menu.add(actionScrapeSelected);
    menu.add(actionScrapeUnscraped);
    menu.add(actionScrapeMetadataSelected);
    menu.addSeparator();
    menu.add(actionEditMovie2);
    menu.add(actionBatchEdit);
    menu.add(actionRename2);
    menu.add(actionMediaInformation2);
    menu.add(actionExport);
    menu.addSeparator();
    menu.add(actionRemove2);

    // popup menu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionScrape2);
    popupMenu.add(actionScrapeSelected);
    popupMenu.add(actionScrapeMetadataSelected);
    popupMenu.addSeparator();
    popupMenu.add(actionEditMovie2);
    popupMenu.add(actionBatchEdit);
    popupMenu.add(actionRename2);
    popupMenu.add(actionMediaInformation2);
    popupMenu.add(actionExport);
    popupMenu.addSeparator();
    popupMenu.add(actionRemove2);

    MouseListener popupListener = new PopupListener(popupMenu);
    table.addMouseListener(popupListener);

    // further initializations
    init();
  }

  /**
   * further initializations.
   */
  private void init() {
    // moviename column
    table.getColumnModel().getColumn(0).setCellRenderer(new BorderCellRenderer());

    // year column
    table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);

    // NFO column
    table.getTableHeader().getColumnModel().getColumn(2).setHeaderRenderer(new IconRenderer("NFO"));
    table.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(20);
    URL imageURL = MainWindow.class.getResource("images/File.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(2).setHeaderValue(new ImageIcon(imageURL));
    }

    // Images column
    table.getTableHeader().getColumnModel().getColumn(3).setHeaderRenderer(new IconRenderer("Images"));
    table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/Image.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(3).setHeaderValue(new ImageIcon(imageURL));
    }

    // trailer column
    table.getTableHeader().getColumnModel().getColumn(4).setHeaderRenderer(new IconRenderer("Trailer"));
    table.getTableHeader().getColumnModel().getColumn(4).setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/ClapBoard.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(4).setHeaderValue(new ImageIcon(imageURL));
    }

    table.setSelectionModel(movieSelectionModel.getSelectionModel());
    // selecting first movie at startup
    if (movieList.getMovies() != null && movieList.getMovies().size() > 0) {
      ListSelectionModel selectionModel = table.getSelectionModel();
      if (selectionModel.isSelectionEmpty()) {
        selectionModel.setSelectionInterval(0, 0);
      }
    }

    panelRight.init();

    // initialize filteredCount
    lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
  }

  /**
   * The Class UpdateDataSourcesAction.
   */
  private class UpdateDataSourcesAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UpdateDataSourcesAction.
     * 
     * @param withTitle
     *          the with title
     */
    public UpdateDataSourcesAction(boolean withTitle) {
      // putValue(NAME, "UDS");
      if (withTitle) {
        putValue(NAME, "Update data sources");
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
        putValue(SHORT_DESCRIPTION, "Update data sources");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      TmmSwingWorker task = new MovieUpdateDatasourceTask();
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, "Only one operation at one time is allowed");
      }
    }
  }

  /**
   * The Class SingleScrapeAction.
   */
  private class SingleScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new SingleScrapeAction.
     * 
     * @param withTitle
     *          the with title
     */
    public SingleScrapeAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, "Search & scrape selected movies");
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
        putValue(SHORT_DESCRIPTION, "Search & scrape movie");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // MovieChooser dialogMovieChooser = new MovieChooser(movie);
      // dialogMovieChooser.pack();
      // dialogMovieChooser.setVisible(true);
      // }
      List<Movie> selectedMovies = new ArrayList<Movie>();
      // save all selected movies in an extra list (maybe scraping of one movie
      // changes the whole list)
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }
      for (Movie movie : selectedMovies) {
        MovieChooser dialogMovieChooser = new MovieChooser(movie, selectedMovies.size() > 1 ? true : false);
        // dialogMovieChooser.pack();
        // dialogMovieChooser.setVisible(true);
        if (!dialogMovieChooser.showDialog()) {
          break;
        }
      }
    }
  }

  /**
   * The Class UnscrapedScrapeAction.
   */
  private class UnscrapedScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UnscrapedScrapeAction.
     */
    public UnscrapedScrapeAction() {
      putValue(NAME, "Search & scrape unscraped movies - force best match");
      putValue(SHORT_DESCRIPTION, "Search & scrape all unscraped movies");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<Movie> unscrapedMovies = movieList.getUnscrapedMovies();
      if (unscrapedMovies.size() > 0) {
        MovieScrapeMetadata dialog = new MovieScrapeMetadata("Search & scrape unscraped movies - force best match");
        dialog.setVisible(true);
        // get options from dialog
        MovieSearchAndScrapeOptions options = dialog.getMovieSearchAndScrapeConfig();
        // do we want to scrape?
        if (dialog.shouldStartScrape()) {
          // scrape
          TmmSwingWorker scrapeTask = new MovieScrapeTask(unscrapedMovies, true, options);
          if (!MainWindow.executeMainTask(scrapeTask)) {
            // inform that only one task at a time can be executed
            JOptionPane.showMessageDialog(null, "Only one operation at one time is allowed");
          }
        }
        dialog.dispose();
      }
    }
  }

  /**
   * The Class UnscrapedScrapeAction.
   */
  private class SelectedScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UnscrapedScrapeAction.
     */
    public SelectedScrapeAction() {
      putValue(NAME, "Search & scrape selected movies - force best match");
      putValue(SHORT_DESCRIPTION, "Search & scrape all selected movies");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // List<Movie> selectedMovies = new ArrayList<Movie>();
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // selectedMovies.add(movieList.getMovies().get(row));
      // }
      List<Movie> selectedMovies = new ArrayList<Movie>();
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }

      if (selectedMovies.size() > 0) {
        // scrapeTask = new ScrapeTask(selectedMovies);
        MovieScrapeMetadata dialog = new MovieScrapeMetadata("Search & scrape selected movies - force best match");
        dialog.setVisible(true);
        // get options from dialog
        MovieSearchAndScrapeOptions options = dialog.getMovieSearchAndScrapeConfig();
        // do we want to scrape?
        if (dialog.shouldStartScrape()) {
          // scrape
          TmmSwingWorker scrapeTask = new MovieScrapeTask(selectedMovies, true, options);
          if (!MainWindow.executeMainTask(scrapeTask)) {
            JOptionPane.showMessageDialog(null, "Only one operation at one time is allowed");
          }
        }
        dialog.dispose();
      }
    }
  }

  /**
   * The Class SelectedScrapeMetadataAction.
   */
  private class SelectedScrapeMetadataAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UnscrapedScrapeAction.
     */
    public SelectedScrapeMetadataAction() {
      putValue(NAME, "Scrape metadata for selected movies");
      putValue(SHORT_DESCRIPTION, "Scrape metadata for selected movies");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<Movie> selectedMovies = new ArrayList<Movie>();
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }

      if (selectedMovies.size() > 0) {
        MovieScrapeMetadata dialog = new MovieScrapeMetadata("Scrape metadata for selected movies");
        dialog.setVisible(true);
        // get options from dialog
        MovieSearchAndScrapeOptions options = dialog.getMovieSearchAndScrapeConfig();
        // do we want to scrape?
        if (dialog.shouldStartScrape()) {
          // scrape
          TmmSwingWorker scrapeTask = new MovieScrapeTask(selectedMovies, false, options);
          if (!MainWindow.executeMainTask(scrapeTask)) {
            JOptionPane.showMessageDialog(null, "Only one operation at one time is allowed");
          }
        }
        dialog.dispose();
      }
    }
  }

  /**
   * The Class EditAction.
   */
  private class EditAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EditAction.
     * 
     * @param withTitle
     *          the with title
     */
    public EditAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Edit movie");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, "Edit movie");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // MovieEditor dialogMovieEditor = new MovieEditor(movie);
      // // dialogMovieEditor.pack();
      // dialogMovieEditor.setVisible(true);
      // }
      List<Movie> selectedMovies = new ArrayList<Movie>();
      // save all selected movies in an extra list (maybe scraping of one movie
      // changes the whole list)
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }
      for (Movie movie : selectedMovies) {
        MovieEditor dialogMovieEditor = new MovieEditor(movie, selectedMovies.size() > 1 ? true : false);
        // dialogMovieEditor.setVisible(true);
        if (!dialogMovieEditor.showDialog()) {
          break;
        }
      }
    }
  }

  /**
   * The Class RemoveAction.
   */
  private class RemoveAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new RemoveAction.
     * 
     * @param withTitle
     *          the with title
     */
    public RemoveAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Remove selected movies");
      }
      else {
        // putValue(LARGE_ICON_KEY, new
        // ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, "Remove selected movies");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      // List<Movie> movies = new ArrayList<Movie>();

      // // get seletected movies
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // movies.add(movie);
      // }

      List<Movie> movies = movieSelectionModel.getSelectedMovies();

      // remove selected movies
      if (movies.size() > 0) {
        for (int i = 0; i < movies.size(); i++) {
          movieList.removeMovie(movies.get(i));
        }
      }
    }

  }

  /**
   * The Class ExportAction.
   */
  private class ExportAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new RemoveAction.
     * 
     * @param withTitle
     *          the with title
     */
    public ExportAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Export selected movies");
      }
      else {
        // putValue(LARGE_ICON_KEY, new
        // ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, "Export selected movies");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      List<Movie> movies = new ArrayList<Movie>(movieSelectionModel.getSelectedMovies());

      // export selected movies
      if (movies.size() > 0) {
        MovieExporterDialog dialog = new MovieExporterDialog(movies);
        dialog.setVisible(true);
      }
    }
  }

  /**
   * The Class RenameAction.
   */
  private class RenameAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new rename action.
     * 
     * @param withTitle
     *          the with title
     */
    public RenameAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Rename selected movies");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/rename-icon.png")));
        putValue(SHORT_DESCRIPTION, "rename selected movies");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<Movie> selectedMovies = new ArrayList<Movie>(movieSelectionModel.getSelectedMovies());

      // rename
      TmmSwingWorker renameTask = new MovieRenameTask(selectedMovies);
      if (!MainWindow.executeMainTask(renameTask)) {
        JOptionPane.showMessageDialog(null, "Only one operation at one time is allowed");
      }
    }
  }

  /**
   * The listener interface for receiving popup events. The class that is
   * interested in processing a popup event implements this interface, and the
   * object created with that class is registered with a component using the
   * component's <code>addPopupListener<code> method. When
   * the popup event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see PopupEvent
   */
  private class PopupListener extends MouseAdapter {

    /** The popup. */
    private JPopupMenu popup;

    /**
     * Instantiates a new popup listener.
     * 
     * @param popupMenu
     *          the popup menu
     */
    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
      // if (table.getSelectedRow() != -1) {
      maybeShowPopup(e);
      // }
    }

    /**
     * Maybe show popup.
     * 
     * @param e
     *          the e
     */
    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        boolean selected = false;
        // check the selected rows
        int row = table.rowAtPoint(e.getPoint());
        int[] selectedRows = table.getSelectedRows();
        for (int selectedRow : selectedRows) {
          if (selectedRow == row) {
            selected = true;
          }
        }

        // if the row, which has been right clicked is not selected - select it
        if (!selected) {
          table.getSelectionModel().setSelectionInterval(row, row);
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  /**
   * Gets the split pane horizontal.
   * 
   * @return the split pane horizontal
   */
  public JSplitPane getSplitPaneHorizontal() {
    return splitPaneHorizontal;
  }

  /**
   * Gets the split pane vertical.
   * 
   * @return the split pane vertical
   */
  public JSplitPane getSplitPaneVertical() {
    return panelRight.getSplitPaneVertical();
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    //
    BeanProperty<MovieList, Integer> movieListBeanProperty = BeanProperty.create("movieCount");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ, movieList,
        movieListBeanProperty, lblMovieCountTotal, jLabelBeanProperty);
    autoBinding_20.bind();
    //
  }

  // private class SwingAction extends AbstractAction {
  // public SwingAction() {
  // putValue(NAME, "SwingAction");
  // putValue(SHORT_DESCRIPTION, "Some short description");
  // }
  //
  // public void actionPerformed(ActionEvent e) {
  // }
  // }

  /**
   * The Class MediaInformationAction.
   */
  private class MediaInformationAction extends AbstractAction {

    /**
     * Instantiates a new media information action.
     */
    public MediaInformationAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, "Update media information of selected movies");
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        // putValue(NAME, "MI");
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
        putValue(SHORT_DESCRIPTION, "Update media information of selected movies");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      List<Movie> selectedMovies = new ArrayList<Movie>();
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }

      // get data of all files within all selected movies
      if (selectedMovies.size() > 0) {
        for (Movie movie : selectedMovies) {
          for (MediaFile file : movie.getMediaFiles()) {
            file.gatherMediaInformation();
            file.saveToDb();
          }
        }
      }
    }
  }

  /**
   * The Class BatchEditAction.
   */
  private class BatchEditAction extends AbstractAction {

    /**
     * Instantiates a new batch edit action.
     */
    public BatchEditAction() {
      putValue(NAME, "Bulk editing");
      putValue(SHORT_DESCRIPTION, "Change genres, tags, movieset and watched flag for all selected movies");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<Movie> selectedMovies = new ArrayList<Movie>();
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }

      // get data of all files within all selected movies
      if (selectedMovies.size() > 0) {
        MovieBatchEditor editor = new MovieBatchEditor(selectedMovies);
        editor.setVisible(true);
      }

    }
  }
}