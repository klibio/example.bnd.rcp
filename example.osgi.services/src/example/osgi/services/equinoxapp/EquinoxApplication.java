package example.osgi.services.equinoxapp;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import example.osgi.services.immediate.ImmediateService;

/**
 * This class controls all aspects of the application's execution
 */
public class EquinoxApplication implements IApplication {

	final private static int timeout = 15 * 1000;
	protected static volatile ImmediateService service = null;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
				.format(new Timestamp(System.currentTimeMillis()));
		System.out.format("%s: %s: headless equinox app launched at %s!\n", Thread.currentThread(),
				EquinoxApplication.class.getSimpleName(), timeStamp);

		BundleContext bundleContext = FrameworkUtil.getBundle(EquinoxApplication.class).getBundleContext();
		ServiceTracker<ImmediateService, ImmediateService> serviceTracker = new ServiceTracker<ImmediateService, ImmediateService>(
				bundleContext, ImmediateService.class, null);
		serviceTracker.open(true);
		service = serviceTracker.waitForService(timeout);
		if (service == null) {
			System.out.format("%s: %s: timeout occured - service not found!\n", Thread.currentThread(),
					EquinoxApplication.class.getSimpleName());
		} else {
			System.out.format("%s: %s: service %s found!\n", Thread.currentThread(),
					EquinoxApplication.class.getSimpleName(), ImmediateService.class);
		}
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
