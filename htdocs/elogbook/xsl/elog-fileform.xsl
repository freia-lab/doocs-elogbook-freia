<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- ******************************************************** -->
  <!-- Stylesheet for the form for text input.                  -->
  <!--                                                          -->
  <!-- Templates defined:                                       -->
  <!--                                        *                 -->
  <!--                                        entry             -->
  <!--                                        title             -->
  <!--                                        author            -->
  <!--                                        date|time         -->
  <!--                                        isodate           -->
  <!--                                        metainfo          -->
  <!--                                        file              -->
  <!--                                        link              -->
  <!--                                        category          -->
  <!--                                        severity          -->
  <!--                                        text              -->
  <!--                                        url_base          -->
  <!-- Templates needed:                                        -->
  <!--                                        keywords          -->
  <!--                                        location          -->
  <!-- Variables needed:                                        -->
  <!--                                        host              -->
  <!--                                        lang_code         -->
  <!--                                                          -->
  <!-- ******************************************************** -->
  
  <!-- ******************************************************** -->
  <!--Include keyword stylesheet (defines keywords and location)-->
  <xsl:include href="elog-master.xsl"/>
  
  <!-- Import selected dictionary -->
  <xsl:variable name="dictionary" select="document('elog-language-bundle.xml')/dictionary/bundle[@language=$lang_code]"/>

  <!-- We will produce only html output                         --> 
  <xsl:output method="html" omit-xml-declaration="yes"/>
  
  <!-- ******************************************************** -->
  <!-- Defines roles for all nodes                              -->
  <xsl:template match="*">
    <html>
      <xsl:if test="$spellchecker_enable!='false'">
        <script language="JavaScript" src="/elogbook/applets/SpellCheck/SpellCheckSingleTextArea.js" type="text/javascript"></script>
      </xsl:if>
      <xsl:if test="$mail2expert!='false'">
        <script language="JavaScript" src="/elogbook/javascript/mail2expert.js" type="text/javascript"></script>
      </xsl:if>
      <xsl:variable name="mailfunc">
        <xsl:if test="$mail2expert!='false'">importXML('<xsl:value-of select="$mail2expert"/>')</xsl:if>
      </xsl:variable>
      <body BGCOLOR="#c0c0c0" onLoad="{$mailfunc}">
        <base href="{$host}"/>
        <h3><xsl:value-of select="$dictionary/term[@key='LabelEntry']"/></h3>
        <!-- HTML form begins here -->
 
        <!-- Detect if metainfo is rel. to docroot or plain filename  -->
        <xsl:variable name="metainfo">
          <xsl:choose>
            <!-- if metainfo starts with IFS it is path rel. to docroot -->
            <xsl:when test="starts-with(/list/entry/metainfo, $fileseparatorChar)">
              <xsl:value-of select="/list/entry/metainfo"/>
            </xsl:when>
            <!-- else it is file in local elog -->
            <xsl:otherwise>
              <xsl:value-of select="concat(/list/entry/url_base, $fileseparatorChar, /list/entry/metainfo)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <form name="inputForm" action="{$host}{$edit_servlet}?source={$metainfo}" ENCTYPE="multipart/form-data" method="post"> 
          <table cellspacing="0"><xsl:apply-templates select="entry"/>
            <!-- first Part under text field -->
            <tr>
              <xsl:choose>
                <xsl:when test="$spellchecker_enable='true'">
                  <td></td>
                  <td>
                    <script type="text/javascript">
                      function OpenWin(Address) {
                      MyWin = window.open(Address, "Spellcheck Options", "width=900,height=800,scrollbars=yes");
                      MyWin.focus();
                      }
                    </script>
                    <input type="button" name="checkSpellingBtn" value="Spellchecker" onclick="onCheckSpelling(document.inputForm.text);"/>
                    <input type="button" value="{$dictionary/term[@key='LabelSpellcheckOption']}" onclick="OpenWin('/elogbook/applets/SpellCheck/setoptions.html');" />
                  </td>
                  <td colspan="2" align="right">
                    <script type="text/javascript">
                      function OpenWin(Address) {
                      MyWin = window.open(Address, "Wiki Markup Help", "width=720,height=600,left=100,top=200");
                      MyWin.focus();
                      }
                    </script>
                    <input type="button" value="{$dictionary/term[@key='LabelMarkupHelp']}" onclick="OpenWin('/elogbook/help/Markup-Help_{$lang_code}.html');" />
                    <input type="submit" value="{$dictionary/term[@key='LabelSaveEntry']}" style="font-weight: bold;"/>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td align="right" colspan="4">
                    <script type="text/javascript">
                      function OpenWin(Address) {
                      MyWin = window.open(Address, "Wiki Markup Help", "width=720,height=600,left=100,top=200");
                      MyWin.focus();
                      }
                    </script>
                    <input type="button" value="{$dictionary/term[@key='LabelMarkupHelp']}" onclick="OpenWin('/elogbook/help/Markup-Help_{$lang_code}.html');" />
                    <input type="submit" value="{$dictionary/term[@key='LabelSaveEntry']}" style="font-weight: bold;"/>
                  </td>
                </xsl:otherwise>
              </xsl:choose>
            </tr>
            <!-- File upload -->
            <tr>
              <td></td>
              <td valign="top">
                <fieldset><legend><b><xsl:value-of select="$dictionary/term[@key='LabelUpload']"/></b></legend>
                  <table><tr><td>
                    <div align="left">
                      <xsl:choose>
                        <xsl:when test="entry/image">
                          <xsl:variable name="imagename"><xsl:value-of select="substring-before($metainfo, '.')"/>.<xsl:value-of select="substring-after(entry/image, '.')"/></xsl:variable>
                          <b><xsl:value-of select="$dictionary/term[@key='LabelImgName']"/></b>
                          <script type="text/javascript">
                            function OpenWin(Address) {
                            MyWin = window.open(Address, "Show image", "width=800,height=600,left=100,top=200");
                            MyWin.focus();
                            }
                          </script>
                          <br><a href="{$host}{$imagename}"  title="Show image - opens new window" onclick="OpenWin(this.href); return false"><xsl:value-of select="entry/image" /></a></br>
                          <b><xsl:value-of select="$dictionary/term[@key='LabelChooseImgKey']"/></b>
                          <br><xsl:value-of select="$dictionary/term[@key='LabelChooseImgKeyComment']"/></br>
                        </xsl:when>
                        <xsl:otherwise>
                          <b><xsl:value-of select="$dictionary/term[@key='LabelChooseImgKey']"/></b>
                          <br><xsl:value-of select="$dictionary/term[@key='LabelChooseImgKeyComment']"/></br>
                        </xsl:otherwise>
                      </xsl:choose>
                      <input name="image" type="file" size="30" value="{entry/image}"/>
                    </div>
                  </td></tr></table>
                </fieldset>
              </td>
              <xsl:if test="$mail2expert!='false'">
                <!-- Mail to experts -->
                <td valign="top" colspan="2">
                  <fieldset><legend><b><xsl:value-of select="$dictionary/term[@key='LabelMailToExpert']"/></b></legend>
                    <table><tr><td><input name="experts" type="text" style="background-color: #E0E0E0" readonly="readonly"/><input name="email" type="HIDDEN" value=""/></td><td><input type="button" name="clear" value="clear" onClick="clearExpert()"/><input type="button" name="add" value="add" onClick="addFreeEmail()"/><input name="femail" type="text" size="35"/></td></tr></table>
                    <table><tr><td valign="top"><xsl:value-of select="$dictionary/term[@key='LabelMailToExpertTopic']"/><br></br><select name="topic" onChange="createList()"></select></td></tr><tr><td><xsl:value-of select="$dictionary/term[@key='LabelMailToExpertExperts']"/><br></br><select name="expertlist" onchange="addExpert()"></select></td></tr></table>
                  </fieldset>
                </td>
              </xsl:if>
            </tr>
          </table>
        </form><!-- End HTML form -->
        
        <!-- The spellchecker applet -->
        <xsl:if test="$spellchecker_enable='true'">
          <xsl:choose>
            <xsl:when test="$lang_code='en'">
              <applet codebase="{$host}/elogbook/applets/SpellCheck" code="SpellCheckApplet.class" name="SpellCheckApplet"
                archive="SpellCheckApplet.jar" width="10" height="5">
                <param name="MinSuggestDepth" value="30"/>
                <param name="Suggestions" value="typographical"/>
                <param name="MainLexicon1" value="am.tlx,url,t"/>
                <param name="MainLexicon2" value="am100k2.clx,url,c"/>
                <param name="MainLexicon3" value="correct.tlx,url,t"/>
                <param name="ALLOW_ACCENTED_CAPS_OPT" value="true"/>
                <param name="IGNORE_NON_ALPHA_WORD_OPT" value="true"/>
                <param name="REPORT_UNCAPPED_OPT" value="true"/>
                <param name="SPLIT_HYPHENATED_WORDS_OPT" value="true"/>
                <param name="SPLIT_CONTRACTED_WORDS_OPT" value="false"/>
                <param name="SPLIT_WORDS_OPT" value="false"/>
                <param name="STRIP_POSSESSIVES_OPT" value="true"/>
                <param name="IGNORE_HTML_MARKUPS_OPT" value="true"/>
                <param name="CASE_SENSITIVE_OPT" value="true"/>
                <param name="LicenseKey" value="43976609"/>
                <param name="Copyright" value="Copyright (c) 2001 Wintertree Software Inc. (www.wintertree-software.com)"/>
              </applet>
            </xsl:when>
            <xsl:when test="$lang_code='de'">
              <applet codebase="{$host}/elogbook/applets/SpellCheck" code="SpellCheckApplet.class" name="SpellCheckApplet"
                archive="SpellCheckApplet.jar" width="10" height="5">
                <param name="MinSuggestDepth" value="30"/>
                <param name="Suggestions" value="typographical"/>
                <param name="MainLexicon1" value="sscege.tlx,url,t"/>
                <param name="MainLexicon2" value="sscege2.clx,url,c"/>
                <param name="MainLexicon3" value="correct.tlx,url,t"/>
                <param name="ALLOW_ACCENTED_CAPS_OPT" value="true"/>
                <param name="IGNORE_NON_ALPHA_WORD_OPT" value="true"/>
                <param name="REPORT_UNCAPPED_OPT" value="true"/>
                <param name="SPLIT_HYPHENATED_WORDS_OPT" value="true"/>
                <param name="SPLIT_CONTRACTED_WORDS_OPT" value="false"/>
                <param name="SPLIT_WORDS_OPT" value="false"/>
                <param name="STRIP_POSSESSIVES_OPT" value="true"/>
                <param name="IGNORE_HTML_MARKUPS_OPT" value="true"/>
                <param name="CASE_SENSITIVE_OPT" value="true"/>
                <param name="LicenseKey" value="43976609"/>
                <param name="Copyright" value="Copyright (c) 2001 Wintertree Software Inc. (www.wintertree-software.com)"/>
              </applet>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="XSL_Error">
                <xsl:with-param name="error_code" select="$error_msg"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if><!-- spellchecker='true' -->
        
      </body> 
    </html>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines template for the entry node                      -->
  <xsl:template match="entry">
    <tr>
      <td><b><xsl:value-of select="$dictionary/term[@key='LabelAuthor']"/></b></td><td><xsl:apply-templates select="author"/></td>
      <td align="right"><b><xsl:value-of select="$dictionary/term[@key='LabelSeverity']"/></b></td><td align="right"><xsl:apply-templates select="severity"/></td>
    </tr>
    <tr>
      <td><b><xsl:value-of select="$dictionary/term[@key='LabelDate']"/></b></td>
      <td>
        <xsl:choose>
          <xsl:when test="$date_fmt='MM/dd/yyyy'">
            <input type="text" style="width: 25%" name="date" value="{concat(substring(isodate, 6, 2),'/',substring(isodate, 9, 2),'/',substring(isodate, 1, 4))}"/>
          </xsl:when>
          <xsl:when test="$date_fmt='dd.MM.yyyy'">
            <input type="text" style="width: 25%" name="date" value="{concat(substring(isodate, 9, 2),'.',substring(isodate, 6, 2),'.',substring(isodate, 1, 4))}"/>
          </xsl:when>
          <xsl:when test="$date_fmt='yyyy-MM-dd'">
            <input type="text"  style="width: 25%" name="date" value="{isodate}"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="XSL_Error">
              <xsl:with-param name="error_code" select="$error_msg"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="time"/></td>
        <td align="right"><b><xsl:value-of select="$dictionary/term[@key='LabelKeyword']"/></b></td><xsl:apply-templates select="keywords"/>
    </tr>
    <xsl:if test="$location_enable='true'">
      <tr>
        <td></td>
        <td></td>
        <td align="right"><b><xsl:value-of select="$dictionary/term[@key='LabelLocation']"/></b></td><xsl:apply-templates select="location"/>
      </tr>
    </xsl:if>
    <tr><td><b><xsl:value-of select="$dictionary/term[@key='LabelTitle']"/></b></td><xsl:apply-templates select="title"/></tr>
    <tr><td><b><xsl:value-of select="$dictionary/term[@key='LabelText']"/></b></td>
      <td colspan="3">
        <xsl:apply-templates select="text"/>
      </td>
    </tr>
    <xsl:apply-templates select="category"/>
    <xsl:apply-templates select="metainfo"/>
    <xsl:apply-templates select="image"/>
    <xsl:apply-templates select="file"/>
    <xsl:apply-templates select="link"/>
    <xsl:apply-templates select="url_base"/>
  </xsl:template>
    
  <!-- ******************************************************** -->
  <!-- Define template for all nodes                      -->
  <xsl:template match="title">
    <td colspan="3"><input type="text" name="{local-name()}" maxlength="150" style="width: 100%" value="{.}" /></td>
  </xsl:template>

  <xsl:template match="author">
    <input type="text" name="{local-name()}" value="{.}" style="width: 50%"/>
  </xsl:template>
      
  <xsl:template match="time">
    <input type="text" name="{local-name()}" value="{.}" style="width: 25%"/>
  </xsl:template>
      
  <!-- ******************************************************** -->
  <!-- Pass the nodes through                           -->
  <xsl:template match="metainfo | link | category">
    <input type="HIDDEN" name="{local-name()}" value="{.}"/>
  </xsl:template>
  
  <xsl:template match="image">
    <input type="HIDDEN" name="imageold" value="{.}"/>
  </xsl:template>
  
  <xsl:template match="file">
    <xsl:choose>
      <xsl:when test="position() &lt;= 9">
        <input type="HIDDEN" name="file-0{position()}" value="{.}"/>
      </xsl:when>
      <xsl:otherwise>
        <input type="HIDDEN" name="file-{position()}" value="{.}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Select bg color matching the severity                   -->
  <xsl:template match="severity">
    <select size="1" name="severity" style="width:100px;">
        <option selected="0"><xsl:value-of select="."/></option>
        <xsl:if test=".!='FATAL'"><option>FATAL</option></xsl:if>
        <xsl:if test=".!='ERROR'"><option>ERROR</option></xsl:if>
        <xsl:if test=".!='WARN'"><option>WARN</option></xsl:if>
        <xsl:if test=".!='INFO'"><option>INFO</option></xsl:if>
        <xsl:if test=".!='IDEA'"><option>IDEA</option></xsl:if>
        <xsl:if test=".!='DOCU'"><option>DOCU</option></xsl:if>
        <xsl:if test=".!='MEASURE'"><option>MEASURE</option></xsl:if>
        <xsl:if test=".!='TODO'"><option>TODO</option></xsl:if>
        <xsl:if test=".!='DONE'"><option>DONE</option></xsl:if>
        <xsl:if test=".!='FIXED'"><option>FIXED</option></xsl:if>
        <xsl:if test=".!='NONE'"><option>NONE</option></xsl:if>
        <xsl:if test=".!='DELETE'"><option>DELETE</option></xsl:if>
        <xsl:if test=".!='WOW'"><option>WOW</option></xsl:if>
      </select>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Show text node in textarea                               -->
  <xsl:template match="text">
    <textarea name="text" rows="14" cols="100">
      <xsl:value-of select="."/>
    </textarea>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- XSL file for editing (used for error reporting)          -->
  <xsl:template match="edit_xsl">
    <input type="HIDDEN" name="edit_xsl" value="{$edit_xsl}"/>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Create backlink from url_base node                       -->
  <xsl:template match="url_base">
    <input type="HIDDEN" name="backlink" value="{$host}{$view_servlet}?file={../url_base}&amp;xsl={$view_xsl}"/>
  </xsl:template>
  
</xsl:stylesheet>
