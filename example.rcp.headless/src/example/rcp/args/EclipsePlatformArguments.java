package example.rcp.args;

import java.util.Arrays;

import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class EclipsePlatformArguments {

	@Reference
	EnvironmentInfo envInfo;

	@Activate
	void activate(BundleContext bc) {
		System.out.println("XYZ: start");
		ServiceReference<?> infoRef = bc.getServiceReference(EnvironmentInfo.class.getName());
		if (infoRef == null) {
			System.out.println(" service not available " + EnvironmentInfo.class);
		} else {

			envInfo = (EnvironmentInfo) bc.getService(infoRef);
			if (envInfo == null) {
				System.out.println("no " + EnvironmentInfo.class);
			} else {
				System.out.println("cmd arguments:");
				String[] commandLineArgs = envInfo.getCommandLineArgs();
				if (commandLineArgs != null && commandLineArgs.length > 0) {
					Arrays.asList(commandLineArgs).forEach(s -> s.toString());
				} else {
					System.out.println("NO ARGS");
				}
				System.out.println("fwk arguments:");
				String[] fwkArgs = envInfo.getFrameworkArgs();
				if (fwkArgs != null && fwkArgs.length > 0) {
					Arrays.asList(fwkArgs).forEach(s -> s.toString());
				} else {
					System.out.println("NO ARGS");
				}
				System.out.println("fwk arguments:");
				String[] nofwkArgs = envInfo.getFrameworkArgs();
				if (nofwkArgs != null && nofwkArgs.length > 0) {
					Arrays.asList(nofwkArgs).forEach(s -> s.toString());
				} else {
					System.out.println("NO ARGS");
				}
			}
		}
	}

	@Deactivate
	void deactivate() {
		System.out.println("XYZ: start");
	}

}
