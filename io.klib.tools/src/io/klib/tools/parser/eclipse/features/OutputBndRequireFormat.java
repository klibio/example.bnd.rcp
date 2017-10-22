package io.klib.tools.parser.eclipse.features;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;
import io.klib.tools.ecl2bnd.model.eclipse.Plugin;

public class OutputBndRequireFormat extends OutputContextDefault implements OutputStrategy {

	private Path path;

	public OutputBndRequireFormat() {
		super();
	}

	@Override
	public void execute(List<Feature> features, Path outputPath) {
		path = outputPath;
		appendBndRequireHeader(tocHeader);
		for (Feature f : features) {
			// map for platform specific bundles/fragments
			TreeMap<String, TreeSet<String>> fragments = new TreeMap<>();
			// pre-fill the supported platform configuration combinations
			fragments.put("(osgi.os=win32)(osgi.ws=win32)(osgi.arch=x86)", new TreeSet<String>());
			fragments.put("(osgi.os=win32)(osgi.ws=win32)(osgi.arch=x86_64)", new TreeSet<String>());
			fragments.put("(osgi.os=linux)(osgi.ws=gtk)(osgi.arch=x86)", new TreeSet<String>());
			fragments.put("(osgi.os=linux)(osgi.ws=gtk)(osgi.arch=x86_64)", new TreeSet<String>());
			fragments.put("(osgi.os=macosx)(osgi.ws=cocoa)(osgi.arch=x86_64)", new TreeSet<String>());

			String featureID = f.getId();
			String featureVersion = f.getVersion();

			// bnd require
			tocHeader.append(String.format("# ${%s%s_%s}\n", FEATURE_PREFIX, featureID, featureVersion));
			featureExpression.append(String.format("%s%s_%s: \\\n", FEATURE_PREFIX, featureID, featureVersion));

			StringBuffer featureIncludesBndRequire = parseFeatureIncludeSection(f);
			StringBuffer featurePluginsBndRequire = parseFeaturePlugins(f, fragments);

			if (featureIncludesBndRequire.length() > 0) {
				featureExpression.append(String.format("%s", featureIncludesBndRequire.toString()));
				if (featurePluginsBndRequire.length() > 0) {
					featureExpression.append(",\\\n    \\\n");
				}
			}
			featureExpression.append(String.format("%s\n", featurePluginsBndRequire.toString()));
			featureExpression.append("\n");

//			featureExpression.append("# Platform specific macros for bundles/fragments\n\n");
			Set<Entry<String, TreeSet<String>>> entrySet = fragments.entrySet();
			for (Entry<String, TreeSet<String>> entry : entrySet) {
				String platformLabel = toHumanReadableString(entry.getKey());

				TreeSet<String> platformBundles = entry.getValue();
				if (!platformLabel.isEmpty() && !platformBundles.isEmpty()) {
					
					tocHeader.append(String.format("# ${%s%s_%s_%s%s}\n", FEATURE_PREFIX, featureID, featureVersion, PLATFORM_PREFIX, platformLabel));
					featureExpression.append(String.format("%s%s_%s_%s%s: \\\n", FEATURE_PREFIX, featureID, featureVersion, PLATFORM_PREFIX, platformLabel));
					platformBundles.stream().sorted().forEach(i -> featureExpression.append(i));

					if (platformBundles.size()>0) {
						// remove the trailing ",\\n"
						if (featureExpression.length() >= 3) {
							featureExpression.replace(featureExpression.length() - 3, featureExpression.length(), "");
						}
					}
					featureExpression.append("\n\n");
				}
			}
		}

		writeBndRequireFile(tocHeader, featureExpression);
		System.out.println(featureExpression);

	}

	private String toHumanReadableString(String label) {
		label = label.replaceAll("\\)\\(", ".");
		label = label.replaceAll("osgi.os=", "");
		label = label.replaceAll("osgi.ws=", "");
		label = label.replaceAll("osgi.arch=", "");
		label = label.replaceAll("\\(|\\)", "");
		return label;
	}

	private void appendBndRequireHeader(StringBuffer tocBndRequire) {
		tocBndRequire.append("# This file contains include variables for bnd files inside statements '-runrequires'\n");
		tocBndRequire.append("\n# Usage example for a dependency on feature org.eclipse.rcp\n");
		tocBndRequire.append(String.format("#-include: \\\n#    %s\n", path.getName(path.getNameCount() - 1)));
		tocBndRequire.append("#\n");
		tocBndRequire.append("# -runrequires: \\\n");
		tocBndRequire.append(String.format("#    ${%sorg.eclipse.rcp_4.7.0.v20170612-1255}\n", FEATURE_PREFIX));
		tocBndRequire.append("#\n");
		tocBndRequire.append("#\n");
		tocBndRequire.append("# TOC of the contained Eclipse Features\n");
		tocBndRequire.append(
				"# every entry name matches an Eclipse Features and contains all included features and plugins\n");
		tocBndRequire.append("#\n\n");
	}

	private void writeBndRequireFile(StringBuffer tocBndRequire, StringBuffer features) {
		byte[] bytesIndex = tocBndRequire.toString().getBytes();
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

	private StringBuffer parseFeaturePlugins(Feature f, TreeMap<String, TreeSet<String>> fragments) {
		StringBuffer plugins = new StringBuffer();
		List<Plugin> pluginList = f.getPlugins();

		if (pluginList != null) {
			for (Plugin p : pluginList) {
				String bundleID = p.getId();
				// avoid dependencies to framework bundles
				if (!bundleID.contains("org.eclipse.osgi") 
						&& !bundleID.startsWith("org.eclipse.equinox.launcher")
					&& !bundleID.endsWith(".source")) {
					String version = p.getVersion();
					String os = p.getOs();
					String ws = p.getWs();
					String arch = p.getArch();
//					String nl = p.getNl();

					if (p.isFragment()) {
						// nothing specific currently
					}

					if (p.isUnpack()) {
						// will need specific treatment
						System.out.println("might cause trouble on execution in bnd");
					}
					String key = "";
					if (os != null) {
						key = "(osgi.os=" + os + ")";
					}
					if (ws != null) {
						key += "(osgi.ws=" + ws + ")";
					}
					if (arch != null) {
						key += "(osgi.arch=" + arch + ")";
					}
					if (key.length() > 0) {
						Set<String> configs = fragments.keySet();
						for (String config : configs) {
							if (config.contains(key)) {
								Set<String> bundles = fragments.get(config);
								bundles.add(String.format(
										"    osgi.identity;filter:='(&(osgi.identity=%s)(version>=%s))',\\\n", bundleID,
										version));
							}
						}
					} else {
						plugins.append(
								String.format("    osgi.identity;filter:='(&(osgi.identity=%s)(version>=%s))',\\\n",
										bundleID, version));
					}
				}
			}
			// remove the trailing ",\\n"
			if (plugins.length() >= 3) {
				plugins.replace(plugins.length() - 3, plugins.length(), "");
			}
		}
		return plugins;
	}

}
