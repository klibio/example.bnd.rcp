package io.klib.transform.xslt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XSLTransform {

	private final static String USERDIR = System.getProperty("user.dir");
	private final static String SEP = System.getProperty("file.separator");
	private final boolean isBackup = Boolean.parseBoolean(System.getProperty("backup", "false"));
	private String nameWithoutExt;

	/**
	 * 
	 * @param args
	 *            jar or xml file to process
	 */
	private void run(String[] args) {
		String xsltFile = SEP + "xslt" + SEP + "transform.xslt";
		Path tmpDir = null;
		try {
			tmpDir = Files.createTempDirectory("_transformXSLT");
			tmpDir.toFile().deleteOnExit();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		File inputFile = new File(args[0]);
		if ((inputFile == null) || !inputFile.exists() || !inputFile.isFile()) {
			System.out.format("provided file %s does not exist", args[0]);
			System.exit(-1);
		}

		InputStream xsltInputStream = null;
		if (args.length > 1) {
			try {
				xsltInputStream = new FileInputStream(new File(args[1]));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			xsltInputStream = XSLTransform.class.getResourceAsStream(xsltFile.replace("\\", "/"));
			if (xsltInputStream == null) {
				try {
					xsltInputStream = new FileInputStream(new File(USERDIR + xsltFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		Path backupFile = inputFile.toPath().resolveSibling("BAK_" + inputFile.getName());
		if (isBackup) {
			backup(inputFile, backupFile);
		}

		nameWithoutExt = inputFile.getName().replaceAll("\\..*", "");
		if (inputFile.getName().endsWith(".jar")) {
			System.out.println("extracting file to    " + tmpDir);
			unzip(inputFile, tmpDir);

			Path extractedFile;
			Path transformSource;
			extractedFile = tmpDir.resolve(nameWithoutExt + ".xml");
			transformSource = extractedFile.resolveSibling(nameWithoutExt + "Source.xml");
			try {
				Files.move(extractedFile, transformSource);
			} catch (IOException e) {
				e.printStackTrace();
			}

			TransformerFactory factory = TransformerFactory.newInstance();
			Source xslt = new StreamSource(xsltInputStream);
			try {
				Transformer transformer = factory.newTransformer(xslt);
				Source text = new StreamSource(transformSource.toFile());
				StreamResult outputTarget = new StreamResult(extractedFile.toFile());
				System.out.println("transforming file     " + extractedFile.toFile());
				transformer.transform(text, outputTarget);
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			try {
				Files.delete(inputFile.toPath());
				Files.delete(transformSource);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Path contentJar = inputFile.toPath().resolveSibling(nameWithoutExt + ".jar");
			try {
				zipFolder(tmpDir.toFile(), contentJar.toFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("processed result file " + contentJar);
		}
	}

	private void backup(File inputFile, Path backupFile) {
		try {
			System.out.println("create a backup of    " + backupFile);
			Files.copy(inputFile.toPath(), backupFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void unzip(File jarFile, Path destination) {
		JarFile jar = null;

		try {
			jar = new JarFile(jarFile);

			Enumeration<?> enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
				JarEntry file = (JarEntry) enumEntries.nextElement();

				if (file.getName().equals("artifacts.xml")) {
					Files.createDirectories(destination);
					String filePathName = destination + File.separator + file.getName();
					File f = new File(filePathName);
					if (file.isDirectory()) {
						f.mkdir();
						continue;
					}
					InputStream is = jar.getInputStream(file);

					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(f));

					while (is.available() > 0) {
						fos.write(is.read());
					}
					is.close();
					fos.close();
				} else {
					continue;
				}
			}
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void zipFolder(File srcFolder, File destZipFile) throws Exception {
		try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
				ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

			addFolderToZip(srcFolder, srcFolder, zip);
		}
	}

	private void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {

		if (srcFile.isDirectory()) {
			addFolderToZip(rootPath, srcFile, zip);
		} else {
			byte[] buf = new byte[1024 * 32];
			int len;
			try (FileInputStream in = new FileInputStream(srcFile)) {
				String name = srcFile.getName();
				zip.putNextEntry(new ZipEntry(name));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			}
		}
	}

	private void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
		for (File fileName : srcFolder.listFiles()) {
			addFileToZip(rootPath, fileName, zip);
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException, TransformerException {
		new XSLTransform().run(args);
	}

}