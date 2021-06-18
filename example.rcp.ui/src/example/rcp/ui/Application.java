package example.rcp.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

public class Application implements IApplication {
	@Override
	public Object start(final IApplicationContext context) throws Exception {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final int totalSeconds = 3;
				monitor.beginTask("I am started from BND", totalSeconds);
				for (int i = 0; i < totalSeconds; i++) {
					monitor.worked(1);
					Thread.sleep(1000);
				}
			}
		};

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		dialog.run(true, false, runnable);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// noop
	}
}
