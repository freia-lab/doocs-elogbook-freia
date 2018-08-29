<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- VARIABLES -->
<xsl:variable name="monthtitle"><xsl:value-of select="/list/entry/monthtitle"/></xsl:variable>
<xsl:variable name="monthlength" select="string-length($monthtitle)" />
<xsl:variable name="weektitle"><xsl:value-of select="/list/entry/weektitle"/></xsl:variable>
<xsl:variable name="weeklength" select="string-length($weektitle)" />

<!-- *************************************************** -->
<!--                                                     -->
<!--  This template is used to convert an isodate title  -->
<!--  into a title that is human readable. If there is   -->
<!--  a pagetitle tag it is also used.                   -->
<!--  e.g. 2009-03 = 2009 March = 2009 Maerz             -->
<!--                                                     -->
<!-- *************************************************** -->
<xsl:template name="createTitle">

	<!-- if there is a pagetitle, show it ! -->
	<xsl:if test="/list/entry/pagetitle !=''">
		<xsl:value-of select="/list/entry/pagetitle"/>
	</xsl:if>
	
	<xsl:choose>
		<!-- when both exists use them both if possible -->
		<xsl:when test="$monthtitle != '' and $weektitle != ''">
			<xsl:choose>
				<!-- YYYY-MM-DDThh:mm and YYYY-MM-DDThh:mm:ss -->
				<xsl:when test="$monthlength>13 and $weeklength>13">
					<xsl:call-template name="printWeekDay"/>
					<xsl:call-template name="printDay"/>
					<xsl:call-template name="printMonth"/>
					<xsl:call-template name="printYear"/>
					<xsl:call-template name="printTime"/>
				</xsl:when>
				<!-- YYYY-MM-DDThh -->
				<xsl:when test="$monthlength=13 and $weeklength=13">
					<xsl:call-template name="printWeekDay"/>
					<xsl:call-template name="printDay"/>
					<xsl:call-template name="printMonth"/>
					<xsl:call-template name="printYear"/>
					<xsl:call-template name="printShift"/>
				</xsl:when>
				<!-- YYYY-MM-DD -->
				<xsl:when test="$monthlength=10 and $weeklength=10">
					<xsl:call-template name="printWeekDay"/>
					<xsl:call-template name="printDay"/>
					<xsl:call-template name="printMonth"/>
					<xsl:call-template name="printYear"/>
				</xsl:when>
				<!-- unknown: print month iso -->
				<xsl:otherwise>
					<xsl:value-of select="$monthtitle"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>

		<!-- YYYY-MM -->
		<xsl:when test="$monthlength=7">
			<xsl:call-template name="printMonth"/>
			<xsl:call-template name="printYear"/>
		</xsl:when>
		
		<!-- YYYY-Www -->
		<xsl:when test="$weeklength=8">
			<xsl:call-template name="printYear"/>
			<xsl:call-template name="printWeek"/>
		</xsl:when>
		
		<!-- YYYY -->
		<xsl:when test="$monthlength=4">
			<xsl:call-template name="printYear"/>
		</xsl:when>
	</xsl:choose>
	
</xsl:template><!-- createTitle end -->



<!-- *************************************************** -->
<!--                                                     -->
<!--  Some helpfunctions to print the parts of an        -->
<!--  isodate. Some transformations use a language-file  -->
<!--  to create a human readable result.                 -->
<!--  e.g. 2009-03 = 2009 March = 2009 Maerz             -->
<!--                                                     -->
<!-- *************************************************** -->

<!-- print year of isodate -->
<xsl:template name="printYear">
	<xsl:text> </xsl:text>
	<!-- use the month when exists -->
	<xsl:choose>
		<xsl:when test="$monthlength>3">
			<xsl:value-of select="substring($monthtitle,1,4)"/>
		</xsl:when>
		<xsl:when test="$weeklength>3">
			<xsl:value-of select="substring($weektitle,1,4)"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<!-- print week of isodate -->
<xsl:template name="printWeek">
	<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateWeek']"/><xsl:text> </xsl:text>
	<xsl:value-of select="(substring($weektitle,7,2))"/>
</xsl:template>

<!-- print the weekday of isodate -->
<xsl:template name="printWeekDay">
	<xsl:choose>
		<xsl:when test="substring($weektitle,10,1)='1'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateMonday']"/>
		</xsl:when>
		<xsl:when test="substring($weektitle,10,1)='2'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateTuesday']"/>
		</xsl:when>
		<xsl:when test="substring($weektitle,10,1)='3'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateWednesday']"/>
		</xsl:when>
		<xsl:when test="substring($weektitle,10,1)='4'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateThursday']"/>
		</xsl:when>
		<xsl:when test="substring($weektitle,10,1)='5'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateFriday']"/>
		</xsl:when>
		<xsl:when test="substring($weektitle,10,1)='6'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateSaturday']"/>
		</xsl:when>
		<xsl:when test="substring($weektitle,10,1)='7'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateSunday']"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<!-- print the shift of isodate (morning, afternoon, night) -->
<xsl:template name="printShift">
<xsl:choose>
	<xsl:when test="substring($monthtitle,12,2)='07'">
		<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateMorning']"/>
	</xsl:when>
	<xsl:when test="substring($monthtitle,12,2)='15'">
		<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateAfternoon']"/>
	</xsl:when>
	<xsl:when test="substring($monthtitle,12,2)='23'">
		<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateNight']"/>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="printTime"/>
	</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- print month of isodate -->
<xsl:template name="printMonth">
	<xsl:choose>
		<xsl:when test="substring($monthtitle,6,2)='01'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateJanuary']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='02'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateFebruary']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='03'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateMarch']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='04'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateApril']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='05'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateMay']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='06'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateJune']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='07'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateJuly']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='08'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateAugust']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='09'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateSeptember']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='10'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateOctober']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='11'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateNovember']"/>
		</xsl:when>
		<xsl:when test="substring($monthtitle,6,2)='12'">
			<xsl:text> </xsl:text><xsl:value-of select="$dictionary/term[@key='DateDecember']"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<!-- print day of isodate -->
<xsl:template name="printDay">
	<xsl:text> </xsl:text>
	<xsl:value-of select="(substring($monthtitle,9,2))"/><xsl:text>.</xsl:text>
</xsl:template>

<!-- print time of isodate -->
<xsl:template name="printTime">
	<xsl:text> </xsl:text>
	<xsl:value-of select="(substring-after($monthtitle,'T'))"/>
</xsl:template>



</xsl:stylesheet>
