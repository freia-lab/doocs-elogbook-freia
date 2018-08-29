<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <!-- ******************************************************** -->
  <!-- This is the master stylesheet for all XSL FO             -->
  <!-- transforming stylesheets.                                -->
  <!-- This stylesheet defines templates for the following      -->
  <!-- tags:                                                    -->
  <!--                                        list              -->
  <!--                                        date              -->
  <!--                                        time              -->
  <!--                                        entry             -->
  <!--                                        text              -->
  <!--                                        several html tags -->
  <!-- The following templates are only dummies to get rid of   -->
  <!-- data that is not used (till now):                        -->
  <!--                                        file              -->
  <!--                                        author            -->
  <!--                                        keywords          -->
  <!--                                        backlink          -->
  <!--                                        title             -->
  <!--                                        severity          -->
  <!--                                        metainfo          -->
  <!--                                        link              -->
  <!--                                        category          -->
  <!--                                        isodate           -->
  <!--                                        add_link          -->
  <!--                                                          -->
  <!-- Variables needed:                                        -->
  <!--                                        host              -->
  <!--                                        lang_code         -->
  <!--                                                          -->
  <!-- ******************************************************** -->

  <!-- ******************************************************** -->
  <!-- Import for determination of the host name                -->
  <xsl:import href="elog-master.xsl"/>

  <!-- ******************************************************** -->
  <!-- Import for displaying of the shiftsummary                -->
  <!--xsl:import href="shiftsum2pdf.xsl"/-->

  <!-- ******************************************************** -->
  <!-- This is the master document layout                       -->
  <xsl:template match="list">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="all"
          margin-right="2cm" margin-left="2cm" 
          margin-bottom="2.5cm" margin-top="2cm" 
          page-width="21cm" page-height="29.7cm">
          <fo:region-body margin-top="1.0cm"/>
          <fo:region-before precedence="true" extent="2cm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <!-- ******************************************************** -->
      <!-- Since FOP 0.20.3rc the following doesn't work anymore:   -->
      <!--                       fo:page-sequence master-name="all" -->
      <!-- We have to use "master-reference instead"                -->
      <fo:page-sequence master-reference="all" initial-page-number="1">

        <!-- ********************************************************   -->
        <!-- Page header. Prints:                                       -->
        <!-- "{name} LogBook {date} Page: {number}" or                  -->
        <!-- "{name} LogBook - Results for ... {req_text} - Page: {number}" -->
        <fo:static-content flow-name="xsl-region-before">
          <fo:block text-align="end" 
            font-size="10pt" 
            line-height="12pt" >
            <fo:table table-layout="fixed">
              <fo:table-column column-width="410pt" column-number="1"/>
              <fo:table-column column-width="67pt" column-number="2"/>
              
              <fo:table-body start-indent="0pt" text-align="end">
                <fo:table-row>
                  <fo:table-cell text-align="left">
                    <fo:block font-size="12pt">
                      <xsl:value-of select="$name"/><xsl:text> </xsl:text><xsl:value-of select="/list/entry/pagetitle"/>
                      <xsl:if test="/list/sparam">
                        <xsl:choose>
                          <xsl:when test="$lang_code='en'"><xsl:text> - Results for search of: </xsl:text></xsl:when>
                          <xsl:when test="$lang_code='de'"><xsl:text> - Ergebnisse der Suche nach: </xsl:text></xsl:when>
                        </xsl:choose>
                        <xsl:if test="name(/list/*[position()=2]) != 'entry'">
                          <fo:inline font-weight="bold"><xsl:value-of select="/list/*[position()=2]"/></fo:inline>
                        </xsl:if>
                      </xsl:if>
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block>
                      <xsl:choose>
                        <xsl:when test="$lang_code='en'"><xsl:text>Page </xsl:text></xsl:when>
                        <xsl:when test="$lang_code='de'"><xsl:text>Seite </xsl:text></xsl:when>
                      </xsl:choose>
                      <fo:page-number/>
                      <xsl:choose>
                        <xsl:when test="$lang_code='en'"><xsl:text> of </xsl:text></xsl:when>
                        <xsl:when test="$lang_code='de'"><xsl:text> von </xsl:text></xsl:when>
                      </xsl:choose>
                      <fo:page-number-citation ref-id="last-page"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
          </fo:block>
          <fo:block space-after="0.0em">
            <fo:leader leader-pattern="rule"
              rule-thickness="2.0pt"
              leader-length="17cm"/>
          </fo:block>
        </fo:static-content> 

        <!-- ******************************************************** -->
        <!-- Main page layout is defined here                         -->
        <fo:flow flow-name="xsl-region-body">
          <xsl:if test="entry/shiftsum">
            <xsl:apply-templates select="entry/shiftsum"/>
            <fo:block space-after="0.0em">
              <fo:leader leader-pattern="rule"
                rule-thickness="1.0pt"
                leader-length="17cm"/>
            </fo:block>
          </xsl:if>
          <xsl:apply-templates select="entry">
            <xsl:sort select="isodate"/>
            <xsl:sort select="time"/>
          </xsl:apply-templates>
          <!-- Needed for getting the total number of pages -->
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
      
    </fo:root>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Defintion of all pattern begins here                     -->
  <!--                                                          -->

  <!-- ******************************************************** -->
  <!-- Defines style for date                                       -->
  <xsl:template name="date">
    <xsl:param name="isodate"/>
    <xsl:variable name="day" select="substring($isodate, 9, 2)"/>
    <xsl:variable name="month" select="substring($isodate, 6, 2)"/>
    <xsl:variable name="year" select="substring($isodate, 1, 4)"/>
    <xsl:variable name="linkdir" select="substring-after(substring-after($url_base, $logroot), $datapath)"/>
    <xsl:choose>
      <xsl:when test="$date_fmt='MM/dd/yyyy'">
        <fo:basic-link external-destination="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}">
          <xsl:value-of select="concat($month, '/', $day, '/', $year)"/></fo:basic-link>
      </xsl:when>
      <xsl:when test="$date_fmt='dd.MM.yyyy'">
        <fo:basic-link external-destination="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}">
          <xsl:value-of select="concat($day, '.', $month, '.', $year)"/></fo:basic-link>
      </xsl:when>
      <xsl:when test="$date_fmt='yyyy-MM-dd'">
        <fo:basic-link external-destination="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}">
          <xsl:value-of select="$isodate"/></fo:basic-link>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="XSL_Error">
          <xsl:with-param name="error_code" select="$error_msg"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Template for each single entry                           -->
  <xsl:template match="time">
    <xsl:if test="../category='USERLOG'">
      <fo:block keep-with-next.within-page="always" font-size="10pt" white-space-collapse="false" space-after="0.2em">
      <fo:table table-layout="fixed">
        <!-- Column for the severity icon -->
        <fo:table-column column-width="20pt" column-number="1"/>
        <!-- Column for the date -->
        <fo:table-column column-width="55pt" column-number="2"/>
        <!-- Column for the time -->
        <fo:table-column column-width="65pt" column-number="3"/>
        <!-- Column for the author -->
        <fo:table-column column-width="80pt" column-number="4"/>
        <!-- Column for the title -->
        <fo:table-column column-width="261pt" column-number="5"/>

	<fo:table-body background-color="#cccccc" start-indent="0pt" text-align="start">
	  <fo:table-row>
            <fo:table-cell>
              <xsl:choose>
                <xsl:when test="../severity='FATAL'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/fatal.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='ERROR'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/error.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='WARN'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/warn.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='INFO'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/info.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='UNKNOWN'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/none.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='IDEA'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/idea.gif')}" height="8pt" scaling="uniform"/></fo:block> 
               </xsl:when>
                <xsl:when test="../severity='DOCU'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/book.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='MEASURE'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/measure.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='TODO'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/todo.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='NEWS'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/news.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='WOW'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/super.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='DONE'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/done.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='FIXED'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/fixed.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:when test="../severity='NONE'">
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/null.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:when>
                <xsl:otherwise>
                  <fo:block><fo:external-graphic src="{concat($docroot, $imagedir, '/none.gif')}" height="8pt" scaling="uniform"/></fo:block>
                </xsl:otherwise>
              </xsl:choose>
            </fo:table-cell>
            <xsl:choose>
              <xsl:when test="$lang_code='en'">
                <fo:table-cell>
                  <fo:block font-size="10pt">
                    <xsl:call-template name="date">
                      <xsl:with-param name="isodate" select="../isodate"/>
                    </xsl:call-template>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block font-size="10pt"><xsl:value-of select="substring(.,1,5)"/></fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block font-size="10pt">
                    <!-- If author is empty -->
                    <xsl:if test="../author='PrintMeta'">Author: Unknown</xsl:if>
                    <xsl:if test="../author='Your Name'">Author: Unknown</xsl:if>
                    <!-- Valid entry in author tag -->
                    <xsl:if test="../author!='PrintMeta'">
                      <xsl:if test="../author!='Your Name'"><xsl:value-of select="../author"/></xsl:if>
                    </xsl:if>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block font-size="10pt" font-weight="bold"><xsl:value-of select="../title"/></fo:block>
                </fo:table-cell>
              </xsl:when>
              <xsl:when test="$lang_code='de'">
                <fo:table-cell>
                  <fo:block font-size="10pt">
                    <xsl:call-template name="date">
                      <xsl:with-param name="isodate" select="../isodate"/>
                    </xsl:call-template>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block font-size="10pt"><xsl:value-of select="substring(.,1,5)"/>
                  <xsl:if test="$name='MVP eLogBook Portalseite'">
                    <xsl:choose>
                      <xsl:when test="contains(../dirpath, 'TTF')"> TTF</xsl:when>
                      <xsl:when test="contains(../dirpath, 'HERA')"> HERA</xsl:when>
                    </xsl:choose>
                  </xsl:if>
                </fo:block>
              </fo:table-cell>
                <fo:table-cell>
                  <fo:block font-size="10pt">
                    <!-- If author is empty -->
                    <xsl:if test="../author='PrintMeta'">Autor: Unbekannt</xsl:if>
                    <xsl:if test="../author='Your Name'">Autor: Unbekannt</xsl:if>
                    <!-- Valid entry in author tag -->
                    <xsl:if test="../author!='PrintMeta'">
                      <xsl:if test="../author!='Your Name'"><xsl:value-of select="../author"/></xsl:if>
                    </xsl:if>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block font-size="10pt" font-weight="bold"><xsl:value-of select="../title"/></fo:block>
                </fo:table-cell>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="XSL_Error">
                  <xsl:with-param name="error_code" select="$error_msg"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
	  </fo:table-row>
	</fo:table-body>
      </fo:table>
    </fo:block>
    <!--fo:block space-after="0.5em"-->
    </xsl:if>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Defines style for a single text entry with graphics.     -->
  <xsl:template match="text">
    <xsl:choose>
      <xsl:when test="$picture='true'">
        <!-- The "linefeed-treatment" is not supported up till now by FOP.  -->
        <!-- So I use "white-space-collapse" instead. For details see:      -->
        <!-- http://marc.theaimsgroup.com/?l=fop-user&m=102746038909112&w=2 -->
        <!--fo:block keep-with-next.within-page="always" font-size="10pt" linefeed-treatment="preserve" -->
        <!-- Further are we here using monospaced font to conserve the ASCII formating -->
        <fo:block keep-with-next.within-page="always" font-family="monospace" font-size="8pt" white-space-collapse="false">
          <xsl:apply-templates/>
        </fo:block>

        <xsl:if test="../file">
          <fo:block keep-with-next.within-page="always" text-align="center" space-after="0.2em">
            <xsl:for-each select="../file">
              <!-- Detect if metainfo is rel. to docroot or plain filename  -->
              <xsl:variable name="filename">
                <xsl:choose>
                  <!-- if metainfo starts with IFS it is path rel. to docroot -->
                  <xsl:when test="starts-with(., $fileseparatorChar)">
                    <xsl:value-of select="."/>
                  </xsl:when>
                  <!-- else it is file in local elog -->
                  <xsl:otherwise>
                    <xsl:value-of select="concat($url_base, ../dirpath, $fileseparatorChar, .)"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <fo:external-graphic src="{$docroot}{$filename}" width="330pt" scaling="uniform" display-align="center"/>
            </xsl:for-each>
          </fo:block>
          <!-- Detect if link is rel. to docroot or plain filename  -->
          <xsl:variable name="linkname">
            <xsl:choose>
              <!-- if link starts with IFS it is path rel. to docroot -->
              <xsl:when test="starts-with(../link, $fileseparatorChar)">
                <xsl:value-of select="../link"/>
              </xsl:when>
              <!-- else it is file in local elog -->
              <xsl:otherwise>
                <xsl:value-of select="concat($url_base, ../dirpath, $fileseparatorChar, ../link)"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="$lang_code='en'">
              <fo:block keep-with-next.within-page="always" font-size="6pt" text-align="center" color="blue">
                <fo:basic-link external-destination="{$host}{$linkname}">
                  <xsl:text>File: </xsl:text><xsl:value-of select="$host"/><xsl:value-of select="$linkname"/>
                </fo:basic-link>
              </fo:block>
            </xsl:when>
            <xsl:when test="$lang_code='de'">
              <fo:block keep-with-next.within-page="always" font-size="6pt" text-align="center" color="blue">
                <fo:basic-link external-destination="{$host}{$linkname}">
                  <xsl:text>Datei: </xsl:text><xsl:value-of select="$host"/><xsl:value-of select="$linkname"/>
                </fo:basic-link>
              </fo:block>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="XSL_Error">
                <xsl:with-param name="error_code" select="$error_msg"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise><!-- Do not show the pictures -->
        <!--xsl:if test="substring(../file, '.jpg')"-->
          <!-- The "linefeed-treatment" is not supported up till now by FOP.  -->
          <!-- So I use "white-space-collapse" instead. For details see:      -->
          <!-- http://marc.theaimsgroup.com/?l=fop-user&m=102746038909112&w=2 -->
          <!--fo:block font-size="10pt" linefeed-treatment="preserve" -->
          <fo:block font-family="Courier" font-size="10pt" white-space-collapse="false">
            <xsl:apply-templates/>
          </fo:block>
        <xsl:if test="../link">
          <!-- Detect if link is rel. to docroot or plain filename  -->
          <xsl:variable name="linkname">
            <xsl:choose>
              <!-- if link starts with IFS it is path rel. to docroot -->
              <xsl:when test="starts-with(../link, $fileseparatorChar)">
                <xsl:value-of select="../link"/>
              </xsl:when>
              <!-- else it is file in local elog -->
              <xsl:otherwise>
                <xsl:value-of select="concat($url_base, ../dirpath, $fileseparatorChar, ../link)"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="$lang_code='en'">
              <fo:block keep-with-next.within-page="always" font-size="6pt" text-align="center" color="blue">
                <fo:basic-link external-destination="{$host}{$linkname}">
                  <xsl:text>File: </xsl:text><xsl:value-of select="$host"/><xsl:value-of select="$linkname"/>
                </fo:basic-link>
              </fo:block>
            </xsl:when>
            <xsl:when test="$lang_code='de'">
              <fo:block keep-with-next.within-page="always" font-size="6pt" text-align="center" color="blue">
                <fo:basic-link external-destination="{$host}{$linkname}">
                  <xsl:text>Datei: </xsl:text><xsl:value-of select="$host"/><xsl:value-of select="$linkname"/>
                </fo:basic-link>
              </fo:block>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="XSL_Error">
                <xsl:with-param name="error_code" select="$error_msg"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:otherwise>      
    </xsl:choose><!-- choose picture -->
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Define sequence for layout within an entry               -->
  <xsl:template match="entry">
    <xsl:if test="severity!='DELETE'">
      <xsl:apply-templates select="time"/>
      <xsl:apply-templates select="text"/>
      <fo:block>
        <fo:leader leader-pattern="rule"
	  rule-style="none"
          rule-thickness="1.0pt"
          leader-length="100%"/>
      </fo:block>
    </xsl:if>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- All following templates are only dummies                 -->
  <xsl:template match="oracle"></xsl:template>
  <xsl:template match="file"></xsl:template>
  <xsl:template match="location"></xsl:template>
  <xsl:template match="author"></xsl:template>
  <xsl:template match="keywords"></xsl:template>
  <xsl:template match="backlink"></xsl:template>
  <xsl:template match="title"></xsl:template>
  <xsl:template match="severity"></xsl:template>
  <xsl:template match="metainfo"></xsl:template>
  <xsl:template match="link"></xsl:template>
  <xsl:template match="category"></xsl:template>
  <xsl:template match="isodate"></xsl:template>
  

  <!-- ******************************************************** -->
  <!-- Default to copy html tags   *                             -->
   <xsl:template match="xxx">
     <xsl:choose>
       <xsl:when test="count(*)>0">
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:value-of select="name()"/><xsl:text disable-output-escaping="yes">&gt;</xsl:text><xsl:apply-templates/><xsl:text disable-output-escaping="yes">&lt;/</xsl:text><xsl:value-of select="name()"/><xsl:text disable-output-escaping="yes">&gt;</xsl:text>
       </xsl:when>
       <xsl:otherwise>
         <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:value-of select="name()"/><xsl:text disable-output-escaping="yes">&gt;</xsl:text><xsl:value-of select="."/><xsl:text disable-output-escaping="yes">&lt;/</xsl:text><xsl:value-of select="name()"/><xsl:text disable-output-escaping="yes">&gt;</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- Tables                                                   -->
   <xsl:template match="table">
     <fo:table table-layout="fixed" width="100%">
	<xsl:for-each select="tr[1]/th|tr[1]/td">
          <fo:table-column column-width="proportional-column-width(1)"/>
	</xsl:for-each>
	<fo:table-body start-indent="0pt" text-align="start">
	   <xsl:apply-templates select="tr"/>
	</fo:table-body>
     </fo:table>
   </xsl:template>

   <xsl:template match="tr">
	<fo:table-row>
	   <xsl:apply-templates select="td"/>
	</fo:table-row>
   </xsl:template>

   <xsl:template match="td">
	<fo:table-cell border-width="1pt" border-style="solid">
	   <fo:block><xsl:apply-templates/></fo:block>
	</fo:table-cell>
   </xsl:template>


  <!-- ******************************************************** -->
  <!-- handle URLs                                              -->
   <xsl:template match="a">
     <fo:basic-link color="#0000ff" external-destination="{.}">
	<xsl:value-of select='.'/>
     </fo:basic-link>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle line breaks                                       -->
   <xsl:template match="br">
	<fo:block/>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle bold                                              -->
   <xsl:template match="b">
	<fo:inline font-weight="bold"><xsl:apply-templates/></fo:inline>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle italic                                            -->
   <xsl:template match="i">
	<fo:inline font-style="italic"><xsl:apply-templates/></fo:inline>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle header                                            -->
  <xsl:template match="h2">
    <fo:block font-size="11" font-weight="bold"
      space-before="6pt"
      space-after="6pt">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="h1">
    <fo:block font-size="12pt" font-weight="bold"
      space-before="6pt"
      space-after="6pt">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle horizontal line                                         -->
   <xsl:template match="hr">
      <fo:block>
        <fo:leader leader-pattern="rule"
          rule-thickness="0.5pt"
          leader-length="100%"/>
      </fo:block>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle lists                                   -->
  <xsl:template match="ul|ol">
    <fo:block>
      <xsl:attribute name="start-indent">
        <xsl:variable name="ancestors">
          <xsl:choose>
            <xsl:when test="count(ancestor::ol) or count(ancestor::ul)">
              <xsl:value-of select="count(ancestor::ul)+count(ancestor::ol)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>0</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="concat($ancestors, 'cm')"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="ul/li">
    <fo:block>
      <xsl:text>&#x2022;</xsl:text><xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="ol/li">
    <fo:block><fo:inline font-weight="bold"><xsl:number value="position()"/>. </fo:inline><xsl:apply-templates/></fo:block>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle code block                                             -->
   <xsl:template match="div">
      <fo:block space-before="0.2em"
        space-after="0.2em"
        background-color="#f0f0e9"
        border-after-color="#cccce0"
        border-after-width="0.1px"
        border-before-color="#cccce0"
        border-before-width="0.1px"
        border-end-color="#cccce0"
        border-end-width="0.1px"
        border-start-color="#cccce0"
        border-start-width="0.1px"
        start-indent="0.1in"
        end-indent="0.1in"
        border-style="solid">
        <xsl:apply-templates/>
      </fo:block>
   </xsl:template>

</xsl:stylesheet>
