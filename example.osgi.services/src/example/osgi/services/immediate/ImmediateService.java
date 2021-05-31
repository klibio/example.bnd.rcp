package example.osgi.services.immediate;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = ImmediateService.class)
public class ImmediateService {

	@Activate
	public void activate() {
		System.out.format("%s:%s waiting for activation\n", Thread.currentThread(),
				ImmediateService.class.getSimpleName());
		try {
			Thread.sleep(4 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.format("%s:%s activated\n", Thread.currentThread(), ImmediateService.class.getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		System.out.format("%s:%s deactivated\n", Thread.currentThread(), ImmediateService.class.getSimpleName());
	}

}
