package io.klib.tools.ecl2bnd;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentSkipListSet;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.klib.tools.ecl2bnd.model.bnd.BndFeature;
import io.klib.tools.ecl2bnd.model.eclipse.Feature;

@Component
public class EclipseRepoParser {

	private boolean debug = Boolean.parseBoolean(System.getProperty("debug", "false"));

	private final static String SEP = System.getProperty("file.separator");

	private final static String resultDir = System.getProperty("user.dir") + SEP + "result";

	private final static String rootDir = "c:/jbe5.0.2/repo";
	private final static String ECL_PLATFORM_VERSION = "R-4.7.1a-201710090410";
	private final static String featureDir = rootDir + "/download.eclipse.org/eclipse/updates/4.7/"
			+ ECL_PLATFORM_VERSION + "/features";

	private JAXBContext ctxt;

	public EclipseRepoParser() {
		try {
			ctxt = JAXBContext.newInstance(Feature.class);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
	}

	@Activate
	public void activate() {
		Path featurePath = Paths.get(featureDir);
		new File(resultDir).mkdirs();

		ConcurrentSkipListSet<Feature> eclFeatures = new ConcurrentSkipListSet<>();
		ConcurrentSkipListSet<URL> urls = new ConcurrentSkipListSet<>();
		try {
			Files.walkFileTree(featurePath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".jar")) {
						try {
							urls.add(new URI("jar:" + file.toUri().toString() + "!/feature.xml").toURL());
						} catch (URISyntaxException uriEx) {
							uriEx.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (URL url : urls) {
			Feature feature = parseFeatureXml(url);
			eclFeatures.add(feature);
		}

		if (debug) {
			// outputs all parsed features as XML
			for (Feature fea : eclFeatures) {
				try {
					StringWriter writer = new StringWriter();
					Marshaller m = ctxt.createMarshaller();
					m.marshal(fea, writer);
					if (debug)
						System.out.println(writer.toString());
					writer.close();
				} catch (JAXBException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ConcurrentSkipListSet<BndFeature> features = buildModel(eclFeatures);
		for (BndFeature bndFeature : features) {
			System.out.println(bndFeature);
		}

		System.out.println("done.");
		try {
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundle(0).stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

	private Feature parseFeatureXml(URL url) {
		Feature eclFeature = null;
		try {
			Unmarshaller jaxbUnMarshaller = ctxt.createUnmarshaller();
			eclFeature = (Feature) jaxbUnMarshaller.unmarshal(url);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return eclFeature;
	}

	private ConcurrentSkipListSet<BndFeature> buildModel(ConcurrentSkipListSet<Feature> eclFeatures) {
		ConcurrentSkipListSet<BndFeature> bndFeatures = new ConcurrentSkipListSet<>();
		// transform eclipse to bnd features
		// mind that feature includes must be referenced later after all features are
		// available as bnd
		for (Feature eclFeature : eclFeatures) {
			System.out.println(eclFeature.getId());
			BndFeature e = new BndFeature(eclFeature);
			bndFeatures.add(e);
		}

		return bndFeatures;
	}

}
