package io.klib.tools.parser.eclipse.features;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

	private Path path;

	private final Hashtable<String, String> config_WIN32_WIN32_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_MACOSX_COCOA_X86_64 = new Hashtable<String, String>();
	private final Hashtable<String, String> config_LINUX_GTK_X86_64 = new Hashtable<String, String>();

	TreeMap<String, TreeSet<String>> fragments = new TreeMap<>();
	TreeMap<String, Hashtable<String, String>> configs = new TreeMap<>();

	private boolean versioned;

	public OutputBndRequireFormat() {
		super();
		this.versioned = versioned;
		config_WIN32_WIN32_X86_64.put("osgi.os", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.ws", "win32");
		config_WIN32_WIN32_X86_64.put("osgi.arch", "x86_64");
		configs.put("win32.win32.x86-64", config_WIN32_WIN32_X86_64);

		config_MACOSX_COCOA_X86_64.put("osgi.os", "macosx");
		config_MACOSX_COCOA_X86_64.put("osgi.ws", "cocoa");
		config_MACOSX_COCOA_X86_64.put("osgi.arch", "x86_64");
		configs.put("macosx.cocoa.x86-64", config_MACOSX_COCOA_X86_64);

		config_LINUX_GTK_X86_64.put("osgi.os", "linux");
		config_LINUX_GTK_X86_64.put("osgi.ws", "gtk");
		config_LINUX_GTK_X86_64.put("osgi.arch", "x86_64");
		configs.put("linux.gtk.x86-64", config_LINUX_GTK_X86_64);
	}

	@Override
	public void execute(List<Feature> features, Path outputPath) {
		path = outputPath;
		appendBndRequireHeader(tocHeader);

		// pre-fill the supported platform configuration combinations
		for (Feature f : features) {
			// map OSGi LDAP filter to bundles/fragments
			fragments.put("win32.win32.x86-64", new TreeSet<String>());
			fragments.put("macosx.cocoa.x86-64", new TreeSet<String>());
			fragments.put("linux.gtk.x86-64", new TreeSet<String>());

			String featureID = f.getId();
			String featureVersion = f.getVersion();

			// bnd require

			if (versioned) {
				tocHeader.append(String.format("# ${%s%s_%s}\n", FEATURE_PREFIX, featureID, featureVersion));
				featureExpression.append(String.format("%s%s_%s: \\\n", FEATURE_PREFIX, featureID, featureVersion));
			} else {
				tocHeader.append(String.format("# ${%s%s}\n", FEATURE_PREFIX, featureID));
				featureExpression.append(String.format("%s%s: \\\n", FEATURE_PREFIX, featureID));
			}

			StringBuffer featureIncludesBndRequire = parseFeatureIncludeSection(f,versioned);
			StringBuffer featurePluginsBndRequire = parseFeaturePlugins(f, fragments);

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
			Set<Entry<String, TreeSet<String>>> entrySet = fragments.entrySet();
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
		System.out.println(featureExpression);

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
		List<Plugin> pluginList = f.getPlugins();
		StringBuffer plugins = new StringBuffer();

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
						String filterExp = "";
						if (filterStr.split("\\)\\(").length > 1) {
							filterExp = "(&" + filterStr + ")";
						} else {
							filterExp = filterStr;
						}
						try {
							Filter bundleFilter = FrameworkUtil.createFilter(filterExp);
							configs.forEach((c, e) -> {
								if (bundleFilter.match(configs.get(c))) {
									Set<String> bundles = fragments.get(c);
									bundles.add(String.format(
											"    osgi.identity;filter:='(&(osgi.identity=%s)(version>=%s))',\\\n",
											bundleID, version));
								}
							});

						} catch (InvalidSyntaxException e) {
							e.printStackTrace();
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
