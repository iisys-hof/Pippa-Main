package de.iisys.pippa.system.scheduler;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.message_processor.AMessageProcessor;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

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

	public void register() {

		Scheduler scheduler = new Scheduler();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "Scheduler");

		Activator.context.registerService(AMessageProcessor.class, scheduler, properties);
		System.out.println("Scheduler as AMessageProcessor registered");
	}

}
