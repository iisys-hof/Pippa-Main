package de.iisys.pippa.support.status;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.core.status.StatusReader;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.register();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public void register() {

		Status status = Status.getInstance();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "Status");
		
		String[] classNames = new String[] {StatusReader.class.getName(), StatusAccess.class.getName()};

		Activator.context.registerService(classNames, status, properties);
		System.out.println("Status as StatusAccess, StatusReader registered");
	}
	
}
