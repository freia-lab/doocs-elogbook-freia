<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- ******************************************************** -->
  <!-- Main stylesheet for display of the all eLogBook pages.   -->
  <!--                                                          -->
  <!-- Templates defined:                                       -->
  <!--                                          list            -->
  <!--                                          file            -->
  <!--                                          entry           -->
  <!-- Variables needed:                                        -->
  <!--                                          name            -->
  <!--                                                          -->
  <!-- ******************************************************** -->

  <!-- ******************************************************** -->
  <!-- Import the overall layout for all eLogBook pages         -->
  <xsl:import href="elog-master.xsl"/>

  <!-- ******************************************************** -->
  <!-- Defines rules for the root element                       -->
  <xsl:template match="list">
    <html>
      <head>
        <meta http-equiv="expires" content="0"/>
        <meta http-equiv="Pragma" content="no-cache"/>
        <meta name="description" content="{$name}"/>

        <link href="{$logroot}/images/Icon.ico" rel="shortcut icon"></link>
	<link rel="stylesheet" type="text/css" href="/elogbook/styles/list-blue.css" title="Blue"></link>
	<link rel="Alternate stylesheet" type="text/css" href="/elogbook/styles/list-classic.css" title="Classic"></link>

        <script src="/elogbook/javascript/treeConnection.js" type="text/javascript"></script>

        <title><xsl:value-of select="$name"/></title> 
      </head>

      <body class="main_body">
        <!--xsl:if test="entry/category!='DIR' and entry/category!='HELP'"></xsl:if>
        <xsl:if test="./entry/severity!='DELETE' and ./entry/category='DIR' or ./entry/category='HELP'">
          <h2><xsl:call-template name="title"/></h2>
        </xsl:if-->
        <!--xsl:apply-templates select="entry/oracle"/-->
        <!--xsl:apply-templates select="entry/shiftsum"/-->
        <!--xsl:apply-templates select="entry/machine_data"/-->
        <xsl:variable name="this_shift" select="substring-after($url_base,'/data')"/>
        <xsl:variable name="next_shift" select="entry/next_shift"/>
	<xsl:if test="($this_shift=$act_dir)">
	  <form name="machine_form">
            <!--input type="button" name="newmachinedata" value="Get Main Operation Parameters"
	    onClick="self.location.href='{$host}/TTFelog/machine.jsp'"/-->
          </form>
	</xsl:if>
        <xsl:if test="not(entry/shiftsum/isodate) and ($this_shift=$act_dir or $act_dir=$next_shift)">
          <!--form name="shiftsum_form">
            <input type="button" name="newshiftsum" value="New Shift Summary"
	    onClick="self.location.href='{$host}/elog/servlet/ShiftForm?file={$url_base}'"/>
          </form-->
        </xsl:if>
        <!--xsl:apply-templates select="include"/-->
        
          
	 <!--<span class="new_insert_old"><xsl:call-template name="new_insert"/></span>-->
	 <span class="new_insert"><xsl:call-template name="new_insert"/></span>
	
         <xsl:variable name="documentorder">
           <xsl:choose>
             <xsl:when test="$list_ascending='true'">ascending</xsl:when>
             <xsl:otherwise>descending</xsl:otherwise>
           </xsl:choose>
         </xsl:variable>
         <xsl:apply-templates select="entry">
           <xsl:sort order="{$documentorder}" select="category='DIR'"/>
           <xsl:sort order="{$documentorder}" select="isodate"/>
           <xsl:sort order="{$documentorder}" select="time"/>
         </xsl:apply-templates>
         
         <xsl:call-template name="footer"/>
	  
      </body>
    </html>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Select the processing for the file tag		        -->
  <xsl:template name="processImgs">
    <xsl:choose>
      <xsl:when test="file">
        <!-- Detect if file is rel. to docroot or plain filename  -->
        <xsl:variable name="filepath">
          <xsl:choose>
            <!-- if file starts with IFS it is path rel. to docroot -->
            <xsl:when test="starts-with(file, $fileseparatorChar)">
              <!-- Dummy to allocate variable for sure -->
            </xsl:when>
            <!-- else it is file in local elog -->
            <xsl:otherwise>
              <xsl:value-of select="concat(../url_base, $fileseparatorChar)"/>
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
              <xsl:value-of select="concat(../url_base, $fileseparatorChar, link)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>     

            <xsl:choose>
              <!-- Show picture? -->
              <xsl:when test="$picture='true'">
                      <xsl:if test="file !=''">
                        <xsl:choose>
                          <xsl:when test="file/../link">
                            <a href="{$linkname}" target="list_frame"><img alt="" src="{$filepath}{file}"/></a>
                          </xsl:when>
                          <xsl:otherwise>
                            <img alt="" src="{$filepath}{file}"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <xsl:when test="../link">
                    <a href="{$linkname}" target="list_frame"><xsl:value-of select="$linkname"/></a>
                    <xsl:text> or </xsl:text>
                    <a href="{$filepath}{file}" target="list_frame"><xsl:value-of select="concat($filepath, file)"/></a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{$filepath}{file}" target="list_frame"><xsl:value-of select="concat($filepath, file)"/></a>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>

      </xsl:when>
      <xsl:otherwise>
        <br></br>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines formating for the entry                          -->
  <xsl:template match="entry">
    <xsl:if test="severity!='DELETE' and category!='DIR'">
      <xsl:if test="../historyof">
        <xsl:apply-templates select="lastmodified"/>
      </xsl:if>

	<!-- the entry header is 1 tablerow with several parts-->
      <table class="header_table">

        <tr >      
	
          <!-- Anchor in ISO8601 format -->
          <a name="{isodate}T{time}"></a>
      
	  <!-- CATEGORY -->
	  <td class="header_category">
	    <xsl:apply-templates select="category"/>
	  </td>  
	  
          <!-- SEVERITY -->
          <td class="header_severity">
            <xsl:apply-templates select="severity"/>
          </td>
	  
	  <!-- DATE -->
	  <td class="header_date">
	    <xsl:call-template name="date">
              <xsl:with-param name="isodate" select="isodate"/>
	    </xsl:call-template>
	    <!--xsl:call-template name="date">
              <xsl:with-param name="isodate" select="isodate"/>
	    </xsl:call-template-->
	  </td>	
	  
	  <!-- TIME -->
          <td class="header_time">
            <nobr><xsl:apply-templates select="time"/></nobr>
          </td>
	  
	  <!-- AUTHOR -->
          <td class="header_author">
            <nobr><xsl:apply-templates select="author"/></nobr>
          </td>
	  
	  <!-- TITLE -->
          <td class="header_title">
            <nobr><xsl:apply-templates select="title"/></nobr>
          </td>	
	  
	  <!-- SPACER -->
	  <!--<td class="spacer"></td>-->
  

	  <!-- HISTORY -->  
          <td class="header_hist"><xsl:apply-templates select="hist"/></td>	  
	  
        </tr>      
	
      </table><!-- header table end -->
       
  <!-- the entry body displayed as a table with 2 rows and no columns -->
      <table class="content_table">
  
        <xsl:choose>
          <!-- when documents folder and config setup is "beside" then a special layout is displayed -->
          <xsl:when test="contains(../url_base,'data/doc') and $text_pos='beside'">            
	    <tc><td class="content_image"><xsl:call-template name="processImgs"/></td></tc> 
	    <tc><td class="content_text"><pre><xsl:apply-templates select="text"/></pre></td></tc>
	    <tc><td class="spacer" ></td></tc>
	  </xsl:when>
	  <!-- here is the normal layout for the logbook : first the text and below the image -->
	  <xsl:otherwise>
	    <xsl:if test="text!=''">
            <tr><td class="content_text"> <pre> <xsl:apply-templates select="text"/> </pre> </td><td class="spacer" ></td></tr>
	    </xsl:if>
	    <xsl:if test="file!=''">
            <tr><td class="content_image"> <xsl:call-template name="processImgs"/> </td><td class="spacer" ></td></tr>
	    </xsl:if>
          </xsl:otherwise>
	</xsl:choose> 
      </table>
    </xsl:if>
    
    <!-- It's a directory link - don't show file -->
    <xsl:if test="category='DIR' and severity!='DELETE'">
    <table>
    <xsl:text>DEBUG elog entry if dir and not delete</xsl:text>
      <TR valign="middle" bgcolor="#bbbbbb">
        <xsl:apply-templates select="category"/>
        <!--xsl:apply-templates select="severity"/-->
        <TD>
          <!-- Anchor in ISO8601 format -->
          <a name="{isodate}T{time}"></a>
        </TD>
        <!--xsl:value-of select="substring(time,1,5)"/-->
        <xsl:apply-templates select="title"/>
        <!--xsl:apply-templates select="author"/>
        <xsl:call-template name="date">
          <xsl:with-param name="isodate" select="isodate"/>
        </xsl:call-template>
        <xsl:text> </xsl:text-->
	<TD><xsl:apply-templates select="thumb"/></TD>
        <TD colspan="2">  <xsl:value-of select="text"/></TD>
      </TR>
      <!--TR><TD></TD><TD></TD><xsl:apply-templates select="text"/></TR-->
    </table>
    </xsl:if>
    
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Defines displaying thumbnails for DIRs                   -->
  <xsl:template match="thumb">
    <img alt="" src="{../url_base}/{.}"/><xsl:text> </xsl:text>
  </xsl:template>

</xsl:stylesheet>
