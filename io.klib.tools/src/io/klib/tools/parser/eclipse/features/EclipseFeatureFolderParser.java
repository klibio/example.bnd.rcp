package io.klib.tools.parser.eclipse.features;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;

@Component
public class EclipseFeatureFolderParser {

	private boolean debug = false;
	private String[] launcherArguments;

	private final static String SEP = System.getProperty("file.separator");
	private final static String resultDir = System.getProperty("user.dir") + SEP + "result";

	private String bndRequireFile;
	private String bndBuildPathFile;
	private String featureDir;
	
    @Reference(target = "(launcher.arguments=*)")
    void args(final Object object, final Map<String, Object> map) {
        launcherArguments = (String[]) map.get("launcher.arguments");
	}

	@Activate
	public void activate() {

		bndRequireFile = launcherArguments[0];
		bndBuildPathFile= launcherArguments[1];
		featureDir= launcherArguments[2];
		
		Path featurePath = Paths.get(featureDir);
		new File(resultDir).mkdirs();

		List<Feature> eclFeatures = new LinkedList<Feature>();

		List<Path> featureJars = collectFeatures(featurePath);
		for (Path path : featureJars) {
			try {
				URI uri = new URI("jar:" + path.toUri().toString());
				eclFeatures.addAll(parseFeatureJar(uri));
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			} catch (URISyntaxException uriEx) {
				uriEx.printStackTrace();
			}
		}

		OutputContext outputContext = new OutputContext();

		outputContext.setStrategy(new OutputBndBuildFormat());
		outputContext.execute(eclFeatures, Paths.get(resultDir, bndBuildPathFile));

		outputContext.setStrategy(new OutputBndRequireFormat());
		outputContext.execute(eclFeatures, Paths.get(resultDir, bndRequireFile));

		if (debug) {
			// outputs all parsed features as XML
			for (Path path : featureJars) {
				try {
					StringWriter writer = new StringWriter();
					JAXBContext context = JAXBContext.newInstance(Feature.class);
					Marshaller m = context.createMarshaller();
					m.marshal(path, writer);
					System.out.println(writer.toString());
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("done - shutting down framework");
		try {
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundle(0).stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	private List<Path> collectFeatures(Path p) {
		List<Path> featureJars = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(p, "*.jar")) {
			for (Path entry : stream) {
				featureJars.add(entry);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return featureJars;
	}

	private List<Feature> parseFeatureJar(URI uri) throws IOException {
		List<Feature> eclFeatures = new LinkedList<Feature>();
		FileSystem zipFs = FileSystems.newFileSystem(uri, Collections.emptyMap());
		zipFs.getRootDirectories().forEach(root -> {
			try {
				Files.walk(root).forEach(path -> {
					if (path.toString().equals("/feature.xml")) {
						try {
							String content = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
							eclFeatures.add(parseFeatureXml(content));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return eclFeatures;
	}

	private Feature parseFeatureXml(String content) {
		Feature eclFeature = null;
		try {
			JAXBContext ctxt = JAXBContext.newInstance(Feature.class);
			Unmarshaller jaxbUnMarshaller = ctxt.createUnmarshaller();
			StringReader reader = new StringReader(content);
			eclFeature = (Feature) jaxbUnMarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return eclFeature;
	}

}
