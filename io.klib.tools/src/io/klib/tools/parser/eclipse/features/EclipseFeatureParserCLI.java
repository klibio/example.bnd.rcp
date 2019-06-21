package io.klib.tools.parser.eclipse.features;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class EclipseFeatureParserCLI {

	private static final String name = "io.klib.tools.parser.eclipse.features.EclipseFeatureFolderParser";
	private final static String SEP = System.getProperty("file.separator");
	private final static String resultDir = System.getProperty("user.dir") + SEP + "generated";
	private final static String FILENAME_BNDBUILD = "bnd_runrequires_Eclipse_Platform.bndrun";
	private final static String FILENAME_BNDREQUIRE = "bnd_buildpath_Eclipse_Platform.bndrun";

	private String[] launcherArguments;
	private String featureDir;

	@Reference(target = "(launcher.arguments=*)")
	void args(final Object object, final Map<String, Object> map) {
		launcherArguments = (String[]) map.get("launcher.arguments");
	}

	@Reference
	public ConfigurationAdmin configAdmin;

	public void activate() {
		if (launcherArguments != null && launcherArguments.length > 0) {

			String bndRequireString = "";
			String bndBuildpathString = "";

			if (launcherArguments.length == 1) {
				featureDir = launcherArguments[0];
				Path featurePath = Paths.get(featureDir);
				File featureFile = featurePath.toFile();
				if (!featureFile.exists() && featureFile.isDirectory() && featureFile.listFiles().length > 0) {
					System.out.format("specified feature directory %s does not exists!", featureDir);
					shutdownGraceful();
				}
				bndRequireString = Paths.get(resultDir, FILENAME_BNDBUILD).toString();
				bndBuildpathString = Paths.get(resultDir, FILENAME_BNDREQUIRE).toString();
			}

			if (launcherArguments.length == 3) {
				featureDir = launcherArguments[0];
				Path featurePath = Paths.get(featureDir);
				File featureFile = featurePath.toFile();
				if (!featureFile.exists() && featureFile.isDirectory() && featureFile.listFiles().length > 0) {
					System.out.format("specified feature directory %s does not exists!", featureDir);
					shutdownGraceful();
				}
				bndRequireString = launcherArguments[1];
				bndBuildpathString = launcherArguments[2];
			}

			try {
				Configuration config = configAdmin.getConfiguration(name, "?");
				Dictionary<String, Object> props = null;
				if (config != null && config.getProperties() != null) {
					props = config.getProperties();
				} else {
					props = new Hashtable<>();
				}
				props.put("featureDirectory", featureDir);
				props.put("bndBuildpathString", bndBuildpathString);
				props.put("bndRequireString", bndRequireString);
				config.update(props);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			usage();
			shutdownGraceful();
		}
	}

	public static void shutdownGraceful() {
		System.out.println("shutting down framework");
		try {
			FrameworkUtil.getBundle(EclipseFeatureParserCLI.class).getBundleContext().getBundle(0).stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	private void usage() {
		System.out.println("# Usage of this jar file via one of the following options");
		System.out.println("# java -jar io.klib.tools.parser.eclipse.features.jar <feature_directory>");
		System.out.println(
				"# java -jar io.klib.tools.parser.eclipse.features.jar <feature_directory> <bndrun require file> <bnd buildpath file>");
	}

}
