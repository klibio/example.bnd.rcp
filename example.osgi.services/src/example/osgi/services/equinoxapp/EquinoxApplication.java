package example.osgi.services.equinoxapp;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import example.osgi.services.immediate.ImmediateService;

/**
 * This class controls all aspects of the application's execution
 */
public class EquinoxApplication implements IApplication {

	final private static int timeout = 10 * 1000;
	protected static volatile ImmediateService service = null;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
				.format(new Timestamp(System.currentTimeMillis()));
		System.out.format("%s: %s: headless equinox app launched at %s!\n", Thread.currentThread(),
				EquinoxApplication.class.getSimpleName(), timeStamp);
		Thread thread = new Thread() {
			int timer = 0;

			public void run() {
				while (service == null && timer < timeout) {
					System.out.format("%s: %s: waiting for ImmediateService since %s seconds!\n",
							Thread.currentThread(), EquinoxApplication.class.getSimpleName(), timer / 1000);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					timer = timer + 1000;
				}
				interrupt();
			}
		};
		thread.setName("wait-for-service");
		thread.run();
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
