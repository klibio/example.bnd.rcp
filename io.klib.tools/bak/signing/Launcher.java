package io.klib.tools.signing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

public class Launcher {

	private static final String USAGE_SIGN = "sign";
	private static final String USAGE_UNSIGN = "unsign";
	private static final String USAGE_VERIFY = "verify";

	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		if ((args == null) || !(args.length >= 2))
			usage();

		if (args[0].equalsIgnoreCase(USAGE_UNSIGN)) {
			File input = new File(args[1]);
			if (input.isDirectory()) {
				JarUnsigner.unsignFolder(args[1]);
			} else {
				if (input.getName().endsWith(JarSigner.JAR)) {
					JarUnsigner.unsignJarFile(args[1]);
				} else {
					JarUnsigner.unsignJarFiles(args[1]);
				}
			}
		} else if (args[0].equalsIgnoreCase(USAGE_SIGN)) {
			if (args.length == 3) {
				Properties props = readProvidedProperties(args);
				File input = new File(args[2]);
				if (input.isDirectory()) {
					JarSigner.signJarFolder(props, args[2]);
				} else {
					if (input.getName().endsWith(JarSigner.JAR)) {
						JarSigner.signJarFile(props, args[2]);
					} else {
						JarSigner.signJarFiles(props, args[2]);
					}
				}
			} else {
				Properties props = extractPropertiesFromSystemProperties(args);
				File input = new File(args[1]);
				if (input.isDirectory()) {
					JarSigner.signJarFolder(props, args[1]);
				} else {
					if (input.getName().endsWith(JarSigner.JAR)) {
						JarSigner.signJarFile(props, args[1]);
					} else {
						JarSigner.signJarFiles(props, args[1]);
					}
				}
			}
		} else if (args[0].equalsIgnoreCase(USAGE_VERIFY)) {
			if (new File(args[1]).isDirectory()) {
				JarSigner.verifyJarFiles(args[1]);
			} else {
				JarSigner.verifyJar(args[1]);
			}
		} else
			usage();

		long durationInS = (System.currentTimeMillis() - start) / 1000;
		System.out.format("execution took %s seconds\n", durationInS);
	}

	private static Properties extractPropertiesFromSystemProperties(String[] args) {
		Properties props = new Properties();
		props.put("KEYSTORE", System.getProperty("KEYSTORE"));
		props.put("STOREPASS", System.getProperty("STOREPASS"));
		props.put("KEYPASS", System.getProperty("KEYPASS"));
		props.put("ALIAS", System.getProperty("ALIAS"));
		if (System.getProperty("TSA") != null) {
			props.put("TSA", System.getProperty("TSA"));
		}
		if (System.getProperty("SIGFILE") != null) {
			props.put("SIGFILE", System.getProperty("SIGFILE"));
		}
		if (System.getProperty("VERBOSE") != null) {
			props.put("VERBOSE", System.getProperty("VERBOSE", "true"));
		}
		return props;
	}

	private static Properties readProvidedProperties(String[] args) {
		Properties props = new Properties();
		try {
			props.load(new File(args[1]).toURI().toURL().openStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	private static boolean usage() {
		System.out.println("usage: jarsigner library\n" + "option 1: unsign <folder>\n"
				+ "option 2: sign <fileWithSigning.properties> <folder>\n" + "option 3: verify <folder>\n");

		return false;
	}

}
