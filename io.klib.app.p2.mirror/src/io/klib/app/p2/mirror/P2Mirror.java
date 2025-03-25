package io.klib.app.p2.mirror;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.internal.repository.tools.MirrorApplication;
import org.eclipse.equinox.p2.internal.repository.tools.RepositoryDescriptor;
import org.eclipse.equinox.p2.internal.repository.tools.SlicingOptions;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class P2Mirror {


	@Reference
	private IProvisioningAgentProvider agentProvider;

	private IProvisioningAgent agent;
	@SuppressWarnings("unused")
	private IArtifactRepositoryManager aRepoMgr;
	@SuppressWarnings("unused")
	private IMetadataRepositoryManager mRepoMgr;

	@SuppressWarnings("unused")
	private static final String FEATURE_GROUP = ".feature.group";
	
	private static final String USER_DIR = System.getProperty("user.dir").replace("\\", "/");
	private static final String LOCAL_ROOT_URI = USER_DIR + "/repo/";
	
//	private String url = "https://download.eclipse.org/nebula/releases/latest/";
//	private String url = "https://download.eclipse.org/eclipse/updates/4.25/";
	// only metadata repo - breaking current implementation
//	private String url = "https://download.eclipse.org/eclipse/updates/4.25/categories/";
//	private String url = "https://download.eclipse.org/releases/2022-09/202209141001/";
//	private String url = "https://download.eclipse.org/oomph/drops/release/1.27.0/";
//	private String url = "https://download.eclipse.org/nebula/releases/latest";
//	private String url = "https://download.eclipse.org/nebula/releases/3.0.0/";
//	private String url = "file:Z:/ENGINE_LIB_DIR/repo/download.eclipse.org/eclipse/updates/4.26/R-4.26-202211231800/";
//	private String url = "jar:https://bndtools.jfrog.io/bndtools/libs-release-local/org/bndtools/org.bndtools.p2/6.4.0/org.bndtools.p2-6.4.0.jar!/";
	
//	private String url = "file:X:/_cec/_bldWork/babel-R0.20.0_2022-12/2022-12";
//	private String url = "https://download.eclipse.org/tools/ajdt/423/dev/update/ajdt-e423-2.2.4.202304111532/";
//	private String url = "https://download.eclipse.org/tools/ajdt/426/dev/update/ajdt-e426-2.2.4.202304111527/";
//	private String url = "https://de-jcup.github.io/update-site-eclipse-bash-editor/update-site/";
//	private String url = "https://download.eclipse.org/releases/2023-03/202303151000/";
//	private String url = "download.eclipse.org/technology/epp/packages/2023-03/202303091200/";

//	private String url = "https://download.eclipse.org/eclipse/updates/4.35/R-4.35-202502280140/";
//	private String url = "https://download.eclipse.org/releases/2025-03/202503121000/";

//	private String url = "https://download.eclipse.org/eclipse/updates/4.34/R-4.34-202411201800/";
//	private String url = "https://download.eclipse.org/releases/2024-12/202412041000/";

	private String url = "https://download.eclipse.org/eclipse/updates/4.33/R-4.33-202409030240/";
//	private String url = "https://download.eclipse.org/releases/2024-09/202409111000/";
	
	private String suffix = url;
	private String localUri;

	public void activate(BundleContext ctx) throws Exception {
		System.out.println("started");

		agent = agentProvider.createAgent(new URI("file:/" + USER_DIR + "/p2"));
		ctx.registerService(IProvisioningAgent.class, agent, null);

		mRepoMgr = getMetadataRepositoryManager();
		aRepoMgr = getArtifactRepositoryManager();

		MirrorApplication mirrorApplication = new MirrorApplication();
		RepositoryDescriptor srcRepoDesc = new RepositoryDescriptor();

		if (url.startsWith("file:")) {
			suffix = url.toString().replaceFirst("file:", "").replaceFirst(":", "_");
		} else {
			suffix = url.toString().replaceFirst(".*?:", "").replaceAll("//", "/");
		}
		if (url.toString().endsWith("!/")) {
			suffix = suffix.toString().replaceAll(".jar!/", "_jar").replaceFirst(".*/", "");
		}

		srcRepoDesc.setLocation(new URI(url));
		mirrorApplication.addSource(srcRepoDesc);

		localUri = LOCAL_ROOT_URI + suffix;
		File targetLocalStorage = new File(localUri);
		targetLocalStorage.mkdirs();
		String name = "x";
		// create metadata repository
		RepositoryDescriptor destMetadataRepoDesc = createTargetRepoDesc(targetLocalStorage, name,
				RepositoryDescriptor.KIND_METADATA);
		mirrorApplication.addDestination(destMetadataRepoDesc);

		// create artifact repository
		RepositoryDescriptor destArtifactRepoDesc = createTargetRepoDesc(targetLocalStorage, name,
				RepositoryDescriptor.KIND_ARTIFACT);
		destArtifactRepoDesc.setCompressed(true);
		destArtifactRepoDesc.setFormat(new URI("file:///Z:/ENGINE_LIB_DIR/cec/p2_repo_packedSiblings"));
		mirrorApplication.addDestination(destArtifactRepoDesc);
		mirrorApplication.setRaw(true);
		mirrorApplication.setVerbose(true);

		SlicingOptions sliceOpts = new SlicingOptions();
//		sliceOpts.latestVersionOnly(true);
//		sliceOpts.considerStrictDependencyOnly(false);
//		sliceOpts.followOnlyFilteredRequirements(false);
//		sliceOpts.includeOptionalDependencies(false);
//		sliceOpts.latestVersionOnly(false);
		mirrorApplication.setSlicingOptions(sliceOpts);

		/* 
		 * List<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
		 * InstallableUnit iu = new InstallableUnit();
		 * iu.setId("org.eclipse.nebula.widgets.paperclips.feature"+FEATURE_GROUP);
		 * iu.setVersion(Version.create("0.0.0")); ius.add(iu);
		 * 
		 * iu = new InstallableUnit();
		 * iu.setId("org.eclipse.nebula.paperclips.widgets");
		 * iu.setVersion(Version.create("0.0.0")); ius.add(iu);
		 * 
		 * mirrorApplication.setSourceIUs(ius);
		 */
		mirrorApplication.run(new NullProgressMonitor());
		
		downloadMetadata();

		System.out.println("finished");
	}

	private RepositoryDescriptor createTargetRepoDesc(final File targetLocation, final String name, final String kind) {
		RepositoryDescriptor destRepoDesc = new RepositoryDescriptor();
		destRepoDesc.setKind(kind);
		destRepoDesc.setCompressed(true);
		destRepoDesc.setLocation(targetLocation.toURI());
		destRepoDesc.setName(name);
		destRepoDesc.setAtomic("true"); // what is this for?^
		// destination.setFormat(sourceLocation); // can be used to define a target
		// format based on existing repository
		return destRepoDesc;
	}

	public IMetadataRepositoryManager getMetadataRepositoryManager() {
		IMetadataRepositoryManager repoMgr = (IMetadataRepositoryManager) agent
				.getService(IMetadataRepositoryManager.SERVICE_NAME);

		if (repoMgr == null) {
			throw new IllegalStateException();
		}

		return repoMgr;
	}

	public IArtifactRepositoryManager getArtifactRepositoryManager() {
		IArtifactRepositoryManager repoMgr = (IArtifactRepositoryManager) agent
				.getService(IArtifactRepositoryManager.SERVICE_NAME);

		if (repoMgr == null) {
			throw new IllegalStateException();
		}

		return repoMgr;
	}

	private void downloadMetadata() {
		String[] files = new String[] { "p2.index", "content.xml.xz", "content.jar", "artifacts.xml.xz",
				"artifacts.jar" };

		try {
			for (int i = 0; i < files.length; i++) {
				String fileUrl = url + files[i];
				URL url = new URL(fileUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("HEAD");

				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					// File exists, download it
					String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
					String filePath = localUri + fileName;

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
