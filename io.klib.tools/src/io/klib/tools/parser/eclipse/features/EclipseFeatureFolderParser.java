package io.klib.tools.parser.eclipse.features;

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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE)
public class EclipseFeatureFolderParser {

	private boolean debug = false;

	@interface Config {
		String featureDirectory();

		String bndBuildpathString();

		String bndRequireString();
	}

	@Activate
	public void activate(Config config) {

		System.out.format("\n# Launching Eclipse Feature Parser inside directory %s\n", config.featureDirectory());
		System.out.format("  creating the bnd build requirement file %s\n", config.bndBuildpathString());
		System.out.format("  creating the bnd run requirement file   %s\n\n\n", config.bndRequireString());

		List<Feature> eclFeatures = new LinkedList<Feature>();

		List<Path> featureJars = collectFeatures(Paths.get(config.featureDirectory()));
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
		Path buildpathPath = Paths.get(config.bndBuildpathString());
		buildpathPath.toFile().getParentFile().mkdirs();
		outputContext.execute(eclFeatures, buildpathPath);

		outputContext.setStrategy(new OutputBndRequireFormat());
		Path bndRequirePath = Paths.get(config.bndRequireString());
		bndRequirePath.toFile().getParentFile().mkdirs();
		outputContext.execute(eclFeatures, bndRequirePath);

		if (debug) {
			System.out.println("DEBUG Output: outputs all parsed features as XML");
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

		EclipseFeatureParserCLI.shutdownGraceful();
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
