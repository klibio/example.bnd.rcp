package io.klib.tools.parser.eclipse.features;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;
import io.klib.tools.ecl2bnd.model.eclipse.Plugin;

public class OutputBndBuildFormat extends OutputContextDefault implements OutputStrategy {

	private Path path;

	private final Hashtable<String, String> config_WIN32_WIN32_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_WIN32_WIN32_AARCH64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_MACOSX_COCOA_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_MACOSX_COCOA_AARCH64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_LINUX_GTK_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_LINUX_GTK_AARCH64 = new Hashtable<String, String>();

	TreeMap<String, TreeSet<String>> fragments = new TreeMap<>();
	TreeMap<String, Hashtable<String, String>> configs = new TreeMap<>();

	public OutputBndBuildFormat() {
		super();

		config_WIN32_WIN32_X86_64.put("osgi.os", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.ws", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.arch", "x86_64");
		configs.put("win32.win32.x86-64", config_WIN32_WIN32_X86_64);

		config_WIN32_WIN32_AARCH64.put("osgi.os", "win32");
		config_WIN32_WIN32_AARCH64.put("osgi.ws", "win32");
		config_WIN32_WIN32_AARCH64.put("osgi.arch", "aarch64");
		configs.put("win32.win32.aarch64", config_WIN32_WIN32_AARCH64);

		config_MACOSX_COCOA_X86_64.put("osgi.os", "macosx");
		config_MACOSX_COCOA_X86_64.put("osgi.ws", "cocoa");
		config_MACOSX_COCOA_X86_64.put("osgi.arch", "x86_64");
		configs.put("macosx.cocoa.x86-64", config_MACOSX_COCOA_X86_64);

		config_MACOSX_COCOA_AARCH64.put("osgi.os", "macosx");
		config_MACOSX_COCOA_AARCH64.put("osgi.ws", "cocoa");
		config_MACOSX_COCOA_AARCH64.put("osgi.arch", "aarch64");
		configs.put("macosx.cocoa.aarch64", config_MACOSX_COCOA_AARCH64);

		config_LINUX_GTK_X86_64.put("osgi.os", "linux");
		config_LINUX_GTK_X86_64.put("osgi.ws", "gtk");
		config_LINUX_GTK_X86_64.put("osgi.arch", "x86_64");
		configs.put("linux.gtk.x86-64", config_LINUX_GTK_X86_64);

		config_LINUX_GTK_AARCH64.put("osgi.os", "linux");
		config_LINUX_GTK_AARCH64.put("osgi.ws", "gtk");
		config_LINUX_GTK_AARCH64.put("osgi.arch", "aarch64");
		configs.put("linux.gtk.aarch64", config_LINUX_GTK_AARCH64);	
	}

	@Override
	public void execute(List<Feature> features, Path outputPath) {
		path = outputPath;
		appendBndBuildHeader(tocHeader);
		for (Feature f : features) {
			// map OSGi LDAP filter to bundles/fragments
			fragments.put("win32.win32.x86-64", new TreeSet<String>());
			fragments.put("win32.win32.aarch64", new TreeSet<String>());
			fragments.put("macosx.cocoa.x86-64", new TreeSet<String>());
			fragments.put("macosx.cocoa.aarch64", new TreeSet<String>());
			fragments.put("linux.gtk.x86-64", new TreeSet<String>());
			fragments.put("linux.gtk.aarch64", new TreeSet<String>());

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
			// featureExpression.append("# Platform specific macros for
			// bundles/fragments\n\n");
			Set<Entry<String, TreeSet<String>>> entrySet = fragments.entrySet();
			for (Entry<String, TreeSet<String>> entry : entrySet) {

				String platformLabel = entry.getKey().toString();

				TreeSet<String> platformBundles = entry.getValue();
				if (!platformLabel.isEmpty() && !platformBundles.isEmpty()) {

					String headerNote = String.format("# ${%s%s_%s_%s%s}\n", FEATURE_PREFIX, featureID, featureVersion,
							PLATFORM_PREFIX, platformLabel);
					tocHeader.append(headerNote);

					String bndFeatureEntry = String.format("%s%s_%s_%s%s: \\\n", FEATURE_PREFIX, featureID,
							featureVersion, PLATFORM_PREFIX, platformLabel);
					featureExpression.append(bndFeatureEntry);
					platformBundles.stream().sorted().forEach(i -> featureExpression.append(i));

					if (platformBundles.size() > 0) {
						// remove the trailing ",\\n"
						if (featureExpression.length() >= 3) {
							featureExpression.replace(featureExpression.length() - 3, featureExpression.length(), "");
						}
					}
					featureExpression.append("\n\n");
				}
			}
		}

		writeBndBuildFile(tocHeader, featureExpression);

	}

	private void appendBndBuildHeader(StringBuffer tocBndBuild) {
		// Path filename = path.getName(path.getNameCount() - 2);
		Path filename = Paths.get("hugo");
		tocBndBuild.append(
				"# This file contains include variables for bnd files inside statements '-buildpath' or '-runbundles'\n");
		tocBndBuild.append("\n# Usage example for a dependency on feature org.eclipse.rcp\n");
		tocBndBuild.append(String.format("#-include: \\\n#    %s\n", filename));
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
				 * String os = p.getOs(); String ws = p.getWs(); String arch = p.getArch();
				 */
				plugins.append(String.format("    %s;version='[%s,%s]',\\\n", bundleID, version, version));
			});
			// remove the trailing ",\\n"
			plugins.replace(plugins.length() - 3, plugins.length(), "");
		}
		return plugins;
	}
}
