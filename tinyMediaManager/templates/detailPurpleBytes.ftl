<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="no" lang="no">
<head><meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<style type="text/css">
 a  {color:#E9DFD3}
 body{font-family:Tahoma; font-size:8pt; cursor:default}
 div{position:absolute; border:none}
 #C1,.C2,.C3,#Title,#Pic{background-color:#DBDBDB; color:#3B3B3B; width:90px; border:solid 1px; border-color:#F0F0F0 #747474 #747474 #F0F0F0; font-weight:600; padding:1px 0px 2px 5px; z-index:8; white-space:nowrap; overflow:hidden}
 #Title{width:648px; top:10px; left:40px; border-width:2px; font-family:Arial; font-size:14pt; font-weight:500; letter-spacing:1px; text-align:center; padding:2px 10px 5px 10px}
 #Pic{top:57px; left:40px; width:96px; height:158px; border-width:2px; vertical-align:top; padding:0px}
 #C1 {top:-5px; left:-5px}
 .C2{background-color:#D0C7BC; color:#32302D; top:-4px; left:90px; height:15px; border-color:#EFE5D8 #716C66 #716C66 #D0C7BC; font-weight:500; padding:0px 0px 0px 20px; z-index:7}
 #B1,#B2{width:8px; height:8px; top:-6px; left:91px; background-color:#DBDBDB; border:solid 1px; border-color:#F0F0F0 #747474 #747474 #DBDBDB; font-size:4px; font-weight:600; text-align:center; vertical-align:middle; z-index:9}
 #B2{top:3px; left:91px; border-color:#747474 #747474 #747474 #DBDBDB}
 .C3{top:-7px; left:-7px; border-width:2px; font-weight:600; letter-spacing:1px; padding:2px 0px 4px 5px; z-index:9}
 .C4{top:15px; left:-7px; background-color:#C8C8C8; color:#000000; border:solid 1px #F0F0F0; font-family:Arial; letter-spacing:1px; padding:6px 10px 0px 10px; overflow:auto;
  scrollbar-track-color:#C8C8C8; scrollbar-darkshadow-color:#C8C8C8; scrollbar-3dlight-color:#C8C8C8; scrollbar-face-color:#DBDBDB; scrollbar-shadow-color:#595A5D; scrollbar-highlight-color:#FFFFFF; scrollbar-arrow-color:#000000}
 #SH,#SP{background-color:#5B5B57; height:17px; top:5px; left:6px}
 .R0,.R1{background-color:#D0C7BC; border:solid 1px; border-color:#716C66 #EFE5D8 #EFE5D8 #716C66; width:15px; height:12px; font-weight:0; float:left; position:relative}
 .R1{background-color:#B4ACA3}
 .S1{top:10px; width:10px; background-color:#C0C0C0}
</style>
<script type="text/javascript" defer="defer">
function NewWin(el){var w=640,h=480,l=(screen.width-w)/2,t=(screen.height-h)/2,winprops='width='+w+',height='+h+',top='+t+',left='+l+',scrollbars=yes,resizable';win=window.open(el.src,'',winprops)}function afterUpdate(){var i,x,a='',b='<div class="R1"></div>',c='<div class="R0"></div>',el=document.getElementById('Rating');x=el.innerHTML;x=x.replace('&nbsp;','');if(!isNaN(x)){x=Math.round(x);for(i=0;i<x;i++)a+=b;for(i=10;i>x;i--)a+=c;el.innerHTML=a}}
</script>
</head>
<body style="background-color:#A39F99">
 <div id="SH" style="width:670px; height:31px; top:17px; left:47px"></div>
 <div id="Title" name="Title">${movie.name?xhtml}</div>
 <div id="SP" style="width:98px; height:159px; top:64px; left:47px"></div>
 <div id="Pic" onmouseover="s=this.style;p=document.getElementById('SP').style;s.top='59px';p.top='62px';s.left='42px';p.left='45px'" onmouseout="s.top='57px';p.top='64px';s.left='40px';p.left='47px'"><img id="Cover" name="Cover" style="width:96px; border:0px; cursor:pointer" src="_MOVIE_COVER_" onclick="NewWin(this)" /></div>

 <div style="top:62px; left:157px">
  <div style="top:0px; width:345px" id="SH"><div id="C1">Original Title</div><div id="B1">.</div><div style="width:230px" id="OriginalTitle" name="OriginalTitle" class="C2">${movie.originalName?xhtml}</div></div>
  <div style="top:18px; width:335px" id="SH"><div id="C1">Director</div><div id="B1">.</div><div style="width:220px" id="Director" name="Director" class="C2">_MOVIE_DIRECTOR_</div></div>
  <div style="top:36px; width:325px" id="SH"><div id="C1">Genre</div><div id="B1">.</div><div style="width:210px" class="C2"><span id="Genre" name="Genre"><#list movie.genres as genre>${genre} </#list></span></div></div>
  <div style="top:54px; width:315px" id="SH"><div id="C1">Country</div><div id="B1">.</div><div style="width:200px" class="C2"><span id="Year" name="Year">_MOVIE_YEAR_</span>&nbsp;-&nbsp;<span id="Country" name="Country">_MOVIE_COUNTRY_</span></div></div>
  <div style="top:72px; width:305px" id="SH"><div id="C1">Public Rating</div><div id="B1">.</div><div style="width:190px" id="Rating" name="Rating" class="C2">_MOVIE_RATED_</div></div>
  <div style="top:90px; width:295px" id="SH"><div id="C1">Language</div><div id="B1">.</div><div style="width:180px" id="OriginalLanguage" name="OriginalLanguage" class="C2">_MOVIE_LANGUAGE_</div></div>
  <div style="top:108px; width:285px" id="SH"><div id="C1">MPAA Rating</div><div id="B1">.</div><div style="width:170px" id="MPAA" name="MPAA" class="C2">_MOVIE_MPAA_</div></div>
  <div style="top:126px; width:275px" id="SH"><div id="C1">Encoded Lang.</div><div id="B1">.</div><div style="width:160px" id="Language" name="Language" class="C2">_MOVIE_LANGUAGEENCODED_</div></div>
  <div style="top:144px; width:265px" id="SH"><div id="C1">Date added</div><div id="B1">.</div><div style="width:150px" id="DateInsert" name="DateInsert" class="C2">${movie.dateAdded?date}</div></div>
 </div>

 <div style="top:62px; left:525px">
  <div style="top:0px; left:0px; width:191px" id="SH"><div id="C1">Codec</div><div id="B2">.</div><div style="width:75px" id="Codec" name="Codec" class="C2">_MOVIE_CODEC_</div></div>
  <div style="top:18px; left:-10px; width:201px" id="SH"><div id="C1">BitRate</div><div id="B2">.</div><div style="width:85px" id="Bitrate" name="Bitrate" class="C2">_MOVIE_BITRATE_</div></div>
  <div style="top:36px; left:-20px; width:211px" id="SH"><div id="C1">Resolution</div><div id="B2">.</div><div style="width:95px" class="C2"><span id="Resolution" name="Resolution">_MOVIE_RESOLUTION_</span>&nbsp;@&nbsp;<span id="FPS" name="FPS">_MOVIE_FPS_</span></div></div>
  <div style="top:54px; left:-30px; width:221px" id="SH"><div id="C1">Audio Format</div><div id="B2">.</div><div style="width:105px" id="AudioCodec" name="AudioCodec" class="C2">_MOVIE_AUDIO_</div></div>
  <div style="top:72px; left:-40px; width:231px" id="SH"><div id="C1">Audio BitRate</div><div id="B2">.</div><div style="width:115px" id="AudioBitRate" name="AudioBitRate" class="C2">_MOVIE_ABITRATE_</div></div>
  <div style="top:90px; left:-50px; width:241px" id="SH"><div id="C1">Channels</div><div id="B2">.</div><div style="width:125px" class="C2"><span id="Channels" name="Channels">_MOVIE_CHANNELS_</span>&nbsp;-&nbsp;<span id="SampleRate" name="SampleRate">_MOVIE_SRATE_</span></div></div>
  <div style="top:108px; left:-60px; width:251px" id="SH"><div id="C1">N&deg; CD</div><div id="B2">.</div><div style="width:135px" class="C2"><span id="Disk" name="Disk">_MOVIE_NCD_</span>&nbsp;-&nbsp;<span id="Filesize" name="Filesize">_MOVIE_FILESIZE_</span></div></div>
  <div style="top:126px; left:-70px; width:261px" id="SH"><div id="C1">Playtime</div><div id="B2">.</div><div style="width:145px" id="Length" name="Length" class="C2">${movie.runtime}</div></div>
  <div style="top:144px; left:-80px; width:271px" id="SH"><div id="C1">Creator</div><div id="B2">.</div><div style="width:155px" id="Ripped" name="Ripped" class="C2">_MOVIE_RIPPER_</div></div>
 </div>

 <div id="SH" style="top:239px; left:47px; width:271px; height:130px">
  <div class="C3" style="width:264px">Starring</div>
  <div class="C4" style="width:250px; height:102px"></div>
  <div style="top:84px; left:-6px; width:270px; height:40px"><div class="S1" style="height:30px"></div><div class="S1" style="top:0px; left:11px; height:40px"></div><div class="S1" style="top:20px; left:22px; width:20px; height:20px"></div><div class="S1" style="left:43px; height:30px"></div><div class="S1" style="top:0px; left:54px; height:40px"></div><div class="S1" style="top:15px; left:65px; height:25px"></div><div class="S1" style="top:25px; left:76px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:97px; height:40px"></div><div class="S1" style="left:108px; height:30px"></div><div class="S1" style="top:20px; left:119px; height:20px"></div><div class="S1" style="left:130px; width:20px; height:30px"></div><div class="S1" style="top:0px; left:151px; height:40px"></div><div class="S1" style="left:162px; height:30px"></div><div class="S1" style="top:20px; left:173px; width:20px; height:20px"></div><div class="S1" style="left:194px; height:30px"></div><div class="S1" style="top:0px; left:205px; height:40px"></div><div class="S1" style="top:15px; left:216px; height:25px"></div><div class="S1" style="top:25px; left:227px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:248px; height:40px"></div><div class="S1" style="top:15px; left:259px; height:25px"></div></div>
  <div class="C4" style="width:250px; height:102px; background:none" id="Actors" name="Actors"><#list movie.cast as c>${c.name} - ${c.character}<br/></#list></div>
 </div>

 <div id="SH" style="top:239px; left:339px; width:378px; height:130px">
  <div class="C3" style="width:371px">Notes</div>
  <div class="C4" style="width:357px; height:102px"></div>
  <div style="top:84px; left:-6px; width:376px; height:40px"><div class="S1" style="height:30px"></div><div class="S1" style="top:0px; left:11px; height:40px"></div><div class="S1" style="top:20px; left:22px; width:20px; height:20px"></div><div class="S1" style="left:43px; height:30px"></div><div class="S1" style="top:0px; left:54px; height:40px"></div><div class="S1" style="top:15px; left:65px; height:25px"></div><div class="S1" style="top:25px; left:76px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:97px; height:40px"></div><div class="S1" style="left:108px; height:30px"></div><div class="S1" style="top:20px; left:119px; height:20px"></div><div class="S1" style="left:130px; width:20px; height:30px"></div><div class="S1" style="top:0px; left:151px; height:40px"></div><div class="S1" style="left:162px; height:30px"></div><div class="S1" style="top:20px; left:173px; width:20px; height:20px"></div><div class="S1" style="left:194px; height:30px"></div><div class="S1" style="top:0px; left:205px; height:40px"></div><div class="S1" style="top:15px; left:216px; height:25px"></div><div class="S1" style="top:25px; left:227px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:248px; height:40px"></div><div class="S1" style="left:259px; height:30px"></div><div class="S1" style="top:20px; left:270px; height:20px"></div><div class="S1" style="top:15px; left:281px; width:20px; height:25px"></div><div class="S1" style="top:0px; left:302px; height:40px"></div><div class="S1" style="top:10px; left:313px; height:30px"></div><div class="S1" style="top:0px; left:324px; height:40px"></div><div class="S1" style="top:20px; left:335px; width:20px; height:20px"></div><div class="S1" style="left:356px; height:30px"></div><div class="S1" style="top:0px; left:367px; height:40px"></div></div>
  <div class="C4" style="width:357px; height:102px; background:none" id="Notes" name="Notes">_MOVIE_NOTES_</div>
 </div>

 <div id="SH" style="top:385px; left:47px; width:670px; height:199px">
  <div class="C3" style="width:663px">Plot Summary</div>
  <div class="C4" style="width:649px; height:171px"></div>
  <div style="top:153px; left:-6px; width:270px; height:40px"><div class="S1" style="height:30px"></div><div class="S1" style="top:0px; left:11px; height:40px"></div><div class="S1" style="top:20px; left:22px; width:20px; height:20px"></div><div class="S1" style="left:43px; height:30px"></div><div class="S1" style="top:0px; left:54px; height:40px"></div><div class="S1" style="top:15px; left:65px; height:25px"></div><div class="S1" style="top:25px; left:76px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:97px; height:40px"></div><div class="S1" style="left:108px; height:30px"></div><div class="S1" style="top:20px; left:119px; height:20px"></div><div class="S1" style="left:130px; width:20px; height:30px"></div><div class="S1" style="top:0px; left:151px; height:40px"></div><div class="S1" style="left:162px; height:30px"></div><div class="S1" style="top:20px; left:173px; width:20px; height:20px"></div><div class="S1" style="left:194px; height:30px"></div><div class="S1" style="top:0px; left:205px; height:40px"></div><div class="S1" style="top:15px; left:216px; height:25px"></div><div class="S1" style="top:25px; left:227px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:248px; height:40px"></div><div class="S1" style="top:15px; left:259px; height:25px"></div><div class="S1" style="top:25px; left:270px; height:15px"></div><div class="S1" style="top:15px; left:281px; height:25px"></div></div>
  <div style="top:153px; left:287px; width:376px; height:40px"><div class="S1" style="height:30px"></div><div class="S1" style="top:0px; left:11px; height:40px"></div><div class="S1" style="top:20px; left:22px; width:20px; height:20px"></div><div class="S1" style="left:43px; height:30px"></div><div class="S1" style="top:0px; left:54px; height:40px"></div><div class="S1" style="top:15px; left:65px; height:25px"></div><div class="S1" style="top:25px; left:76px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:97px; height:40px"></div><div class="S1" style="left:108px; height:30px"></div><div class="S1" style="top:20px; left:119px; height:20px"></div><div class="S1" style="left:130px; width:20px; height:30px"></div><div class="S1" style="top:0px; left:151px; height:40px"></div><div class="S1" style="left:162px; height:30px"></div><div class="S1" style="top:20px; left:173px; width:20px; height:20px"></div><div class="S1" style="left:194px; height:30px"></div><div class="S1" style="top:0px; left:205px; height:40px"></div><div class="S1" style="top:15px; left:216px; height:25px"></div><div class="S1" style="top:25px; left:227px; width:20px; height:15px"></div><div class="S1" style="top:0px; left:248px; height:40px"></div><div class="S1" style="left:259px; height:30px"></div><div class="S1" style="top:20px; left:270px; height:20px"></div><div class="S1" style="top:15px; left:281px; width:20px; height:25px"></div><div class="S1" style="top:0px; left:302px; height:40px"></div><div class="S1" style="top:10px; left:313px; height:30px"></div><div class="S1" style="top:0px; left:324px; height:40px"></div><div class="S1" style="top:20px; left:335px; width:20px; height:20px"></div><div class="S1" style="left:356px; height:30px"></div><div class="S1" style="top:0px; left:367px; height:40px"></div></div>
  <div class="C4" style="width:649px; height:171px; background:none" id="Plot" name="Plot">${movie.overview?xhtml}</div>
 </div>

 <div style="width:676px; top:600px; left:40px; background-color:#000000; line-height:1px">&nbsp;</div>
 <div style="width:676px; top:606px; left:40px; color:#14171A; text-align:center">This MovieCard &copy;2003-2004 by <a id="mail" href="mailto:PurpleBytes" onClick="document.getElementById('mail').href='mailto:'+'Purple'+'Bytes'+'@'+'aol.'+'com'">PurpleBytes</a></div>
</body></html>