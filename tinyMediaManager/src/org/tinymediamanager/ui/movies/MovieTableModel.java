package org.tinymediamanager.ui.movies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;

public class MovieTableModel extends AbstractTableModel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE    = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  /** The Constant checkIcon. */
  private final static ImageIcon      checkIcon = new ImageIcon(MainWindow.class.getResource("images/Checkmark.png"));

  /** The Constant crossIcon. */
  private final static ImageIcon      crossIcon = new ImageIcon(MainWindow.class.getResource("images/Cross.png"));

  private MovieList                   movieList = MovieList.getInstance();

  private final List<Movie>           movies;

  private final List<Movie>           filteredMovies;

  private Comparator<Movie>           comparator;

  public MovieTableModel() {
    movies = movieList.getMovies();
    filteredMovies = new ArrayList<Movie>(movies);
    comparator = new MovieExtendedComparator(SortColumn.TITLE, true);
  }

  @Override
  public int getColumnCount() {
    return 5;
  }

  @Override
  public int getRowCount() {
    return filteredMovies.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return BUNDLE.getString("metatag.title"); //$NON-NLS-1$

      case 1:
        return BUNDLE.getString("metatag.year"); //$NON-NLS-1$

      case 2:
        return BUNDLE.getString("metatag.nfo"); //$NON-NLS-1$

      case 3:
        return BUNDLE.getString("metatag.images"); //$NON-NLS-1$

      case 4:
        return BUNDLE.getString("metatag.trailer"); //$NON-NLS-1$
    }

    throw new IllegalStateException();
  }

  @Override
  public Object getValueAt(int rowIndex, int column) {
    Movie movie = filteredMovies.get(rowIndex);
    switch (column) {
      case 0:
        return movie.getTitleSortable();

      case 1:
        return movie.getYear();

      case 2:
        if (movie.getHasNfoFile()) {
          return checkIcon;
        }
        return crossIcon;

      case 3:
        if (movie.getHasImages()) {
          return checkIcon;
        }
        return crossIcon;

      case 4:
        if (movie.getHasTrailer()) {
          return checkIcon;
        }
        return crossIcon;
    }

    throw new IllegalStateException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
      case 1:
        return String.class;

      case 2:
      case 3:
      case 4:
        return ImageIcon.class;
    }

    throw new IllegalStateException();
  }

  /**
   * Sort movies.
   * 
   * @param column
   *          the column
   * @param ascending
   *          the ascending
   */
  public void sortMovies(MovieExtendedComparator.SortColumn column, boolean ascending) {
    comparator = new MovieExtendedComparator(column, ascending);
    sort();
    fireTableDataChanged();
  }

  private void sort() {
    Collections.sort(filteredMovies, comparator);
  }

  public List<Movie> getFilteredMovies() {
    return this.filteredMovies;
  }

  /**
   * Filter movies.
   * 
   * @param filter
   *          the filter
   */
  public void filterMovies(HashMap<MovieMatcher.SearchOptions, Object> filter) {
    MovieMatcher matcher = new MovieMatcher(filter);
    filteredMovies.clear();
    for (int i = 0; i < movies.size(); i++) {
      if (matcher.matches(movies.get(i))) {
        filteredMovies.add(movies.get(i));
      }
    }
    sort();
    fireTableDataChanged();
  }

}
