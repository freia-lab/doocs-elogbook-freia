<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- ******************************************************** -->
  <!-- This stylesheet defines templates for the following      -->
  <!-- tags:                                                    -->
  <!--                                        list              -->
  <!--                                        entry             -->
  <!--                                        text              -->
  <!--                                        psfile            -->
  <!--                                        jpgfile           -->
  <!--                                        category          -->
  <!--                                        title             -->
  <!--                                                          -->
  <!-- The text, category and title tag are overriden.          -->
  <!--                                                          -->
  <!-- ******************************************************** -->

  <!-- ******************************************************** -->
  <!-- Import the overall layout for all pages              -->
  <xsl:import href="elog-master-wiki3.xsl"/>

  <!-- ******************************************************** -->
  <!-- Defines rules for the root element                       -->
  <xsl:template match="list">
    <html>
      <head>
        <meta http-equiv="expires" content="0"/>
        <META name="description" CONTENT="{$name}"/>
        <META HTTP-EQUIV="Pragma" CONTENT="no-cache"/>
        <META HTTP-EQUIV="expires" CONTENT="0"/>
	<style type="text/css">
         h1 { font-style:bold; font-size:large;}
         h2 { font-style:bold; font-size:medium; } 	 
	  		pre { font-size:12px; }
	  		.cb { background-color:#f0f0e9; border:3px solid #CCCCe0; margin-left: 1em; margin-right: 1em; font-size:80%;}
	  		ol { margin-bottom:0px; margin-top:0px; }
	  		ul { margin-bottom:0px; margin-top:0px; }
	  		.ut { font-size:12px; border:1px solid ; background-color:#e0e0e0; cellpadding:4; }
	  		.tc { border:1px solid ;  }
	  		hlt { background-color:yellow; }	  
	</style>
        <TITLE><xsl:value-of select="$name"/></TITLE> 
      </head>
      <body BGCOLOR="#cccccc">
        <h3>Search results: <xsl:value-of select="./total"/>  match(es) for

	  <xsl:if test="req_text"> text or title = <font color="#3300FF"><xsl:value-of select="req_text"/></font></xsl:if>
	  <xsl:if test="req_all"> text or title  = <font color="#3300FF"><xsl:value-of select="req_all"/></font></xsl:if>
	  <xsl:if test="req_phr"> phrase <font color="#3300FF">"<xsl:value-of select="req_phr"/>"</font></xsl:if>
	  <xsl:if test="req_or">  some of words : <font color="#3300FF"><xsl:value-of select="req_or"/></font></xsl:if>
	  <xsl:if test="req_not"> text without words  <font color="#3300FF"><xsl:value-of select="req_not"/></font></xsl:if>	  	  	  
	  <xsl:if test="keywds">  keyword = <font color="#3300FF"><xsl:value-of select="keywds"/> </font></xsl:if>
	  <xsl:if test="auth">  author = <font color="#3300FF"><xsl:value-of select="auth"/> </font></xsl:if>
	  <xsl:if test="loc">  location = <font color="#3300FF"><xsl:value-of select="loc"/> </font></xsl:if>
	  <xsl:if test="sev">  severity = <font color="#3300FF"><xsl:value-of select="sev"/> </font></xsl:if>
	  <xsl:if test="yr1"> start year = <font color="#3300FF"><xsl:value-of select="yr1"/> </font></xsl:if>
	  <xsl:if test="mon1"> start month = <font color="#3300FF"><xsl:value-of select="mon1"/> </font></xsl:if>
	  <xsl:if test="yr2"> end year = <font color="#3300FF"><xsl:value-of select="yr2"/> </font></xsl:if>
	  <xsl:if test="mon2"> end month = <font color="#3300FF"><xsl:value-of select="mon2"/> </font></xsl:if>

          <!-- We need to cut out the xsl argument since it's search.xsl not elog2pdf.xsl -->
          <xsl:variable name="sparam">
            <xsl:value-of select="concat(substring-before(/list/sparam, '&amp;xsl='), substring-after(/list/sparam, '.xsl'))"/>
          </xsl:variable>

	  <xsl:variable name="strt">
	   <xsl:value-of select="./start"/>
	  </xsl:variable>

          <!-- PDF generation buttons -->
          <xsl:choose>
            <xsl:when test="$lang_code='en'">
              <xsl:text> </xsl:text>
			  <a href="{$host}{$logroot}/results.jsp?{$sparam}&amp;xsl={$pdf_xsl}&amp;start={$strt}&amp;format=PDF&amp;pic=true&amp;dummy=dummy.pdf">
                <img border="0" src="{$imagedir}/pdfdoc_img.gif" alt="" title="Print page as PDF (with pictures)"/>
              </a>
            </xsl:when>
            <xsl:when test="$lang_code='de'">
              <xsl:text> </xsl:text>
			  <a href="{$host}{$logroot}/results.jsp?{$sparam}&amp;xsl={$pdf_xsl}&amp;start={$strt}&amp;format=PDF&amp;pic=true&amp;dummy=dummy.pdf">
                <img border="0" src="{$imagedir}/pdfdoc_img.gif" alt="" title="Drucke Seite als PDF mit Bildern"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="XSL_Error">
                <xsl:with-param name="error_code" select="$error_msg"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </h3>

  	<xsl:variable name="adr1" select="concat($host, $logroot, '/results_pdf.jsp?')"/>

	<xsl:variable name="epp" select="entries"/>
	<xsl:variable name="np" select="start + $epp"/>

	<xsl:variable name="pp" select="start - $epp"/>

	<xsl:variable name="parm" select="sparam"/>

        <!-- Call the counting for loop -->
       <TABLE cellspacing="0">
	<tr>
      	 <xsl:if test="$pp &gt;= 0">
      	    <td width="16" align="center">
      	    	<a href="{$adr1}{$parm}&amp;start={$pp}" target="_self">Previous</a>
      	    </td>
      	 </xsl:if>

	<xsl:variable name="cur"  select="start div $epp"/>
	<xsl:variable name="pagecount" select="total div $epp"/>

      <xsl:choose>
	<xsl:when test="($cur - 5 ) &gt;= 0">
	   <xsl:variable name="s1"   select="$cur - 5"/>
	   <xsl:choose>
	   	<xsl:when test="($s1 + 10) &lt; $pagecount">
			<xsl:variable name="end"   select="$s1 + 10"/>
         		<xsl:call-template name="for-loop">
           			<xsl:with-param name="testValue" select="$end"/>
           			<xsl:with-param name="i" select="$s1"/>
           			<xsl:with-param name="k" select="$s1 * $epp"/>
         		</xsl:call-template>
	   	</xsl:when>
	   	<xsl:otherwise>
			<xsl:variable name="end"   select="$pagecount"/>

         		<xsl:call-template name="for-loop">
           			<xsl:with-param name="testValue" select="$end"/>
           			<xsl:with-param name="i" select="$s1"/>
           			<xsl:with-param name="k" select="$s1 * $epp"/>
         		</xsl:call-template>
	   	</xsl:otherwise>
	   </xsl:choose>
	</xsl:when>
	<xsl:otherwise>
	     <xsl:variable name="s1"   select="0"/>
	     <xsl:variable name="end"  select="$pagecount"/>
	     <xsl:choose>
	        <xsl:when test="($s1+10) &lt; $pagecount">
		   <xsl:variable name="end" select="$s1+10"/>
         	   <xsl:call-template name="for-loop">
           		<xsl:with-param name="testValue" select="$end"/>
           		<xsl:with-param name="i" select="$s1"/>
           		<xsl:with-param name="k" select="$s1 * $epp"/>
         	   </xsl:call-template>
	        </xsl:when>
	        <xsl:otherwise>
		   <xsl:if test="$pagecount &gt; 1">
         	      <xsl:call-template name="for-loop">
           		<xsl:with-param name="testValue" select="$end"/>
           		<xsl:with-param name="i" select="$s1"/>
           		<xsl:with-param name="k" select="$s1 * $epp"/>
         	      </xsl:call-template>
		   </xsl:if>
	        </xsl:otherwise>
	     </xsl:choose>
	</xsl:otherwise>
     </xsl:choose>

      	 <xsl:if test="$np &lt; total">
      	    <td width="16" align="center">
      	    	<a href="{$adr1}{$parm}&amp;start={$np}" target="_self">Next</a>
      	    </td>
      	 </xsl:if> 	
 	</tr>
      </TABLE>

        <TABLE cellspacing="0">
          <xsl:apply-templates select="entry">
            <xsl:sort order="descending" select="isodate"/>
            <xsl:sort order="descending" select="time"/>
          </xsl:apply-templates>
        </TABLE>
      </body> 
    </html>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Select the processing for the file tag		        -->
  <xsl:template name="processImgs">
    <xsl:if test="file">
    <!-- Detect if file is rel. to docroot or plain filename  -->
    <xsl:variable name="filepath">
      <xsl:choose>
        <!-- if file starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(file, $fileseparatorChar)">
          <!-- Dummy to allocate variable for sure -->
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat(dirpath, $fileseparatorChar)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Detect if link is rel. to docroot or plain filename  -->
    <xsl:variable name="linkname">
      <xsl:choose>
        <!-- if link starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(link, $fileseparatorChar)">
          <xsl:value-of select="link"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat(dirpath, $fileseparatorChar, link)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <TD BGCOLOR="#ffffff" colspan="3">
      <TABLE>
        <xsl:choose>
          <!-- Is there a picture? -->
          <xsl:when test="file">
            <!-- Build up groups of five pictures (starting at 0) -->
            <xsl:for-each select="file[(position() + 4) mod 5 = 0]">
              <TR>
                <TD>
                  <xsl:if test=". !=''">
                    <xsl:choose>
                      <xsl:when test="../link">
                        <a href="{$linkname}" target="list_frame"><img src="{$filepath}{.}"/></a>
                      </xsl:when>
                      <xsl:otherwise>
                        <img src="{$filepath}{.}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </TD>
                <TD>
                  <xsl:if test="following-sibling::file[1] !=''">
                    <xsl:choose>
                      <xsl:when test="../link">
                        <a href="{$linkname}" target="list_frame"><img src="{$filepath}{following-sibling::file[1]}"/></a>
                      </xsl:when>
                      <xsl:otherwise>
                        <img src="{$filepath}{following-sibling::file[1]}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </TD>
                <TD>
                  <xsl:if test="following-sibling::file[2] !=''">
                    <xsl:choose>
                      <xsl:when test="link">
                        <a href="{$linkname}" target="list_frame"><img src="{$filepath}{following-sibling::file[2]}"/></a>
                      </xsl:when>
                      <xsl:otherwise>
                        <img src="{$filepath}{following-sibling::file[2]}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </TD>
                <TD>
                  <xsl:if test="following-sibling::file[3] !=''">
                    <xsl:choose>
                      <xsl:when test="link">
                        <a href="{$linkname}" target="list_frame"><img src="{$filepath}{following-sibling::file[3]}"/></a>
                      </xsl:when>
                      <xsl:otherwise>
                        <img src="{$filepath}{following-sibling::file[3]}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </TD>
                <TD>
                  <xsl:if test="following-sibling::file[4] !=''">
                    <xsl:choose>
                      <xsl:when test="link">
                        <a href="{$linkname}" target="list_frame"><img src="{$filepath}{following-sibling::file[4]}"/></a>
                      </xsl:when>
                      <xsl:otherwise>
                        <img src="{$filepath}{following-sibling::file[4]}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </TD>
              </TR>
            </xsl:for-each>
          </xsl:when>
        </xsl:choose>
      </TABLE>
    </TD>
  </xsl:if>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines formating for the entry                          -->
  <xsl:template match="entry">
    <xsl:if test="severity!='DELETE' and category!='DIR'">
      <TR>
        <xsl:apply-templates select="category"/>
        <xsl:apply-templates select="severity"/>
        <TD BGCOLOR="#99ffff">
          <!-- Anchor in ISO8601 format -->
          <a name="{isodate}T{time}"></a>
          <xsl:call-template name="mydate">
            <xsl:with-param name="isodate" select="isodate"/>
          </xsl:call-template>
          <xsl:text> </xsl:text>
          <xsl:value-of select="substring(time,1,5)"/>
        </TD>
        <xsl:apply-templates select="author"/>
        <xsl:apply-templates select="title"/>
      </TR>
      <!-- Defines if text is 'above' or 'beside' the images -->
      <xsl:choose>
        <!-- Define special layout for document part of the elog -->
        <xsl:when test="contains(../url_base,'data/doc')">
          <TR colspan="3">
            <xsl:call-template name="processImgs"/>
            <xsl:apply-templates select="text"/>
          </TR>
          <TR>
            <TD></TD><TD></TD>
          </TR>
        </xsl:when>
        <!-- Layout according to value of the conf file -->
        <xsl:when test="$text_pos='above'">
          <TR>
            <TD></TD><TD></TD>
            <xsl:apply-templates select="text"/>
          </TR>
          <TR>
            <TD></TD><TD></TD>
            <xsl:call-template name="processImgs"/>
          </TR>
          <TR>
            <TD colspan="5"></TD>
          </TR>
        </xsl:when>
        <!-- Layout according to value of the conf file -->
        <xsl:when test="$text_pos='beside'">
          <TR colspan="3">
            <xsl:call-template name="processImgs"/>
            <xsl:apply-templates select="text"/>
          </TR>
          <TR>
            <TD></TD><TD></TD>
          </TR>
        </xsl:when>
        <!-- Default layout is text above image -->
        <xsl:otherwise>
          <TR>
            <TD></TD><TD></TD>
            <xsl:apply-templates select="text"/>
          </TR>
          <TR>
            <TD></TD><TD></TD>
          </TR>
          <TR>
            <TD></TD><TD></TD>
            <xsl:call-template name="processImgs"/>
          </TR>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <!-- It's a directory link - don't show file -->
    <xsl:if test="category='DIR' and severity!='DELETE'">
      <TR>
        <xsl:apply-templates select="category"/>
        <!--xsl:apply-templates select="severity"/-->
        <TD>
          <!-- Anchor in ISO8601 format -->
          <a name="{isodate}T{time}"></a>
          <!--xsl:value-of select="substring(time,1,5)"/-->
          <xsl:apply-templates select="title"/>
          <!--xsl:apply-templates select="author"/>
          <xsl:call-template name="date">
            <xsl:with-param name="isodate" select="isodate"/>
          </xsl:call-template>
          <xsl:text> </xsl:text-->
        </TD>
      </TR>
      <!--TR><TD></TD><TD></TD><xsl:apply-templates select="text"/></TR-->
    </xsl:if>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines style for date                                   -->
  <xsl:template name="mydate">
    <xsl:param name="isodate"/>
    <xsl:variable name="day" select="substring($isodate, 9, 2)"/>
    <xsl:variable name="month" select="substring($isodate, 6, 2)"/>
    <xsl:variable name="year" select="substring($isodate, 1, 4)"/>
    <xsl:variable name="linkdir" select="substring-after(substring-after(dirpath, $logroot), $datapath)"/>
    <xsl:choose>
      <xsl:when test="$date_fmt='MM/dd/yyyy'">
        <a target="_top" title="Use right mouse 'copy link location' for ref. to this entry" href="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}"><xsl:value-of select="concat($month, '/', $day, '/', $year)"/></a>
      </xsl:when>
      <xsl:when test="$date_fmt='dd.MM.yyyy'">
        <a target="_top" title="Use right mouse 'copy link location' for ref. to this entry" href="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}"><xsl:value-of select="concat($day, '.', $month, '.', $year)"/></a>
      </xsl:when>
      <xsl:when test="$date_fmt='yyyy-MM-dd'">
        <a target="_top" title="Use right mouse 'copy link location' for ref. to this entry" href="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}"><xsl:value-of select="$isodate"/></a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="XSL_Error">
          <xsl:with-param name="error_code" select="$error_msg"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines handling for category                            -->
  <xsl:template match="category">
    <!-- Detect if file is rel. to docroot or plain filename  -->
    <xsl:variable name="metainfo">
      <xsl:choose>
        <!-- if file starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../metainfo, $fileseparatorChar)">
          <xsl:value-of select="../metainfo"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat(../dirpath, $fileseparatorChar, ../metainfo)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <TR>
      <TD></TD>
      <TD></TD>
      <TD colspan="2">
        <BR/>Logbook entry: <a href="{$host}{$view_servlet}?file={../dirpath}&amp;xsl={$view_xsl}&amp;picture=true#{../isodate}T{../time}">
        <xsl:value-of select="../dirpath"/></a>
    </TD>
    <TD></TD>
  </TR>
  <xsl:choose>
    <xsl:when test=".='SYSLOG'">
      <TD><img src="{$imagedir}/log.gif"/></TD>
    </xsl:when>
    <xsl:when test=".='USERLOG'">
      <TD><a href="{$host}{$edit_servlet}?file=/{$metainfo}&amp;xsl={$edit_xsl}&amp;mode=edit"><img
      border="0" src="{$imagedir}/meta.gif" alt="change this entry"/></a></TD>
    </xsl:when>
    <xsl:when test=".='HELP'">
      <TD><a href="{$host}{$edit_servlet}?file=/{$metainfo}&amp;xsl={$edit_xsl}&amp;mode=edit"
      target="list_frame"><img border="0" src="{$imagedir}/help.gif" alt="change this entry"/>   </a></TD>
    </xsl:when>
    <xsl:when test=".='UNKNOWN'">
      <TD><a href="{$host}{$edit_servlet}?file=/{$metainfo}&amp;xsl={$edit_xsl}&amp;mode=edit"
      target="list_frame"><img border="0" src="{$imagedir}/unknown.gif" alt="update existing logbook entry" /></a></TD>
    </xsl:when>
    <xsl:when test=".='DIR'">
      <TD><img src="{$imagedir}/dir.gif"/></TD>
    </xsl:when>
    <xsl:when test=".='IMAGE'">
      <TD><img border="0" src="{$imagedir}/null.gif"/></TD>
    </xsl:when>
    <xsl:when test=".='IMAGE_HAS_PS'">
      <TD><img border="0" src="{$imagedir}/null.gif"/></TD>
    </xsl:when>
    <xsl:when test=".='ERROR'">
      <TD><a href="{$host}{$edit_servlet}?file=/{$metainfo}&amp;xsl={$edit_xsl}&amp;mode=edit"
      target="list_frame"><img border="0" src="{$imagedir}/error.gif" alt="change this entry"/></a></TD>
    </xsl:when>
    <xsl:otherwise>
      <TD BGCOLOR="#c0c0c0">?<xsl:value-of select="."/>?</TD>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines handling for title                               -->
  <xsl:template match="title">
    <!-- Detect if metainfo is rel. to docroot or plain filename  -->
    <xsl:variable name="metainfo">
      <xsl:choose>
        <!-- if metainfo starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../metainfo, $fileseparatorChar)">
          <xsl:value-of select="../metainfo"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat(../dirpath, $fileseparatorChar, ../metainfo)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Detect if file is rel. to docroot or plain filename  -->
    <xsl:variable name="filename">
      <xsl:choose>
        <!-- if file starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../filename, $fileseparatorChar)">
          <xsl:value-of select="../filename"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat(../dirpath, $fileseparatorChar, ../filename)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="linkname">
      <xsl:choose>
        <!-- if link starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../link, $fileseparatorChar)">
          <xsl:value-of select="../link"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat(../dirpath, $fileseparatorChar, ../link)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <TD>
      <xsl:choose>
        <xsl:when test="../category='DIR'">
          <a href="{$host}{$view_servlet}?file={$filename}&amp;xsl={$view_xsl}&amp;picture=true" target="list_frame">
            <xsl:value-of select="."/>
          </a>
        </xsl:when>
        <xsl:when test="../category='IMAGE'">
          <a href="{$host}{$view_servlet}?file={$filename}" target="list_frame">
            <img src="{$filename}"/>
          </a>
        </xsl:when>
        <xsl:when test="../category='IMAGE_HAS_PS'">
          <a href="{$linkname}" target="list_frame"><img src="{$metainfo}"/></a>
        </xsl:when>
        <xsl:otherwise>
          <b><xsl:value-of select="."/></b>
        </xsl:otherwise>
      </xsl:choose>
    </TD>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Overrides text import rules                              -->
  <xsl:template match="text">
    <TD BGCOLOR="#ffffff" colspan="3">
      <PRE width="90">
        <xsl:apply-templates/>
      </PRE>
    </TD>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- For loop to create links to next/prev matches            -->
  <xsl:template name="for-loop">
    <xsl:param name="testValue"/>
    <xsl:param name="i"/>
    <xsl:param name="k"/>
    <xsl:param name="p" select="sparam"/>
    <xsl:param name="entp" select="entries"/>
       
    <xsl:variable name="testPassed">
      <xsl:if test="$i &lt; $testValue">
        <xsl:text>true</xsl:text>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="adr1" select="concat($host, $logroot, '/results.jsp?')"/>

    <xsl:if test="$testPassed='true'">
      <td width="16" align="center">
	<xsl:choose>
	  <xsl:when test="($i * $entp) = start">
	  	<b><xsl:value-of select="$i"/></b>
	  </xsl:when>
	  <xsl:otherwise>
            <a href="{$adr1}{$p}&amp;start={$k}" target="_self">
	    	         <xsl:value-of select="$i"/></a>
	  </xsl:otherwise>
	</xsl:choose>
      </td>

      <xsl:call-template name="for-loop">
        <xsl:with-param name="i"         select="$i + 1"/>
    	<xsl:with-param name="k"	 select="$k + $entp"/>
        <xsl:with-param name="testValue" select="$testValue"/>
      </xsl:call-template>
    </xsl:if> 
  </xsl:template>

</xsl:stylesheet>
