/*******************************************************************************
 * Copyright (c) 2010 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package bndtools.runtime.applaunch.eclipse4;

import java.util.Dictionary;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.eclipse.osgi.service.runnable.ParameterizedRunnable;
import org.osgi.framework.BundleContext;

class EclipseApplicationLauncher implements ApplicationLauncher {

    private final Logger log = Logger.getLogger(EclipseApplicationLauncher.class.getPackage().getName());
    private final BundleContext bc;

      
    public EclipseApplicationLauncher(BundleContext context) {
        this.bc = context;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void launch(final ParameterizedRunnable runnable, final Object context) {
        log.log(Level.FINE, "Received launch request from Eclipse application service, registering java.lang.Runnable{main.thread=true}");
        Runnable service = new Runnable() {
            public void run() {
                try {
                    log.log(Level.FINE, "Executing appplication on thread {0} ({1}).", new Object[] { Thread.currentThread().getName(),
                            Thread.currentThread().getId() });
                    runnable.run(context);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error executing application", e);
                }
            }
        };
		Dictionary svcProps = new Properties();
        svcProps.put("main.thread", "true");
        bc.registerService(Runnable.class.getName(), service, svcProps);
    }

    public void shutdown() {
        log.warning("Ignoring shutdown call");
    }
}
