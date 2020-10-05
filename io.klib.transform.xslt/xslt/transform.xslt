<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- identity transformation - copy everything (nodes and attributes) without modifications -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- remove all required capabilities -->
	<xsl:template match="required[@namespace='osgi.service']">
		<!-- removing tag for required osgi.service -->
	</xsl:template>
	<xsl:template match="requiredProperties[@namespace='osgi.service']">
		<!-- removing tag for requiredProperties osgi.service -->
	</xsl:template>
	<xsl:template match="requiredProperties[@namespace='osgi.contract']">
		<!-- removing tag for requiredProperties osgi.contract -->
	</xsl:template>

	<!-- remove all provided capabilities -->
	<xsl:template match="provided[@namespace='osgi.service']">
		<!-- removing tag for provided osgi.contract -->
	</xsl:template>
    <xsl:template match="provided[@namespace='osgi.service']">
        <!-- removing tag for provided osgi.service -->
    </xsl:template>
    <xsl:template match="provided[@namespace='osgi.contract']">
        <!-- removing tag for provided osgi.contract -->
    </xsl:template>

</xsl:stylesheet>