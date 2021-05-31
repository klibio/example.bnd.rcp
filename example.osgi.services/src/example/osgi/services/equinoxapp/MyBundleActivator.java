package example.osgi.services.equinoxapp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import example.osgi.services.immediate.ImmediateService;

public class MyBundleActivator implements BundleActivator {

	private ServiceTracker<ImmediateService, ImmediateService> serviceTracker;

	@Override
	public void start(BundleContext context) throws Exception {
		if (serviceTracker == null) {
			System.out.format("%s,%s: creating service tracker for %s\n", Thread.currentThread(),
					MyBundleActivator.class.getSimpleName(), ImmediateService.class);
			serviceTracker = new ServiceTracker<ImmediateService, ImmediateService>(context, ImmediateService.class,
					null) {
				@Override
				public ImmediateService addingService(ServiceReference<ImmediateService> reference) {
					ImmediateService result = super.addingService(reference);

					// The required service has become available, so we should
					// start our service if it hasn't been started yet.
					if (EquinoxApplication.service == null) {
						System.out.format("%s,%s: service %s available\n", Thread.currentThread(),
								MyBundleActivator.class.getSimpleName(), ImmediateService.class);
						EquinoxApplication.service = result;
					}
					return result;
				}

				@Override
				public void removedService(ServiceReference<ImmediateService> reference, ImmediateService service) {
					super.removedService(reference, service);
				}
			};
		}
		// Now activate (open) the service tracker.
		serviceTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
