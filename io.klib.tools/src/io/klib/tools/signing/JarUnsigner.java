package io.klib.tools.signing;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class JarUnsigner {

	private static final String JAR = "jar";
	private static final String MANIFEST = "META-INF/MANIFEST.MF";

	public static void main(String... args) {
		long start = System.currentTimeMillis();
		if (args != null) {
			for (String folder : args) {
				unsignJarFiles(folder);
			}
		}
		long durationInS = (System.currentTimeMillis() - start) / 1000;
		System.out.format("execution took %s seconds\n", durationInS);
	}

	public static void unsignJarFiles(String rootFolder) {

		try {
			Files.find(Paths.get(rootFolder), Integer.MAX_VALUE,
					(filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(JAR))
					.forEach(f -> {
						try {
							Path sourceFile = f.toAbsolutePath();
							Path tempFile = Files.createTempFile("unsign", "");
							Files.move(sourceFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
							unsignJar(tempFile.toString(), sourceFile.toString());
							tempFile.toFile().delete();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void unsignJar(String infile, String outfile) {
		try {
			ZipFile zipFile = new ZipFile(infile);
			final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outfile));
			for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
				ZipEntry entryIn = (ZipEntry) e.nextElement();

				if (!exclude_file(entryIn.getName())) {

					/* copy the entry as-is */
					zos.putNextEntry(new ZipEntry(entryIn.getName()));
					InputStream is = zipFile.getInputStream(entryIn);
					byte[] buf = new byte[1024];
					int len;
					while ((len = (is.read(buf))) > 0) {
						zos.write(buf, 0, len);
					}
					zos.closeEntry();

				} else {

					if (MANIFEST.equals(entryIn.getName())) {
						/* if MANIFEST, adjust the entry */
						zos.putNextEntry(new ZipEntry(MANIFEST));

						// manifest entries until first empty line. i.e. the 'MainAttributes' section
						// (this method is used so to keep the formatting exactly the same)
						InputStream mIS = zipFile.getInputStream(entryIn);
						BufferedReader in = new BufferedReader(new InputStreamReader(mIS));
						String line = in.readLine();
						byte[] mNL = "\n".getBytes("UTF-8");
						while (line != null && !line.trim().isEmpty()) {
							zos.write(line.getBytes("UTF-8"));
							zos.write(mNL);
							line = in.readLine();
						}
						zos.write(mNL);
						zos.closeEntry();

					} else {
						/* else: Leave out the Signature files */
					}

				}

			}
			zos.close();
			zipFile.close();
			System.out.println("Successfully unsigned " + outfile);

		} catch (IOException ex) {
			System.err.println("Error for file: " + infile);
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Exclude .SF signature file Exclude .RSA and DSA (signed version of .SF file)
	 * Exclude SIG- files (unknown sign types for signed .SF file) Exclude Manifest
	 * file
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean exclude_file(String filename) {
		return filename.equals("META-INF/MANIFEST.MF") || filename.startsWith("META-INF/SIG-")
				|| filename.startsWith("META-INF/")
						&& (filename.endsWith(".SF") || filename.endsWith(".RSA") || filename.endsWith(".DSA"));
	}

}