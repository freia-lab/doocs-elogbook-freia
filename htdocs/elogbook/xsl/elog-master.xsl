<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- ******************************************************** -->
  <!-- Master stylesheet for all eLogBook stylesheets.          -->
  <!--                                                          -->
  <!-- Templates defined:                                       -->
  <!--                                        new_insert        -->
  <!--                                        category          -->
  <!--                                        title             -->
  <!--                                        author            -->
  <!--                                        time              -->
  <!--                                        date              -->
  <!--                                        text              -->
  <!--                                        add-link          -->
  <!--                                        severity          -->
  <!--                                        include           -->
  <!-- Variables defined:                                       -->
  <!--                                        xml_uri           -->
  <!--                                                          -->
  <!-- NOTE:                                                    -->
  <!-- The param xml_uri is passed to this stylesheet by pass-  -->
  <!-- ing it as an external parameter to the transformer.      -->
  <!--                                                          -->
  <!-- ******************************************************** -->
  
  <!-- Import rules for title creation/formatting -->
  <xsl:import href="isodate-transformation.xsl"/>

  <!-- Import selected dictionary -->
  <xsl:variable name="dictionary" select="document('elog-language-bundle.xml')/dictionary/bundle[@language=$lang_code]"/>

  <!-- ******************************************************** -->
  <!-- Global parameter pointing to the location of 'conf.xml'. -->
  <xsl:param name="xml_uri"/>

   <!-- ******************************************************* -->
  <!-- Transform uri to the full path to language xml           -->
  <xsl:param name="language_file">
    <xsl:variable name="tmp_str" select="substring-before($xml_uri, '/data')"/>
    <xsl:variable name="language" select="document($conf_file)/logbook/lang_code"/>
    <xsl:choose>
     
      <xsl:when test="document($conf_file)/logbook/lang_code='de'">
        <xsl:value-of select="concat($docroot, '/elogbook/lang/', 'de.xml')"/>
      </xsl:when>
      <xsl:when test="document($conf_file)/logbook/lang_code='en'">
        <xsl:value-of select="concat($docroot, '/elogbook/lang/', 'en.xml')"/>
      </xsl:when>
      <xsl:when test="document($conf_file)/logbook/lang_code='sp'">
        <xsl:value-of select="concat($docroot, '/elogbook/lang/', 'sp.xml')"/>
      </xsl:when>      
      <xsl:otherwise>
        <xsl:value-of select="concat($docroot, '/elogbook/lang/', 'en.xml')"/>
      </xsl:otherwise>
    
    
    </xsl:choose>
  </xsl:param>
  
  <!-- ******************************************************** -->
  <!-- Switch for display of pictures (only used for FO trafo.) -->
  <xsl:param name="picture"/>

  <!-- ******************************************************** -->
  <!-- Ascending order (Used for XMLlist and FO trafo.) -->
  <xsl:param name="list_ascending" select="false" />

  <!-- ******************************************************** -->
  <!-- Transform xml_uri the full path to work.xml              -->
  <xsl:param name="work_file">
    <xsl:variable name="tmp_str" select="substring-before($xml_uri, '/data')"/>
    <xsl:value-of select="concat('file:', $tmp_str, '/jsp/', 'work.xml')"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Param pointing to the current work directory             -->
  <xsl:param name="act_dir">
    <xsl:value-of select="document($work_file)/work/act_dir"/>
  </xsl:param>
  
  <!-- ******************************************************** -->
  <!-- Transform xml_uri to the full path to conf.xml           -->
  <xsl:param name="conf_file">
    <xsl:variable name="tmp_str" select="substring-before($xml_uri, '/data')"/>
    <xsl:value-of select="concat('file:', $tmp_str, '/jsp/', 'conf.xml')"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Param holding the literal logbook name			-->
  <xsl:param name="name">
    <xsl:value-of select="document($conf_file)/logbook/name"/>
  </xsl:param>
  
  <!-- ******************************************************** -->
  <!-- Import for determination of the absolut path             -->
  <xsl:param name="docroot">
    <xsl:value-of select="document($conf_file)/logbook/docroot"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Import for determination of eLogBook root dir            -->
  <xsl:param name="logroot">
    <xsl:value-of select="document($conf_file)/logbook/logroot"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Pointing to elog's data directory                        -->
  <xsl:param name="datapath">
    <xsl:value-of select="document($conf_file)/logbook/datapath"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Name of server hosting the elog(s)                       -->
  <xsl:param name="host">
    <xsl:value-of select="document($conf_file)/logbook/host"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Param defining the used language ('en' or 'de')          -->
  <xsl:param name="lang_code">
    <xsl:value-of select="document($conf_file)/logbook/lang_code"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Param defining the date format                           -->
  <xsl:param name="date_fmt">
    <xsl:value-of select="document($conf_file)/logbook/date_fmt"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Define if entry text is 'above' or 'beside' images       -->
  <xsl:param name="text_pos">
    <xsl:value-of select="document($conf_file)/logbook/text_pos"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Param defining used printer                              -->
  <xsl:param name="printer">
    <xsl:value-of select="document($conf_file)/logbook/printer"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Import for determination of eLogBook root dir            -->
  <xsl:param name="commentdir">
    <xsl:value-of select="document($conf_file)/logbook/commentdir"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Import for determination of eLogBook documentation dir   -->
  <xsl:param name="docudir">
    <xsl:value-of select="document($conf_file)/logbook/docudir"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Import for determination of eLogBook image dir           -->
  <xsl:param name="imagedir">
    <xsl:value-of select="document($conf_file)/logbook/imagedir"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Path to and name of the tree-servlet                     -->
  <xsl:param name="tree_servlet">
    <xsl:value-of select="document($conf_file)/logbook/tree_servlet"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path to and name of view servlet                         -->
  <xsl:param name="view_servlet">
    <xsl:value-of select="document($conf_file)/logbook/view_servlet"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Path to and name of edit servlet                         -->
  <xsl:param name="edit_servlet">
    <xsl:value-of select="document($conf_file)/logbook/edit_servlet"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Path to and name of search servlet                       -->
  <xsl:param name="srch_servlet">
    <xsl:value-of select="document($conf_file)/logbook/srch_servlet"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path to and name of the view XSL file                    -->
  <xsl:param name="view_xsl">
    <xsl:value-of select="document($conf_file)/logbook/view_xsl"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path to and name of the edit XSL file                    -->
  <xsl:param name="edit_xsl">
    <xsl:value-of select="document($conf_file)/logbook/edit_xsl"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path to and name of the search XSL file                  -->
  <xsl:param name="search_xsl">
    <xsl:value-of select="document($conf_file)/logbook/search_xsl"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path to and name of the search2 XSL file                 -->
  <xsl:param name="search2_xsl">
    <xsl:value-of select="document($conf_file)/logbook/search2_xsl"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path to and name of elog to PDF XSL file                 -->
  <xsl:param name="pdf_xsl">
    <xsl:value-of select="document($conf_file)/logbook/pdf_xsl"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Path search index                                        -->
  <xsl:param name="srch_index">
    <xsl:value-of select="document($conf_file)/logbook/srch_index"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- List of <keyword> elements                               -->
  <xsl:param name="keyword_list">
    <xsl:value-of select="document($conf_file)/logbook/keyword_list"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- List of <location> elements                              -->
  <xsl:param name="location_list">
    <xsl:value-of select="document($conf_file)/logbook/location_list"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Use location: true/false                                 -->
  <xsl:param name="location_enable">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/location_list/@enabled='true'">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Show location in list frame: true/false                                 -->
  <xsl:param name="show_location">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/location_list/@show='true'">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Mail to expert: if true = listname                       -->
  <xsl:param name="mail2expert">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/mail2expert/@enabled='true'">
        <xsl:value-of select="document($conf_file)/logbook/mail2expert/list/@name"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Additional List - name is taken from attr                -->
  <xsl:param name="additional_list">
    <xsl:value-of select="document($conf_file)/logbook/additional_list"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Use additional_list: true/false                          -->
  <xsl:param name="additional_list_enable">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/additional_list/@enabled='true'">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Name of the additional list                              -->
  <xsl:param name="additional_list_name">
    <xsl:value-of select="document($conf_file)/logbook/additional_list/@items"/>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Use spellchecker: true/false                             -->
  <xsl:param name="spellchecker_enable">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/spellchecker/@enabled='true'">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Use shiftsummary: true/false                             -->
  <xsl:param name="shiftsummary_enable">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/shiftsummary/@enabled='true'">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>
 
  <!-- ******************************************************** -->
  <!-- Show history of entries: true/false			-->
  <xsl:param name="view_history">
    <xsl:choose>
      <xsl:when test="document($conf_file)/logbook/view_history/@enabled='true'">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Define a param holding the base dir name.                -->
  <xsl:param name="url_base">
    <xsl:value-of select="/list/url_base"/>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- General error text                                       -->
  <xsl:param name="error_msg">
    <xsl:text>Error in XSL transformation!</xsl:text>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- Path separator character (JAVA: file.separatorChar)      -->
  <xsl:param name="fileseparatorChar">/</xsl:param>

  <!-- ******************************************************** -->
  <!-- Inserts one newline                                      -->
  <xsl:param name="newline">
    <xsl:text>
    </xsl:text>
  </xsl:param>

  <!-- ******************************************************** -->
  <!-- End of 'global parameter' section. Don not insert any    -->
  <!-- non-param values above this line                         -->
  <!-- ******************************************************** -->

  <!-- ******************************************************** -->
  <!-- This template returns the title for the current page     -->
  <xsl:template name="title">
    <xsl:call-template name="createTitle"/>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Footer contains prev next navigation arrows              -->
  <xsl:template name="footer">
  </xsl:template>
      
  <!-- ******************************************************** -->
  <!-- Defines template for the keyword node                    -->
  <xsl:template match="keywords">
    <td align="right">
      <select size="1" name="keywords" style="width: 100px">
        <xsl:choose>
          <xsl:when test=".=''">
            <option selected="0"><xsl:value-of select="document($conf_file)/logbook/keyword_list/keyword[1]"/></option>
            <xsl:call-template name="processList">
              <xsl:with-param name="List" select="document($conf_file)/logbook/keyword_list/keyword"/>
              <xsl:with-param name="selected" select="document($conf_file)/logbook/keyword_list/keyword[1]"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <option selected="0"><xsl:value-of select="."/></option>
            <xsl:call-template name="processList">
              <xsl:with-param name="List" select="document($conf_file)/logbook/keyword_list/keyword"/>
              <xsl:with-param name="selected" select="."/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </select>
    </td>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines template for the location node                    -->
  <xsl:template match="location">
    <td align="right">
      <select size="1" name="location" style="width: 100px">
        <xsl:choose>
          <xsl:when test=".=''">
            <option selected="0"><xsl:value-of select="document($conf_file)/logbook/location_list/location[1]"/></option>
            <xsl:call-template name="processList">
              <xsl:with-param name="List" select="document($conf_file)/logbook/location_list/location"/>
              <xsl:with-param name="selected" select="document($conf_file)/logbook/location_list/location[1]"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <option selected="0"><xsl:value-of select="."/></option>
            <xsl:call-template name="processList">
              <xsl:with-param name="List" select="document($conf_file)/logbook/location_list/location"/>
              <xsl:with-param name="selected" select="."/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </select>
    </td>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Iterates over the list "List" and select value           -->
  <xsl:template name="processList">
    <xsl:param name="List"/>
    <xsl:param name="selected"/>
    <xsl:for-each select="$List">
      <xsl:if test="$selected!=."><option><xsl:value-of select="."/></option></xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Used for adding a new entry                              -->
  <!--                                                          -->
  <xsl:template name="new_insert">
    <table width="100%"><tr>
      <xsl:choose>
        <xsl:when test="/list/entry[1]/edit[2]='no' or /list/historyof">
          <td width="30%"></td>
          <xsl:call-template name="navigation"/>
          <xsl:call-template name="sorting"/>
          <xsl:call-template name="create_pdf"/>
        </xsl:when>
        <xsl:otherwise>
          <td width="30%"><a href="{$host}{$edit_servlet}?file={url_base}&amp;xsl={$edit_xsl}&amp;mode=create"><img  border="0" src="{$imagedir}/new_entry.png" alt="" title="{document($language_file)/language/t_new_entry}"/></a></td>
          <xsl:call-template name="navigation"/>
          <xsl:call-template name="sorting"/>
          <xsl:call-template name="create_pdf"/>
          <xsl:if test="/list/entry/allow='subdir'"><a href="{$host}{$logroot}/new_dir.jsp?dir={$url_base}" ><img  alt="" border="0" src="{$imagedir}/newdir.gif" title="{document($language_file)/language/t_create_new_folder}"/></a></xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </tr></table>
  </xsl:template>
  
  <xsl:template name="navigation">
    <td width="60%"><table><tr><td width="10%">
      <xsl:if test="/list/historyof">
        <a href="{$host}{$view_servlet}?file={$url_base}&amp;xsl={$view_xsl}&amp;picture=true"><img border="0" src="{$imagedir}/back.png" alt="" title="{document($language_file)/language/t_back_to_lb}"/></a>
      </xsl:if>
      <xsl:if test="/list/entry/prev_shift">
        <a href="javascript:jumpTo('{$host}{$view_servlet}?file={$logroot}{$datapath}{/list/entry/prev_shift}&amp;xsl={$view_xsl}&amp;picture=true');"><img border="0" src="{$imagedir}/back.png" alt="" title="{document($language_file)/language/t_goto_prev}"/></a>
      </xsl:if></td>
      <td width="80%" align="center"><font style="font-weight: bold; font-size:18; margin-left:10">
        <xsl:call-template name="title"/>
        <xsl:if test="/list/historyof"><xsl:value-of select="document($language_file)/language/history_of"/><span style="color:blue;"><xsl:value-of select="substring(/list/entry/title, 0, 20)"/></span></xsl:if>
      </font></td><td align="right">
      <xsl:if test="/list/entry/next_shift">
        <a href="javascript:jumpTo('{$host}{$view_servlet}?file={$logroot}{$datapath}{/list/entry/next_shift}&amp;xsl={$view_xsl}&amp;picture=true');"><img border="0" src="{$imagedir}/forward.png" alt="" title="{document($language_file)/language/t_goto_next}"/></a>
      </xsl:if></td></tr></table>
    </td>
  </xsl:template>
  
  <xsl:template name="sorting">
    <td align="right" margin="-10">
    <a href="{$host}{$view_servlet}?file={url_base}&amp;xsl={$view_xsl}&amp;ascending=true&amp;picture=true">
      <img border="0" src="{$imagedir}/up.png" title="{$dictionary/term[@key='LabelSortAsc']}"/></a>
    <a href="{$host}{$view_servlet}?file={url_base}&amp;xsl={$view_xsl}&amp;ascending=false&amp;picture=true">
      <img border="0" src="{$imagedir}/down.png"  title="{$dictionary/term[@key='LabelSortDes']}"/></a>
    </td>
  </xsl:template>
  
  <xsl:template name="create_pdf">
    <td align="right" margin="-10">
      <a href="{$host}{$view_servlet}?file={url_base}&amp;xsl={$pdf_xsl}&amp;picture=true&amp;format=PDF&amp;dummy=dummy.pdf"><img  border="0" src="{$imagedir}/gnome-mime-application-pdf.png" alt="" title="{document($language_file)/language/t_print_pdf_with_p}"/></a>
      <a href="{$host}{$view_servlet}?file={url_base}&amp;xsl={$pdf_xsl}&amp;format=PDF&amp;dummy=dummy.pdf"><img  border="0" src="{$imagedir}/ascii.png" alt="" title="{document($language_file)/language/t_print_pdf_without_p}"/></a></td>
    </xsl:template>
    
 <!-- ******************************************************** -->
  <!-- Selects an icon matching the category                    -->
  <!--                                                          -->
  <xsl:template match="category">
    <!-- Detect if metainfo is rel. to docroot or plain filename  -->
    <xsl:variable name="metaname">
      <xsl:choose>
        <!-- if metainfo starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../metainfo, $fileseparatorChar)">
          <xsl:value-of select="../metainfo"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat($url_base, $fileseparatorChar, ../metainfo)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Detect if file is rel. to docroot or plain filename  -->
    <xsl:variable name="filename">
      <xsl:choose>
        <!-- if metainfo starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../file, $fileseparatorChar)">
          <xsl:value-of select="../file"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat($url_base, $fileseparatorChar, ../file)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

        <xsl:choose>
          <xsl:when test=".='SYSLOG'"> 
            <img alt="" src="{$imagedir}/log.gif"/>
          </xsl:when>
          <xsl:when test=".='USERLOG'">
            <xsl:choose>
              <xsl:when test="/list/entry[1]/edit[2]='no'">
                <img alt="" border="0" src="{$imagedir}/null.gif"/>
              </xsl:when>	    
              <xsl:otherwise>
                <a href="{$host}{$edit_servlet}?file={$metaname}&amp;xsl={$edit_xsl}&amp;mode=edit"><img border="0" src="{$imagedir}/meta.gif" alt="" title="{document($language_file)/language/t_edit_entry}"/></a>              
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test=".='HELP'">
              <a href="{$host}{$edit_servlet}?file={$metaname}&amp;xsl={$edit_xsl}&amp;mode=edit" target="list_frame"><img border="0" src="{$imagedir}/help.gif" alt="" title="{document($language_file)/language/t_edit_entry}"/></a>
          </xsl:when>
          <xsl:when test=".='UNKNOWN'">
            <a href="{$host}{$edit_servlet}?file={$metaname}&amp;xsl={$edit_xsl}&amp;mode=edit" target="list_frame"><img border="0" src="{$imagedir}/unknown.gif" alt="" title="{document($language_file)/language/t_edit_entry}"/></a>
          </xsl:when>
          <xsl:when test=".='DIR'">
            <a href="{$host}{$view_servlet}?file={$filename}&amp;xsl={$view_xsl}&amp;picture=true" target="list_frame"><img alt="" src="{$imagedir}/dir.gif" border="0"/></a>
          </xsl:when>
          <xsl:when test=".='IMAGE'">
            <img alt="" border="0" src="{$imagedir}/null.gif"/>
          </xsl:when>
          <xsl:when test=".='IMAGE_HAS_PS'">
            <img alt="" border="0" src="{$imagedir}/null.gif"/>
          </xsl:when>
          <xsl:when test=".='ERROR'">
            <a href="{$host}{$edit_servlet}?file={$metaname}&amp;xsl={$edit_xsl}&amp;mode=edit" target="list_frame"><img border="0" src="/images/error.gif" alt="" title="{document($language_file)/language/t_edit_entry}"/></a>
          </xsl:when>
          <xsl:when test=".='HTMLLOG'">
            <a href="{$host}/FCKeditor/Edit.jsp?elog={$logroot}&amp;file={$metaname}&amp;xsl={$edit_xsl}&amp;mode=edit"><img border="0" src="{$imagedir}/meta.gif" alt="" title="{document($language_file)/language/t_edit_entry}"/></a>
          </xsl:when>
          <xsl:otherwise>
            ?<xsl:value-of select="."/>?
          </xsl:otherwise>
        </xsl:choose>


  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Select the processing concerning the category            -->
  <xsl:template match="title">
    <!-- Detect if metainfo is rel. to docroot or plain filename  -->
    <xsl:variable name="filename">
      <xsl:choose>
        <!-- if metainfo starts with IFS it is path rel. to docroot -->
        <xsl:when test="starts-with(../file, $fileseparatorChar)">
          <xsl:value-of select="../file"/>
        </xsl:when>
        <!-- else it is file in local elog -->
        <xsl:otherwise>
          <xsl:value-of select="concat($url_base, $fileseparatorChar, ../file)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

      <xsl:choose>
        <xsl:when test="../category='DIR'">
          <a href="javascript:jumpTo('{$host}{$view_servlet}?file={$filename}&amp;xsl={$view_xsl}&amp;picture=true');" target="list_frame">
            <xsl:value-of select="."/>
          </a>
        </xsl:when>
        <xsl:when test="../category='IMAGE'">
          <a href="{$host}{$view_servlet}?file={$filename}" target="list_frame">
            <img src="{$filename}"/>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>

  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Used to print an error message (language mismatch).      -->
  <xsl:template name="XSL_Error">
    <xsl:param name="error_code"/>
    <xsl:text>ERROR: </xsl:text><xsl:value-of select="$error_code"/>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Define style for author                                  -->
  <xsl:template match="author">
      <xsl:value-of select="."/>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- sets button for history                                  -->
  <xsl:template match="hist">
  
   <xsl:if test="$view_history='true'">
   
      <xsl:if test=".='yes'">
      
        <xsl:variable name="histdate" select="substring(../metainfo, 0, 20)"/>

          <a href="{$host}{$view_servlet}?file={$url_base}&amp;xsl={$view_xsl}&amp;picture=true&amp;history={$histdate}" target="list_frame">
            <img border="0" src="{$imagedir}/meta_hist.gif" alt="" title="{document($language_file)/language/t_history}"/>
          </a>

      </xsl:if>

   </xsl:if>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- displays last modified date                              -->
  <xsl:template match="lastmodified">
    
      <xsl:value-of select="document($language_file)/language/history_last_modified"/>
      <xsl:value-of select="."/>
    
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines style for date                                   -->
  <xsl:template name="date">
    <xsl:param name="isodate"/>
    <xsl:variable name="day" select="substring($isodate, 9, 2)"/>
    <xsl:variable name="month" select="substring($isodate, 6, 2)"/>
    <xsl:variable name="year" select="substring($isodate, 1, 4)"/>
    <xsl:variable name="linkdir" select="substring-after(substring-after($url_base, $logroot), $datapath)"/>
    <xsl:choose>
      <xsl:when test="$date_fmt='MM/dd/yyyy'">
        <a target="_top" title="{document($language_file)/language/t_date}" href="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}"><xsl:value-of select="concat($month, '/', $day, '/', $year)"/></a>
      </xsl:when>
      <xsl:when test="$date_fmt='dd.MM.yyyy'">
        <a target="_top" title="{document($language_file)/language/t_date}" href="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}"><xsl:value-of select="concat($day, '.', $month, '.', $year)"/></a>
      </xsl:when>
      <xsl:when test="$date_fmt='yyyy-MM-dd'">
        <a target="_top" title="{document($language_file)/language/t_date}" href="{$host}{$logroot}/show.jsp?dir={$linkdir}&amp;pos={$isodate}T{time}"><xsl:value-of select="$isodate"/></a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="XSL_Error">
          <xsl:with-param name="error_code" select="$error_msg"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Defines style for time                                   -->
  <xsl:template match="time">
      <xsl:value-of select="substring(.,1,5)"/>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Define style for text                                    -->
  <xsl:template match="text">
	<!-- We need this preformating to conserve the CR etc. -->
	<xsl:if test="../category='HTMLLOG'">
	  <iframe src="{$url_base}/{../link}" width="100%" height="300">Error: your browser has no IFRAMES</iframe>
	</xsl:if>
   	<xsl:apply-templates/>		
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Template for conversation to a link                      -->
  <xsl:template name="add-link">
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="contains($string, 'https://')">
        <!-- add links to the string *before* the 'http://' -->
        <xsl:call-template name="add-link">
          <xsl:with-param name="string" select="substring-before($string, 'https://')"/>
        </xsl:call-template>
        <xsl:variable name="rest" select="substring-after($string, 'https://')"/>
        <xsl:variable name="URL">
          <xsl:choose>
            <!-- Search for SPACE character terminating the link -->
            <xsl:when test="substring-before($rest, ' ')!=''">
              <xsl:value-of select="concat('https://', substring-before($rest, ' '))"/>
            </xsl:when>
            <!-- Search for LINEFEED character terminating the link -->
            <xsl:when test="substring-before($rest, '')!=''">
              <xsl:value-of select="concat('https://', substring-before($rest, ' '))"/>
            </xsl:when>
            <!-- Could not detect end of link statement (exit) -->
            <xsl:otherwise>
              <xsl:value-of select="concat('https://', $rest)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <!-- create the link -->
        <a href="{$URL}"><xsl:value-of select="$URL"/></a><xsl:text> </xsl:text>
        <!-- process the string after the link -->
        <xsl:call-template name="add-link">
          <xsl:with-param name="string" select="substring-after($rest, ' ')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($string, 'http://')">
        <!-- add links to the string *before* the 'http://' -->
        <xsl:call-template name="add-link">
          <xsl:with-param name="string" select="substring-before($string, 'http://')"/>
        </xsl:call-template>
        <xsl:variable name="rest" select="substring-after($string, 'http://')"/>
        <xsl:variable name="URL">
          <xsl:choose>
            <!-- Search for SPACE character terminating the link -->
            <xsl:when test="substring-before($rest, ' ')!=''">
              <xsl:value-of select="concat('http://', substring-before($rest, ' '))"/>
            </xsl:when>
            <!-- Search for LINEFEED character terminating the link -->
            <xsl:when test="substring-before($rest, '')!=''">
              <xsl:value-of select="concat('http://', substring-before($rest, ' '))"/>
            </xsl:when>
            <!-- Could not detect end of link statement (exit) -->
            <xsl:otherwise>
              <xsl:value-of select="concat('http://', $rest)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <!-- create the link -->
        <a href="{$URL}"><xsl:value-of select="$URL"/></a><xsl:text> </xsl:text>
        <!-- process the string after the link -->
        <xsl:call-template name="add-link">
          <xsl:with-param name="string" select="substring-after($rest, ' ')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($string, 'file:/')">
        <!-- give the value of the string before the 'file:/' -
             it can't contain 'http://' or 'file:/' -->
        <xsl:value-of select="substring-before($string, 'file:/')"/>
        <xsl:variable name="rest" select="substring-after($string, 'file:/')"/>
        <xsl:variable name="URL" select="concat('file:/', substring-before($rest, ' '))"/>
        <!-- create the link -->
        <a href="{$URL}"><xsl:value-of select="$URL"/></a><xsl:text> </xsl:text>
        <!-- process the string after the link -->
        <xsl:call-template name="add-link">
          <xsl:with-param name="string" select="substring-after($rest, ' ')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Select icon for severity                                 -->
  <xsl:template match="severity">
    <xsl:choose>
      <xsl:when test=".='FATAL'">	<img src="{$imagedir}/fatal.gif" alt="" title="{document($language_file)/language/s_fatal}"/>	</xsl:when>
      <xsl:when test=".='ERROR'">	<img src="{$imagedir}/error.gif" alt="" title="{document($language_file)/language/s_error}"/>	</xsl:when>
      <xsl:when test=".='WARN'">	<img src="{$imagedir}/warn.gif" alt="" title="{document($language_file)/language/s_warn}"/>	</xsl:when>
      <xsl:when test=".='INFO'">	<img src="{$imagedir}/info.gif" alt="" title="{document($language_file)/language/s_info}"/>	</xsl:when>
      <xsl:when test=".='UNKNOWN'">	<img src="{$imagedir}/none.gif" alt=""/>							</xsl:when>
      <xsl:when test=".='IDEA'">	<img src="{$imagedir}/idea.gif" alt="" title="{document($language_file)/language/s_idea}"/>	</xsl:when>
      <xsl:when test=".='DOCU'">	<img src="{$imagedir}/book.gif" alt="" title="{document($language_file)/language/s_docu}"/>	</xsl:when>
      <xsl:when test=".='MEASURE'">	<img src="{$imagedir}/measure.gif" alt="" title="{document($language_file)/language/s_measure}"/></xsl:when>
      <xsl:when test=".='TODO'">	<img src="{$imagedir}/todo.gif" alt="" title="{document($language_file)/language/s_todo}"/>	</xsl:when>
      <xsl:when test=".='DONE'">	<img src="{$imagedir}/done.gif" alt="" title="{document($language_file)/language/s_done}"/>	</xsl:when>
      <xsl:when test=".='FIXED'">	<img src="{$imagedir}/fixed.gif" alt="" title="{document($language_file)/language/s_fixed}"/>	</xsl:when>
      <xsl:when test=".='NONE'">	<img src="{$imagedir}/null.gif" width="31px" border="0" height="17px" alt=""/>			</xsl:when>
      <xsl:when test=".='CHRIS'">	<img src="{$imagedir}/christree.gif" alt=""/>							</xsl:when>
      <xsl:when test=".='NEWS'">	<img src="{$imagedir}/news.gif" alt="" title="{document($language_file)/language/s_news}"/>	</xsl:when>
      <xsl:when test=".='WOW'">		<img src="{$imagedir}/super.gif" alt="" title="{document($language_file)/language/s_wow}"/>	</xsl:when>
      <xsl:when test=".='DELETE'">	<img src="{$imagedir}/null.gif" alt=""/>							</xsl:when>
      <!--<xsl:otherwise><TD BGCOLOR="#c0c0c0">?<xsl:value-of select="."/>?</TD></xsl:otherwise>-->
    </xsl:choose>
  </xsl:template>

  <!-- ******************************************************** -->
  <!-- Defines formating for external html files                -->
  <xsl:template match="include">
    <IFRAME src="{.}" width="100%" height="300">
      <ILAYER src="{.}"></ILAYER>
    </IFRAME>
  </xsl:template>
  
  <!-- ******************************************************** -->
  <!-- Default to copy html tags                                -->
   <xsl:template match="*">
     <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:value-of select="name()"/>
     <xsl:for-each select="@*">
       <xsl:text> </xsl:text><xsl:value-of select="name()"/><xsl:text>=</xsl:text><xsl:text>"</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
     </xsl:for-each>
     <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text disable-output-escaping="yes">&lt;/</xsl:text><xsl:value-of select="name()"/><xsl:text disable-output-escaping="yes">&gt;</xsl:text>
   </xsl:template>
   
   <!-- ******************************************************** -->
   <!-- Tables in text fields                                    -->
   <xsl:template match="table">
	<xsl:text disable-output-escaping="yes">&lt;table class="ut" rules="all"&gt;</xsl:text><xsl:apply-templates/><xsl:text disable-output-escaping="yes">&lt;/table&gt;</xsl:text>
   </xsl:template>

   <!-- ******************************************************** -->
   <!-- Table cells in text fields                                    -->
   <xsl:template match="td">
	<xsl:text disable-output-escaping="yes">&lt;td class="tc"&gt;</xsl:text><xsl:apply-templates/><xsl:text disable-output-escaping="yes">&lt;/td&gt;</xsl:text>
   </xsl:template>

   <!-- ******************************************************** -->
   <!-- handle URLs                                              -->
   <xsl:template match="a">
	<xsl:text disable-output-escaping="yes">&lt;a target="_top" href="</xsl:text><xsl:value-of select='.'/><xsl:text disable-output-escaping="yes">"&gt;</xsl:text><xsl:value-of select='.'/><xsl:text disable-output-escaping="yes">&lt;/a&gt;</xsl:text>
   </xsl:template>

  <!-- ******************************************************** -->
  <!-- handle line breaks                                              -->
   <xsl:template match="br">
	<xsl:text disable-output-escaping="yes">&lt;br/&gt;</xsl:text>
   </xsl:template>


</xsl:stylesheet>
