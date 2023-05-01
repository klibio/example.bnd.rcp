package bndtools.runtime.applaunch.eclipse4;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.location.EquinoxLocations;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("rawtypes")
class LauncherTracker extends ServiceTracker {

	private static final String CLEAN = "-clean"; //$NON-NLS-1$
	private static final String CONSOLE = "-console"; //$NON-NLS-1$
	private static final String CONSOLE_LOG = "-consoleLog"; //$NON-NLS-1$
	private static final String DEBUG = "-debug"; //$NON-NLS-1$
	private static final String INITIALIZE = "-initialize"; //$NON-NLS-1$
	private static final String DEV = "-dev"; //$NON-NLS-1$
	private static final String WS = "-ws"; //$NON-NLS-1$
	private static final String OS = "-os"; //$NON-NLS-1$
	private static final String ARCH = "-arch"; //$NON-NLS-1$
	private static final String NL = "-nl"; //$NON-NLS-1$
	private static final String NL_EXTENSIONS = "-nlExtensions"; //$NON-NLS-1$
	private static final String CONFIGURATION = "-configuration"; //$NON-NLS-1$ 
	private static final String USER = "-user"; //$NON-NLS-1$
	private static final String NOEXIT = "-noExit"; //$NON-NLS-1$
	private static final String LAUNCHER = "-launcher"; //$NON-NLS-1$
	// this is more of an Eclipse argument but this OSGi implementation stores
	// its
	// metadata alongside Eclipse's.
	private static final String DATA = "-data"; //$NON-NLS-1$

	// System properties
	public static final String PROP_BUNDLES = "osgi.bundles"; //$NON-NLS-1$
	public static final String PROP_BUNDLES_STARTLEVEL = "osgi.bundles.defaultStartLevel"; //$NON-NLS-1$ //The start level used to install the bundles
	public static final String PROP_EXTENSIONS = "osgi.framework.extensions"; //$NON-NLS-1$
	public static final String PROP_INITIAL_STARTLEVEL = "osgi.startLevel"; //$NON-NLS-1$ //The start level when the fwl start
	public static final String PROP_DEBUG = "osgi.debug"; //$NON-NLS-1$
	public static final String PROP_DEV = "osgi.dev"; //$NON-NLS-1$
	public static final String PROP_CLEAN = "osgi.clean"; //$NON-NLS-1$
	public static final String PROP_CONSOLE = "osgi.console"; //$NON-NLS-1$
	public static final String PROP_CONSOLE_CLASS = "osgi.consoleClass"; //$NON-NLS-1$
	public static final String PROP_CHECK_CONFIG = "osgi.checkConfiguration"; //$NON-NLS-1$
	public static final String PROP_OS = "osgi.os"; //$NON-NLS-1$
	public static final String PROP_WS = "osgi.ws"; //$NON-NLS-1$
	public static final String PROP_NL = "osgi.nl"; //$NON-NLS-1$
	private static final String PROP_NL_EXTENSIONS = "osgi.nl.extensions"; //$NON-NLS-1$
	public static final String PROP_ARCH = "osgi.arch"; //$NON-NLS-1$
	public static final String PROP_ADAPTOR = "osgi.adaptor"; //$NON-NLS-1$
	public static final String PROP_SYSPATH = "osgi.syspath"; //$NON-NLS-1$
	public static final String PROP_LOGFILE = "osgi.logfile"; //$NON-NLS-1$
	public static final String PROP_FRAMEWORK = "osgi.framework"; //$NON-NLS-1$
	public static final String PROP_INSTALL_AREA = "osgi.install.area"; //$NON-NLS-1$
	public static final String PROP_FRAMEWORK_SHAPE = "osgi.framework.shape"; //$NON-NLS-1$ //the shape of the fwk (jar, or folder)
	public static final String PROP_NOSHUTDOWN = "osgi.noShutdown"; //$NON-NLS-1$
	public static final String PROP_EXITCODE = "eclipse.exitcode"; //$NON-NLS-1$
	public static final String PROP_EXITDATA = "eclipse.exitdata"; //$NON-NLS-1$
	public static final String PROP_CONSOLE_LOG = "eclipse.consoleLog"; //$NON-NLS-1$
	public static final String PROP_IGNOREAPP = "eclipse.ignoreApp"; //$NON-NLS-1$
	public static final String PROP_REFRESH_BUNDLES = "eclipse.refreshBundles"; //$NON-NLS-1$
	static final String PROP_LAUNCHER = "eclipse.launcher"; //$NON-NLS-1$

	public static boolean debug = false;

	private final Logger log = Logger.getLogger(Activator.class.getPackage()
			.getName());

	// fields introduced for Equinox Configuration
	private static Map<String, String> configuration = null;
	private static EquinoxConfiguration equinoxConfig;
	@SuppressWarnings("unused")
	private static String[] allArgs = null;
	private static String[] frameworkArgs = null;
	private static String[] appArgs = null;

	@SuppressWarnings("unchecked")
	public LauncherTracker(BundleContext context) {
		super(context, createFilter(), null);
	}

	private static Filter createFilter() {
		try {
			return FrameworkUtil
					.createFilter("(&(objectClass=aQute.launcher.Launcher)(launcher.ready=true))"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object addingService(ServiceReference reference) {

		prepareEnvService(context);

		// Find and start the Equinox Application bundle
		boolean found = false;
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			if ("org.eclipse.equinox.app".equals(getBsn(bundle))) {
				found = true;
				try {
					bundle.start();
				} catch (BundleException e) {
					log.log(Level.SEVERE,
							"Unable to start bundle org.eclipse.equinox.app. Eclipse application cannot start.",
							e);
				}
				break;
			}
		}
		if (!found)
			log.warning("Unable to find bundle org.eclipse.equinox.app. Eclipse application will not start.");

		// Register the ApplicationLauncher
		log.fine("Registering ApplicationLauncher service.");
		return context.registerService(ApplicationLauncher.class.getName(),
				new EclipseApplicationLauncher(context), null);
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		((ServiceRegistration) service).unregister();
	}

	private String getBsn(Bundle bundle) {
		String bsn = (String) bundle.getHeaders().get(
				Constants.BUNDLE_SYMBOLICNAME);
		int semiColonIndex = bsn.indexOf(';');
		if (semiColonIndex > -1)
			bsn = bsn.substring(0, semiColonIndex);
		return bsn;
	}

	private void prepareEnvService(BundleContext bc) {
		// retrieve the current EquinoxConfiguration
		ServiceReference<EnvironmentInfo> srvcRef = bc
				.getServiceReference(EnvironmentInfo.class);
		if (srvcRef != null) {
			EnvironmentInfo service = bc.getService(srvcRef);
			if (service instanceof EquinoxConfiguration) {
				equinoxConfig = (EquinoxConfiguration) service;
			}
		}

		// process the arguments for bnd launcher
		String[] parameters = null;
		try {
			ServiceReference<?>[] serviceReferences = context
					.getServiceReferences("aQute.launcher.Launcher",
							"(launcher.arguments=*)");
			if (serviceReferences != null && serviceReferences.length > 0) {
				Object property = serviceReferences[0]
						.getProperty("launcher.arguments");

				if (property instanceof String[]) {
					parameters = (String[]) property;
					processCommandLine(parameters);
					equinoxConfig.setAllArgs(parameters);
					equinoxConfig.setFrameworkArgs(frameworkArgs);
					equinoxConfig.setAppArgs(appArgs);
					
					log.fine("configured program arguments " + parameters);
				}
			} else {
				log.log(Level.SEVERE,
						"service aQute.launcher.Launcher with props launcher.arguments could not be retireved");
			}
		} catch (Exception e) {
			log.log(Level.SEVERE,
					"command line parameters could not be configured.");
		}

	}

	/*
	 * copy from EclipseStarter.class ( removed the initialize variable )
	 */
	private static void processCommandLine(String[] args) throws Exception {
		allArgs = args;
		if (args.length == 0) {
			frameworkArgs = args;
			return;
		}
		int[] configArgs = new int[args.length];
		configArgs[0] = -1; // need to initialize the first element to something
							// that could not be an index.
		int configArgIndex = 0;
		for (int i = 0; i < args.length; i++) {
			boolean found = false;
			// check for args without parameters (i.e., a flag arg)

			// check if debug should be enabled for the entire platform
			// If this is the last arg or there is a following arg (i.e., arg+1
			// has a leading -),
			// simply enable debug. Otherwise, assume that that the following
			// arg is
			// actually the filename of an options file. This will be processed
			// below.
			if (args[i].equalsIgnoreCase(DEBUG)
					&& ((i + 1 == args.length) || ((i + 1 < args.length) && (args[i + 1]
							.startsWith("-"))))) { //$NON-NLS-1$
				setProperty(PROP_DEBUG, ""); //$NON-NLS-1$
				debug = true;
				found = true;
			}

			// check if development mode should be enabled for the entire
			// platform
			// If this is the last arg or there is a following arg (i.e., arg+1
			// has a leading -),
			// simply enable development mode. Otherwise, assume that that the
			// following arg is
			// actually some additional development time class path entries.
			// This will be processed below.
			if (args[i].equalsIgnoreCase(DEV)
					&& ((i + 1 == args.length) || ((i + 1 < args.length) && (args[i + 1]
							.startsWith("-"))))) { //$NON-NLS-1$
				setProperty(PROP_DEV, ""); //$NON-NLS-1$
				found = true;
			}

			// look for the initialization arg
			if (args[i].equalsIgnoreCase(INITIALIZE)) {
				// initialize = true;
				found = true;
			}

			// look for the clean flag.
			if (args[i].equalsIgnoreCase(CLEAN)) {
				setProperty(PROP_CLEAN, "true"); //$NON-NLS-1$
				found = true;
			}

			// look for the consoleLog flag
			if (args[i].equalsIgnoreCase(CONSOLE_LOG)) {
				setProperty(PROP_CONSOLE_LOG, "true"); //$NON-NLS-1$
				found = true;
			}

			// look for the console with no port.
			if (args[i].equalsIgnoreCase(CONSOLE)
					&& ((i + 1 == args.length) || ((i + 1 < args.length) && (args[i + 1]
							.startsWith("-"))))) { //$NON-NLS-1$
				setProperty(PROP_CONSOLE, ""); //$NON-NLS-1$
				found = true;
			}

			if (args[i].equalsIgnoreCase(NOEXIT)) {
				setProperty(PROP_NOSHUTDOWN, "true"); //$NON-NLS-1$
				found = true;
			}

			if (found) {
				configArgs[configArgIndex++] = i;
				continue;
			}
			// check for args with parameters. If we are at the last argument or
			// if the next one
			// has a '-' as the first character, then we can't have an arg with
			// a parm so continue.
			if (i == args.length - 1 || args[i + 1].startsWith("-")) { //$NON-NLS-1$
				continue;
			}
			String arg = args[++i];

			// look for the console and port.
			if (args[i - 1].equalsIgnoreCase(CONSOLE)) {
				setProperty(PROP_CONSOLE, arg);
				found = true;
			}

			// look for the configuration location .
			if (args[i - 1].equalsIgnoreCase(CONFIGURATION)) {
				setProperty(EquinoxLocations.PROP_CONFIG_AREA, arg);
				found = true;
			}

			// look for the data location for this instance.
			if (args[i - 1].equalsIgnoreCase(DATA)) {
				setProperty(EquinoxLocations.PROP_INSTANCE_AREA, arg);
				found = true;
			}

			// look for the user location for this instance.
			if (args[i - 1].equalsIgnoreCase(USER)) {
				setProperty(EquinoxLocations.PROP_USER_AREA, arg);
				found = true;
			}

			// look for the launcher location
			if (args[i - 1].equalsIgnoreCase(LAUNCHER)) {
				setProperty(EquinoxLocations.PROP_LAUNCHER, arg);
				found = true;
			}
			// look for the development mode and class path entries.
			if (args[i - 1].equalsIgnoreCase(DEV)) {
				setProperty(PROP_DEV, arg);
				found = true;
			}

			// look for the debug mode and option file location.
			if (args[i - 1].equalsIgnoreCase(DEBUG)) {
				setProperty(PROP_DEBUG, arg);
				debug = true;
				found = true;
			}

			// look for the window system.
			if (args[i - 1].equalsIgnoreCase(WS)) {
				setProperty(PROP_WS, arg);
				found = true;
			}

			// look for the operating system
			if (args[i - 1].equalsIgnoreCase(OS)) {
				setProperty(PROP_OS, arg);
				found = true;
			}

			// look for the system architecture
			if (args[i - 1].equalsIgnoreCase(ARCH)) {
				setProperty(PROP_ARCH, arg);
				found = true;
			}

			// look for the nationality/language
			if (args[i - 1].equalsIgnoreCase(NL)) {
				setProperty(PROP_NL, arg);
				found = true;
			}

			// look for the locale extensions
			if (args[i - 1].equalsIgnoreCase(NL_EXTENSIONS)) {
				setProperty(PROP_NL_EXTENSIONS, arg);
				found = true;
			}

			// done checking for args. Remember where an arg was found
			if (found) {
				configArgs[configArgIndex++] = i - 1;
				configArgs[configArgIndex++] = i;
			}
		}

		// remove all the arguments consumed by this argument parsing
		if (configArgIndex == 0) {
			frameworkArgs = new String[0];
			appArgs = args;
			return;
		}
		appArgs = new String[args.length - configArgIndex];
		frameworkArgs = new String[configArgIndex];
		configArgIndex = 0;
		int j = 0;
		int k = 0;
		for (int i = 0; i < args.length; i++) {
			if (i == configArgs[configArgIndex]) {
				frameworkArgs[k++] = args[i];
				configArgIndex++;
			} else
				appArgs[j++] = args[i];
		}
		return;
	}

	/*
	 * exact copy from EclipseStarter.class
	 */
	private synchronized static Object setProperty(String key, String value) {
		if (equinoxConfig != null) {
			return equinoxConfig.setProperty(key, value);
		}
		if ("true".equals(getConfiguration().get(EquinoxConfiguration.PROP_USE_SYSTEM_PROPERTIES))) { //$NON-NLS-1$
			System.setProperty(key, value);
		}
		return getConfiguration().put(key, value);
	}

	/*
	 * exact copy from EclipseStarter.class
	 */
	private synchronized static Map<String, String> getConfiguration() {
		if (configuration == null) {
			configuration = new HashMap<String, String>();
			// TODO hack
			String useSystemProperties = System.getProperty(
					EquinoxConfiguration.PROP_USE_SYSTEM_PROPERTIES, "true"); //$NON-NLS-1$
			if ("true".equals(useSystemProperties)) { //$NON-NLS-1$
				configuration
						.put(EquinoxConfiguration.PROP_USE_SYSTEM_PROPERTIES,
								"true"); //$NON-NLS-1$
			}
		}
		return configuration;
	}

}
