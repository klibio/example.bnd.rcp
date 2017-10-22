package io.klib.tools.parser.eclipse.features;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;
import io.klib.tools.ecl2bnd.model.eclipse.Plugin;

public class OutputBndBuildFormat extends OutputContextDefault implements OutputStrategy {

	private Path path;

	public OutputBndBuildFormat() {
		super();
	}

	@Override
	public void execute(List<Feature> features, Path outputPath) {
		path = outputPath;
		appendBndBuildHeader(tocHeader);
		features.stream().forEach(f -> {

			String featureID = f.getId();
			String featureVersion = f.getVersion();

			// bnd build
			tocHeader.append(String.format("# ${%s%s_%s}\n", FEATURE_PREFIX, featureID, featureVersion));
			featureExpression.append(String.format("%s%s_%s: \\\n", FEATURE_PREFIX, featureID, featureVersion));

			StringBuffer featureIncludesBndBuild = parseFeatureIncludeSection(f);
			StringBuffer featurePluginsBndBuild = parseFeaturePlugins(f);

			if (featureIncludesBndBuild.length() > 0) {
				featureExpression.append(String.format("%s", featureIncludesBndBuild.toString()));
				if (featurePluginsBndBuild.length() > 0) {
					featureExpression.append(",\\\n    \\\n");
				}
			}
			featureExpression.append(String.format("%s\n", featurePluginsBndBuild.toString()));
			featureExpression.append("\n");
		});

		writeBndBuildFile(tocHeader, featureExpression);

	}

	private void appendBndBuildHeader(StringBuffer tocBndBuild) {
		//Path filename = path.getName(path.getNameCount() - 2);
		Path filename = Paths.get("hugo");
		tocBndBuild.append(
				"# This file contains include variables for bnd files inside statements '-buildpath' or '-runbundles'\n");
		tocBndBuild.append("\n# Usage example for a dependency on feature org.eclipse.rcp\n");
		tocBndBuild.append(String.format("#-include: \\\n#    %s\n", filename ));
		tocBndBuild.append("#\n");
		tocBndBuild.append("# -buildpath: \\\n");
		tocBndBuild.append(String.format("#    ${%sorg.eclipse.rcp_4.7.0.v20170612-1255}\n", FEATURE_PREFIX));
		tocBndBuild.append("# or \n");
		tocBndBuild.append("# -runbundles: \\\n");
		tocBndBuild.append(String.format("#    ${%sorg.eclipse.rcp_4.7.0.v20170612-1255}\n", FEATURE_PREFIX));
		tocBndBuild.append("#\n");
		tocBndBuild.append("#\n");
		tocBndBuild.append("# TOC of the contained Eclipse Features\n");
		tocBndBuild.append(
				"# every entry name matches an Eclipse Features and contains all included features and plugins\n");
		tocBndBuild.append("#\n\n");
	}

	private void writeBndBuildFile(StringBuffer tocBndBuild, StringBuffer features) {
		byte[] bytesIndex = tocBndBuild.toString().getBytes();
		byte[] bytesFeatures = features.toString().getBytes();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			outputStream.write(bytesIndex);
			outputStream.write("# below this you have the concrete inlcude statements \n\n".getBytes());
			outputStream.write(bytesFeatures);
			Files.write(path, outputStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StringBuffer parseFeaturePlugins(Feature f) {
		StringBuffer plugins = new StringBuffer();
		List<Plugin> pluginList = f.getPlugins();
		if (pluginList != null) {
			pluginList.stream().forEach(p -> {
				String bundleID = p.getId();
				String version = p.getVersion();
/*				
				String os = p.getOs();
				String ws = p.getWs();
				String arch = p.getArch();
*/
				plugins.append(String.format("    %s;version='[%s,%s]',\\\n", bundleID, version, version));
			});
			// remove the trailing ",\\n"
			plugins.replace(plugins.length() - 3, plugins.length(), "");
		}
		return plugins;
	}
}
