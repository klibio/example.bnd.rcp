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

		if ((args == null) || !(args.length == 2 || args.length == 3))
			usage();
		if (args[0].equalsIgnoreCase(USAGE_UNSIGN)) {
			JarUnsigner.unsignJarFiles(args[1]);
		} else if (args[0].equalsIgnoreCase(USAGE_SIGN)) {
			Properties props = new Properties();
			try {
				props.load(new File(args[1]).toURI().toURL().openStream());
				JarSigner.signJarFiles(props, args[2]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args[0].equalsIgnoreCase(USAGE_VERIFY)) {
			JarSigner.verifyJarFiles(args[1]);
		} else
			usage();

		long durationInS = (System.currentTimeMillis() - start) / 1000;
		System.out.format("execution took %s seconds\n", durationInS);
	}

	private static boolean usage() {
		System.out.println("usage: jarsigner library\n" + "option 1: unsign <folder>\n"
				+ "option 2: sign <fileWithSigning.properties> <folder>\n" + "option 3: verify <folder>\n");

		return false;
	}

}
