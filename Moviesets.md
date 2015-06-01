# Introduction #

Here you will find a short description how moviesets work in tinyMediaManager.


# Details #

A movieset is a collection of movies. For example all Harry Potter movies belong to the **Harry Potter Collection**.
In tinyMediaManger you will have the possibility to group your movies into moviesets, attach metadata to it (these won't be available to XBMC) and order them the right way, so XBMC or MediaPortal will show them in the right order.

![![](http://tinymediamanager.googlecode.com/svn/wiki/images/movieset_panel_linux_thumb.jpg)](http://tinymediamanager.googlecode.com/svn/wiki/images/movieset_panel_linux.jpg)


## Assigning a movie to a movieset ##

You can simply add a new movieset by clicking the "add" button in the movieset panel and inserting a name for it. After you added a new movieset, you have two options to add movies to a movieset.

The first one is to scrape metadata for this movieset (the metadata is coming from TMDB) by clicking the "search" button. This will open a new window where you can see the search result. After selecting a movieset from the search results tinyMediaManager will download the metadata and try to find movies from your database, which are in this movieset (matching is done by the TMDB id and IMDB id).

![![](http://tinymediamanager.googlecode.com/svn/wiki/images/search_movieset_linux_thumb.jpg)](http://tinymediamanager.googlecode.com/svn/wiki/images/search_movieset_linux.jpg)


With the option **Assign movies to this movieset** tinyMediaManager will automatically assign all found movies to this movieset.

Option two is to manually assign movies to a movieset. This can be done in the movie edit window. There is a new option called movieset where you have a dropdown with an empty movieset (to unassign) and all moviesets. Just choose the movieset and this movie is assigned to the movieset.

![![](http://tinymediamanager.googlecode.com/svn/wiki/images/edit_movie_linux_thumb.jpg)](http://tinymediamanager.googlecode.com/svn/wiki/images/edit_movie_linux.jpg)


## Removing a movie from a movieset ##

As described before, you can choose the empty movieset in the movie edit window to unassign a movie from a movieset.
Alternatively you also can use the "remove" button in the movieset view when a movie is selected. This will also unassign the movie from a movieset.
In the movieset edit window, you also have the chance to unassign a movie from a movieset.

## Editing a movieset ##

With the "edit" button, you can also manually edit a movieset (change title, add a overview, reorder movies or unassign movies)