package bndtools.runtime.applaunch.eclipse4.test;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class ExampleIntegrationTest {

	private final BundleContext context = FrameworkUtil.getBundle(ExampleIntegrationTest.class).getBundleContext();

	@BeforeAll
	public void before() {
		Assertions.assertNotNull(context);
	}

	@AfterAll
	public void after() {
		Assertions.assertNotNull(context);
	}

	@Test
	public void testExample() {
		Assertions.assertNotNull(context);
	}

}