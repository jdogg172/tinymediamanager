# Introduction #
**tinyMediaManager** uses the **[Java Minimal Template Engine](http://jmte.googlecode.com/svn/trunk/doc/index.html)** (JMTE) to construct the exported page.

If you want to edit or create a new template make a copy of the template folder you would like to build upon. Do not edit the default templates; the default templates will be overwritten each time you start tMM and you will lose your changes.

If you create a nice template [contact tMM](http://www.tinymediamanager.org/index.php/contact-us/) and we will check it out for distribution with the program.

# Required Files #
Templates rely on three files to export successfully. All other files you create will also be exported, retaining their directory structure, when the page is built by tMM; this allows for the inclusion of stylesheets, images and scripts.
| **File** | **Description** |
|:---------|:----------------|
| template.conf | _This configuration file tells tMM where to find the other two required files._ |
| list.jmte | _This may be renamed as long as you also reflect the change in template.conf._ |
| detail.jmte | _This may be renamed as long as you also reflect the change in template.conf. detail.jmte is required only if you want tMM to build individual `<movie>`.html files for inclusion into index.html either through an .ajax() call or iframe._ |

# Configuration #
Each template must be in its own directory and include a **template.conf** file. The contents of **template.conf** must include:
| **Line** | **Description** |
|:---------|:----------------|
| name=<name of template> | _The name that will display to the user when exporting through the UI._ |
| type={movie} | _Currently only movie templates are supported._ |
| list=<path to list template> | _(default: list.jmte) This is the template which will be used to build index.html or movielist.xml/csv._ |
| detail=<path to detail template> | _(default: detail.jmte) Remove this line if you do not require individual `<movie>`.html pages._ |
| extension={html|xml|csv} | _(default: html) This is the format tMM will export._ |
| description=`<text>` | Write a short description that will print in the tMM exporter UI. Newlines (\n) should be used to insert paragraph breaks.|
| url=<url to homepage> | _The URL to the page that hosts this template or to the author's homepage. Remove this line if you have neither._ |

Using the above information write your template.conf file. It may resemble this example:
```

name=Jelly is Delicious
type=movie
list=list.jmte
detail=detail.jmte
extension=html
description=Created by William Shatner\n\nThis template has jelly in its gears.
url=https://github.com/TheShatner/jelly_template
```

# Template Code #
list.jmte and detail.jmte are HTML pages. The JMTE syntax is used to insert variables like movie name, cast, genre and file information. All of the variables are stored in the list array **movies**. To access each movies' variables you must itterate over the entire list array.

In the following code the list array **movies** is itterated over. For each movie entry we assign the variable **movie** to hold its details and append the name of a variable to print individual attributes.
```html

<div class="movie details">
${foreach movies movie}
<span class="bold">Title</span>: ${movie.name}
<span class="bold">Year</span>: ${movie.year}
${end}
</div>
```
As you can see, the **name** variable in ${movie.name} tells JMTE to print the name of the movie. The variable **name** is a string, but some movie variables are also list arrays. Print the list array genres with the following code:
```html

${foreach movies movie}
${movie.name}
<span class="genreList">
${foreach movie.genres genre , }       // " , " comma is used here as genre seperator
${genre}
${end}
</span>
${end}
```
In this example we itterated over the movies list array like in the previous example. Then, from within the first foreach loop, we itterated over the genres list array and printed them. We told JMTE to separate each entry with a comma by putting a comma at the end of the foreach instance.

# Possible Variables #
Paste the following code into list.jmte then export a single movie with that template. Use the output as reference to find values you would like to use and how to obtain them.
```html

<!DOCTYPE html>
<head><style type="text/css">body, h1 { margin:0; padding: 0; } h2 { margin-left: 2em; }</style></head>
<body>
<h1>Movie</h1>
<pre>
${foreach movies movie}
sortTitle=${movie.sortTitle}
tagline=${movie.tagline}
votes=${movie.votes}
runtime=${movie.runtime}
director=${movie.director}
writer=${movie.writer}
dataSource=${movie.dataSource}
watched=false
movieSet=${movie.movieSet}
isDisc=true/false
spokenLanguages=${movie.spokenLanguages}
subtitles=true
country=${movie.country}
releaseDate=${movie.releaseDate} [null]
${foreach movie.genres genre ,}
${genre}
${end}
${foreach movie.tags tag}
${tag}
${end}
${foreach movie.extraThumbs extraThumb}
${extraThumb}
${end}
${foreach movie.extraFanarts extraFanart}
${extraFanart}
${end}
certification=${movie.certification}
titleSortable=${movie.titleSortable}
newlyAdded=true/false
id=${movie.id}
tmdbId=${movie.tmdbId}
imdbId=${movie.imdbId}
title=${movie.title}
originalTitle=${movie.originalTitle}
year=${movie.year}
plot=${movie.plot}
rating=${movie.rating}
path=${movie.path}
fanartUrl=${movie.fanartUrl}
posterUrl=${movie.posterUrl}
bannerUrl=${movie.bannerUrl}
thumbUrl=${movie.thumbUrl}
dateAdded=${movie.dateAdded}
productionCompany=${movie.productionCompany}
scraped=true/false
duplicate=true/false
runtimeFromMediaFiles=${movie.runtimeFromMediaFiles}
runtimeFromMediaFilesInMinutes=${movie.runtimeFromMediaFilesInMinutes}
</pre>

<h2>Cast</h2>
<pre>
${foreach movie.actors actor}
name=${actor.name}
character=${actor.character}
thumb=${actor.thumb}
thumbPath=${actor.thumbPath}
${end}
</pre>

<h2>Video</h2>
<pre>
${foreach movie.videoFiles video}
path=${video.path}
filename=${video.filename}
filesize=${video.filesize}
videoCodec=${video.videoCodec}
containerFormat=${video.containerFormat}
videoFormat=${video.videoFormat}
exactVideoFormat=${video.exactVideoFormat}
videoWidth=${video.videoWidth}
videoHeight=${video.videoHeight}
overallBitRate=${video.overallBitRate}
stacking=${video.path}
type=${video.path}
duration=${video.duration}
durationInMinutes=${video.durationInMinutes}
durationHM=${video.durationHM}
durationHHMMSS=${video.durationHHMMSS}
subtitles=${video.subtitles}
file=${video.file}
${end}
${end}
</pre>
</body>
</html>
```