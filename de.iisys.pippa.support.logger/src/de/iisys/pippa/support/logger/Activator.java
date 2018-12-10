package de.iisys.pippa.support.logger;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.logger.PippaLogger;

public class Activator implements BundleActivator {

	private static BundleContext context;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.register();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	/**
	 * registers the PippaLogger within in the OSGi service registry
	 */
	private void register() {

		PippaLoggerImpl logger = PippaLoggerImpl.getInstance();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "PippaLogger");

		Activator.context.registerService(PippaLogger.class, logger, properties);
		System.out.println("PippaLoggerImpl as PippaLogger registered");
	}

}
