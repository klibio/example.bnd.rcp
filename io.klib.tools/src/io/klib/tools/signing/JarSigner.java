package io.klib.tools.signing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * JarSigner - signing jar files
 * 
 * https://docs.oracle.com/javase/tutorial/deployment/jar/signing.html
 * 
 * @author Peter Kirschner
 *
 */
public class JarSigner {

	public static final String SIGNEDJAR = "-signedjar";
	public static final String KEYSTORE = "-keystore";
	public static final String STOREPASS = "-storepass";
	public static final String KEYPASS = "-keypass";
	public static final String ALIAS = "alias";
	public static final String SIGFILE = "-sigfile";
	public static final String TSA = "-tsa";
	public static final String VERIFY = "-verify";
	public static final String VERBOSE = "-verbose";
	public static final String CERTS = "-certs";

	private static final String JAR = "jar";

	public static boolean signed = false;
	public static String output = "";

	public static void signJarFiles(Properties config, String rootFolder) {
		try {
			Files.find(Paths.get(rootFolder), Integer.MAX_VALUE,
					(filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(JAR))
					.forEach(f -> {
						try {
							Path sourceFile = f.toAbsolutePath();
							Path tempFile = Files.createTempFile("sign", "");
							Files.move(sourceFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
							signJar(tempFile.toString(), sourceFile.toString(), config);
							tempFile.toFile().delete();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void signJarFile(Properties config, String jarFile) {
		try {
			Path sourceFile = Paths.get(jarFile).toAbsolutePath();
			Path tempFile = Files.createTempFile("sign", "");
			Files.move(sourceFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
			signJar(tempFile.toString(), sourceFile.toString(), config);
			tempFile.toFile().delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void signJar(String unsignedJar, String signedJar, Properties config) {
		try {
			System.out.format("signing jarfile %s", signedJar);
			PrintStream stdout = System.out;
			Path signedPath = Paths.get(signedJar);
			Path unsignedPath = Paths.get(unsignedJar);
			Path logPath = unsignedPath.resolveSibling("sign_" + signedPath.getFileName() + ".log");
			PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logPath.toFile())),
					true);
			System.setOut(printStream);
			System.setErr(printStream);

			List<String> args = new LinkedList<String>();
			args.add(KEYSTORE);
			args.add(config.get("KEYSTORE").toString());
			args.add(STOREPASS);
			args.add(config.get("STOREPASS").toString());
			args.add(KEYPASS);
			args.add(config.get("KEYPASS").toString());
			if (config.containsKey("TSA")) {
				args.add(TSA);
				args.add(config.get("TSA").toString());
			}
			if (config.containsKey("SIGFILE")) {
				args.add(SIGFILE);
				args.add(config.get("SIGFILE").toString());
			}
			if (config.containsKey("VERBOSE")) {
				args.add(VERBOSE);
			}
			args.add(SIGNEDJAR);
			args.add(signedJar);
			args.add(unsignedJar);
			args.add(config.get("ALIAS").toString());
			sun.security.tools.jarsigner.Main.main(args.toArray(new String[args.size()]));
			printStream.close();
			System.setOut(stdout);
			String result = " - failed!\n";
			try {
				Scanner scanner = new Scanner(logPath.toFile());
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.contains("jar signed.")) {
						result = " - successful";
					}
					if (line.contains("timestamp will expire")) {
						result = result + " - " + line;
					}
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.format(result + "\n");
			if (!config.containsKey("VERBOSE")) {
				logPath.toFile().deleteOnExit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void verifyJarFiles(String rootFolder) {
		try {
			Files.find(Paths.get(rootFolder), Integer.MAX_VALUE,
					(filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(JAR))
					.forEach(f -> {
						verifyJar(f.toString());
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void verifyJarFile(String jarFile) {
		verifyJar(jarFile);
	}

	private static void verifyJar(String jar) {
		try {
			System.out.format("verify jarfile %s", jar);
			PrintStream stdout = System.out;
			Path signedPath = Paths.get(jar);
			Path logPath = signedPath.resolveSibling("sign_" + signedPath.getFileName() + ".log");
			PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logPath.toFile())),
					true);
			System.setOut(printStream);
			System.setErr(printStream);

			List<String> args = new LinkedList<String>();
			args.add(VERIFY);
			args.add(VERBOSE);
			args.add(CERTS);
			args.add(jar);
			sun.security.tools.jarsigner.Main.main(args.toArray(new String[args.size()]));
			printStream.close();
			System.setOut(stdout);
			String result = " - failed!\n";
			try {
				Scanner scanner = new Scanner(logPath.toFile());
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.contains("jar verified.")) {
						result = " - successful";
					}
					if (line.contains("self-signed")) {
						result = result + " - self-signed entries contained";
					}
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.format(result + "\n");
			logPath.toFile().deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
