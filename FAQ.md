## How does tinyMediaManager find my movies ##
tinyMediaManager is constructed to search for movies in separate directories. One movie per directory! It scans the chosen directories recursively until it finds a moviefile in a directory (recognized filetypes are specified in config.xml). The directory name will be taken as the temporary movie name.

## tinyMediaManager crashes at startup or the information is wrong ##
Try to initialize the data tinyMediaManager uses:
First delete tmm.odb (database where all data is stored). If that didn't work also try to delete config.xml

## tinyMediaManager crashes as soon as I click on a moviename ##
If you are using Java 7 with a release lower than u10, then tinyMediaManager will crash because of a bug in java. Its fixed in Java 7u10.
Just wait until that patch is available or switch to Java6

## not all movies are found when updating a datasource ##
Be sure that your file and directory structure meets the requirement of tinyMediaManager. Only one movie per directory is allowed (the directory name containing the movie file will be taken as the initial movie name):

  * Datasource
    * Movie A
      * A.avi
    * Movie B
      * B.mkv

also nesting of directories is allowed:

  * Datasource
    * Folder A
      * Movie A1
        * A1.avi
      * Movie A2
        * A2.mkv
    * Folder B
      * Movie B1
        * B1.mpg

the following file extensions will be recognized as movie files:
  * avi
  * mkv
  * mp4
  * mpg
  * wmv
  * ts
  * mpeg
  * divx
  * h264
  * mov

if you want to add more file extenstions, simply add them in the config.xml as follows:

```
<videoFileTypes>
  <filetype>.mpg</filetype>
  <filetype>.avi</filetype>
  <filetype>.mp4</filetype>
  <filetype>.mkv</filetype>
  <filetype>.wmv</filetype>
  <filetype>.ts</filetype>
  <filetype>.mpeg</filetype>
  <filetype>.divx</filetype>
  <filetype>.h264</filetype>
  <filetype>.mov</filetype>
</videoFileTypes>
```