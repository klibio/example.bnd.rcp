import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileDownloader {

	public static void main(String[] args) {
		String[] files = new String[] { "p2.index", "content.xml.xz", "content.jar", "artifacts.xml.xz","artifacts.jar" };
		String fileUrlRoot = "https://download.eclipse.org/tools/ajdt/423/dev/update/ajdt-e423-2.2.4.202304111532/";
		String localFolderPath = "X:/IDEfix/klibio-example-bnd-rcp/git/example.bnd.rcp/io.klib.app.p2.mirror/repo/download.eclipse.org/tools/ajdt/423/dev/update/ajdt-e423-2.2.4.202304111532/";

		try {
			for (int i = 0; i < files.length; i++) {
				String fileUrl = fileUrlRoot+files[i];
				URL url = new URL(fileUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("HEAD");

				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					// File exists, download it
					String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
					String filePath = localFolderPath + fileName;

					Path destination = Path.of(filePath);
					Files.copy(url.openStream(), destination, StandardCopyOption.REPLACE_EXISTING);

					System.out.println("File downloaded successfully: " + filePath);
				} else {
					// File does not exist
					System.out.println("File not found: " + fileUrl);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
