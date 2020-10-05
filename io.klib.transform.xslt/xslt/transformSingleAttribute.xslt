<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- identity transformation - copy everything (nodes and attributes) without modifications -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- remove  -->
	<xsl:template match="requiredProperties[@match='(objectClass=java.lang.Object)']">
		<!-- removing tag for required osgi.service -->
	</xsl:template>

</xsl:stylesheet>