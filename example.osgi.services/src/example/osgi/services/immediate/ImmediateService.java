package example.osgi.services.immediate;

import java.util.Random;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true, service = ImmediateService.class)
public class ImmediateService {

	@Activate
	public void activate() {
		try {
			int randomNumber = new Random().ints(3, 10).findFirst().getAsInt();
			System.out.format("%s: %s: delay activation for %s seconds\n", Thread.currentThread(),
					ImmediateService.class.getSimpleName(), randomNumber);
			Thread.sleep(randomNumber * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.format("%s: %s: activated\n", Thread.currentThread(), ImmediateService.class.getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		System.out.format("%s: %s: deactivated\n", Thread.currentThread(), ImmediateService.class.getSimpleName());
	}

}
