<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- identity transformation - copy everything (nodes and attributes) without 
		modifications -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- remove all download-* properties via empty template suppresses this attribute -->
	<xsl:template match="property [@name='download.md5']" />
    <xsl:template match="property [@name='download.checksum.md5']" />
    <xsl:template match="property [@name='download.checksum.sha-256']" />

</xsl:stylesheet>