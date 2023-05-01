package example.rcp.args;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class ServiceA {

	@Activate
	void activate(BundleContext bundleContext) {
		// TODO Auto-generated method stub
	}

	@Deactivate
	void deactivate() {
		// TODO Auto-generated method stub
	}

}
