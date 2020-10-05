package io.klib.tools.signing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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

	private static final String META_INF = "META-INF";
	private static final String MANIFEST_MF = "MANIFEST.MF";

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

	public static final String JAR = "jar";

	public static boolean signed = false;
	public static String output = "";

	public static void signJarFolder(Properties config, String rootFolder) {
		try {
			long numFiles = Files
					.find(Paths.get(rootFolder), Integer.MAX_VALUE,
							(filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(JAR))
					.count();

			Files.find(Paths.get(rootFolder), Integer.MAX_VALUE,
					(filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(JAR)).parallel()
					.forEach(f -> {
						try {
							Path sourceFile = f.toAbsolutePath();
							if (containsManifest(sourceFile)) {
								Path tempFile = Files.createTempFile("sign", "");
								Files.move(sourceFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
								signJar(tempFile.toString(), sourceFile.toString(), config);
								tempFile.toFile().delete();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void signJarFiles(Properties config, String filelist) {
		try {
			Scanner scanner = new Scanner(new File(filelist));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				try {
					Path sourceFile = Paths.get(line).toAbsolutePath();
					Path tempFile = Files.createTempFile("sign", "");
					Files.move(sourceFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
					signJar(tempFile.toString(), sourceFile.toString(), config);
					tempFile.toFile().delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
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
			/*
			 * Path signedPath = Paths.get(signedJar); Path unsignedPath =
			 * Paths.get(unsignedJar); PrintStream stdout = System.out; Path logPath =
			 * unsignedPath.resolveSibling("sign_" + signedPath.getFileName() + ".log");
			 * PrintStream printStream = new PrintStream(new BufferedOutputStream(new
			 * FileOutputStream(logPath.toFile())), true); System.setOut(printStream);
			 * System.setErr(printStream);
			 */
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
			/*
			 * printStream.close(); System.setOut(stdout); String result = " - failed!"; try
			 * { Scanner scanner = new Scanner(logPath.toFile()); while
			 * (scanner.hasNextLine()) { String line = scanner.nextLine(); if
			 * (line.contains("jar signed.")) { result = " - successful"; } if
			 * (line.contains("timestamp will expire")) { result = result + " - " + line; }
			 * } scanner.close(); } catch (FileNotFoundException e) { e.printStackTrace(); }
			 * System.out.format("\nsigning jarfile %s %s", signedJar, result); if
			 * (!config.containsKey("VERBOSE")) { logPath.toFile().deleteOnExit(); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void verifyJarFiles(String rootFolder) {
		try {
			Files.find(Paths.get(rootFolder), Integer.MAX_VALUE,
					(filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(JAR))
					.forEach(f -> {
						if (containsManifest(f)) {
							verifyJar(f.toString());
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean verifyJar(String jar) {
		boolean rv = false;
		try {
			System.out.format("verify jarfile %s", jar);
			PrintStream stdout = System.out;
			Path signedPath = Paths.get(jar);
			if (containsManifest(signedPath)) {

				Path logPath = signedPath.resolveSibling("sign_" + signedPath.getFileName() + ".log");
				PrintStream printStream = new PrintStream(
						new BufferedOutputStream(new FileOutputStream(logPath.toFile())), true);
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
				String result = " - failed!";
				try {
					Scanner scanner = new Scanner(logPath.toFile());
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						if (line.contains("jar verified.")) {
							result = " - successful";
							rv = true;
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}

	private static boolean containsManifest(Path path) {
		boolean rv = false;
		FileSystem fs;
		try {
			fs = FileSystems.newFileSystem(path, null);
			Path manifestFile = fs.getPath(META_INF, MANIFEST_MF);
			if (Files.exists(manifestFile)) {
				rv = true;
			} else {
				System.out.format(" - skipping provided jar file %s does not contain file %s/%s\n", path, META_INF,
						MANIFEST_MF);
			}
		} catch (IOException e) {
			// nothing to do
		}
		return rv;
	}
}
