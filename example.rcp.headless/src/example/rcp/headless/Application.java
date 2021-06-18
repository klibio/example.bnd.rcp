package example.rcp.headless;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
				.format(new Timestamp(System.currentTimeMillis()));
		System.out.format("RCP headless app launched at %s!\n", timeStamp);
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
