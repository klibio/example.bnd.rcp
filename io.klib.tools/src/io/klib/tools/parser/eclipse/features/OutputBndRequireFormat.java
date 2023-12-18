package io.klib.tools.parser.eclipse.features;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;
import io.klib.tools.ecl2bnd.model.eclipse.Plugin;

public class OutputBndRequireFormat extends OutputContextDefault implements OutputStrategy {

	private boolean debug = Boolean.parseBoolean(System.getProperty("debug", "false"));
	private Path path;

	private final Hashtable<String, String> config_WIN32_WIN32_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_WIN32_WIN32_AARCH64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_MACOSX_COCOA_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_MACOSX_COCOA_AARCH64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_LINUX_GTK_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_LINUX_GTK_AARCH64 = new Hashtable<String, String>();

	TreeMap<String, TreeSet<String>> filterBundles = new TreeMap<>();
	TreeMap<String, Hashtable<String, String>> supportedOsgiConfigs = new TreeMap<>();

	private boolean versioned;

	public OutputBndRequireFormat(boolean versioned) {
		super();
		this.versioned = versioned;
		config_WIN32_WIN32_X86_64.put("osgi.os", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.ws", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.arch", "x86_64");
		supportedOsgiConfigs.put("win32.win32.x86_64", config_WIN32_WIN32_X86_64);

		config_WIN32_WIN32_X86_64.put("osgi.os", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.ws", "win32");
		config_WIN32_WIN32_AARCH64.put("osgi.arch", "aarch64");
		supportedOsgiConfigs.put("win32.win32.aarch64", config_WIN32_WIN32_AARCH64);

		config_MACOSX_COCOA_X86_64.put("osgi.os", "macosx");
		config_MACOSX_COCOA_X86_64.put("osgi.ws", "cocoa");
		config_MACOSX_COCOA_X86_64.put("osgi.arch", "x86_64");
		supportedOsgiConfigs.put("cocoa.macosx.x86_64", config_MACOSX_COCOA_X86_64);

		config_MACOSX_COCOA_AARCH64.put("osgi.os", "macosx");
		config_MACOSX_COCOA_AARCH64.put("osgi.ws", "cocoa");
		config_MACOSX_COCOA_AARCH64.put("osgi.arch", "aarch64");
		supportedOsgiConfigs.put("cocoa.macosx.aarch64", config_MACOSX_COCOA_AARCH64);

		config_LINUX_GTK_X86_64.put("osgi.os", "linux");
		config_LINUX_GTK_X86_64.put("osgi.ws", "gtk");
		config_LINUX_GTK_X86_64.put("osgi.arch", "x86_64");
		supportedOsgiConfigs.put("gtk.linux.x86_64", config_LINUX_GTK_X86_64);

		config_LINUX_GTK_AARCH64.put("osgi.os", "linux");
		config_LINUX_GTK_AARCH64.put("osgi.ws", "gtk");
		config_LINUX_GTK_AARCH64.put("osgi.arch", "aarch64");
		supportedOsgiConfigs.put("gtk.linux.aarch64", config_LINUX_GTK_AARCH64);
	}

	@Override
	public void execute(List<Feature> features, Path outputPath) {
		path = outputPath;
		appendBndRequireHeader(tocHeader);

		// map OSGi LDAP filter to bundles/fragments
		filterBundles.put("win32.win32.x86-64", new TreeSet<String>());
		filterBundles.put("win32.win32.aarch64", new TreeSet<String>());
		filterBundles.put("cocoa.macosx.x86-64", new TreeSet<String>());
		filterBundles.put("cocoa.macosx.aarch64", new TreeSet<String>());
		filterBundles.put("gtk.linux.x86-64", new TreeSet<String>());
		filterBundles.put("gtk.linux.aarch64", new TreeSet<String>());

		// pre-fill the supported platform configuration combinations
		for (Feature f : features) {
			String featureID = f.getId();
			String featureVersion = f.getVersion();
			if (debug)
				System.out.println("processing feature " + featureID);

			// bnd require
			if (versioned) {
				tocHeader.append(String.format("# ${%s%s_%s}\n", FEATURE_PREFIX, featureID, featureVersion));
				featureExpression.append(String.format("%s%s_%s: \\\n", FEATURE_PREFIX, featureID, featureVersion));
			} else {
				tocHeader.append(String.format("# ${%s%s}\n", FEATURE_PREFIX, featureID));
				featureExpression.append(String.format("%s%s: \\\n", FEATURE_PREFIX, featureID));
			}

			StringBuffer featureIncludesBndRequire = parseFeatureIncludeSection(f, versioned);
			StringBuffer featurePluginsBndRequire = parseFeaturePlugins(f, filterBundles);

			if (featureIncludesBndRequire.length() > 0) {
				featureExpression.append(String.format("%s", featureIncludesBndRequire.toString()));
				if (featurePluginsBndRequire.length() > 0) {
					featureExpression.append(",\\\n    \\\n");
				}
			}
			featureExpression.append(String.format("%s\n", featurePluginsBndRequire.toString()));
			featureExpression.append("\n");

			// featureExpression.append("# Platform specific macros for
			// bundles/fragments\n\n");
			Set<Entry<String, TreeSet<String>>> entrySet = filterBundles.entrySet();
			for (Entry<String, TreeSet<String>> entry : entrySet) {

				String platformLabel = entry.getKey().toString();

				TreeSet<String> platformBundles = entry.getValue();
				if (!platformLabel.isEmpty() && !platformBundles.isEmpty()) {

					String headerNote = "";
					if (versioned) {
						headerNote = String.format("# ${%s%s_%s_%s%s}\n", FEATURE_PREFIX, featureID, featureVersion,
								PLATFORM_PREFIX, platformLabel);
					} else {
						headerNote = String.format("# ${%s%s_%s%s}\n", FEATURE_PREFIX, featureID, PLATFORM_PREFIX,
								platformLabel);
					}

					tocHeader.append(headerNote);

					String bndFeatureEntry = "";
					if (versioned) {
						bndFeatureEntry = String.format("%s%s_%s_%s%s: \\\n", FEATURE_PREFIX, featureID, featureVersion,
								PLATFORM_PREFIX, platformLabel);
					} else {
						bndFeatureEntry = String.format("%s%s_%s%s: \\\n", FEATURE_PREFIX, featureID, PLATFORM_PREFIX,
								platformLabel);
					}

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

		writeBndRequireFile(tocHeader, featureExpression);

	}

	private void appendBndRequireHeader(StringBuffer toc) {
		toc.append("# This file contains include variables for bnd files inside statements '-runrequires'\n");
		toc.append("\n# Usage example for a dependency on feature org.eclipse.rcp\n");
		toc.append(String.format("#-include: \\\n#    %s\n", path.getName(path.getNameCount() - 1)));
		toc.append("#\n");
		toc.append("# -runrequires: \\\n");
		toc.append(String.format("#    ${%sorg.eclipse.rcp_4.7.0.v20170612-1255}\n", FEATURE_PREFIX));
		toc.append("#\n");
		toc.append("#\n");
		toc.append("# TOC of the contained Eclipse Features\n");
		toc.append("# every entry name matches an Eclipse Features and contains all included features and plugins\n");
		toc.append("#\n\n");
	}

	private void writeBndRequireFile(StringBuffer toc, StringBuffer features) {
		byte[] bytesIndex = toc.toString().getBytes();
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
		String filterExp = "";

		if (pluginList != null) {

			for (Plugin p : pluginList) {
				String bundleID = p.getId();
				// avoid dependencies to framework bundles
				if (!bundleID.contains("org.eclipse.osgi") && !bundleID.startsWith("org.eclipse.equinox.launcher")) {
					String version = p.getVersion();
					String os = p.getOs();
					String ws = p.getWs();
					String arch = p.getArch();
					// String nl = p.getNl();

					if (p.isFragment()) {
						// nothing specific currently
					}

					if (p.isUnpack()) {
						// will need specific treatment
						System.out.println("might cause trouble on execution in bnd");
					}

					String filterStr = "";
					if (os != null) {
						filterStr = filterStr.concat("(osgi.os=" + os + ")");
					}
					if (ws != null) {
						filterStr = filterStr.concat("(osgi.ws=" + ws + ")");
					}
					if (arch != null) {
						filterStr = filterStr.concat("(osgi.arch=" + arch + ")");
					}

					if (filterStr.length() > 0) {
						if (filterStr.split("\\)\\(").length > 1) {
							filterExp = "(&" + filterStr + ")";
						} else {
							filterExp = filterStr;
						}
						try {
							Filter bundleFilter = FrameworkUtil.createFilter(filterExp);
							supportedOsgiConfigs.forEach((c, e) -> {
								if (bundleFilter.match(supportedOsgiConfigs.get(c))) {
									Set<String> bundles = fragments.get(c);
									if (bundles == null)
										bundles = new HashSet<>();
									if (versioned) {
										bundles.add(String.format(
												"    osgi.identity;filter:='(&(osgi.identity=%s)(version>=%s))',\\\n",
												bundleID, version));
									} else {
										bundles.add(String.format("    osgi.identity;filter:='(osgi.identity=%s)',\\\n",
												bundleID));
									}
								}
							});

						} catch (InvalidSyntaxException e) {
							e.printStackTrace();
						}
					} else {
						String bundleString = "";
						if (versioned) {
							bundleString = String.format(
									"    osgi.identity;filter:='(&(osgi.identity=%s)(version>=%s))',\\\n", bundleID,
									version);
						} else {
							bundleString = String.format("    osgi.identity;filter:='(osgi.identity=%s)',\\\n",
									bundleID);
						}
						plugins.append(bundleString);
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
