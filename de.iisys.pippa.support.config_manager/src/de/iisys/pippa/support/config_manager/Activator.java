package de.iisys.pippa.support.config_manager;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.config_manager.ConfigManager;

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

		ConfigManager configManager = ConfigManagerImpl.getInstance();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "ConfigManager");

		Activator.context.registerService(ConfigManager.class, configManager, properties);
		System.out.println("ConfigManagerImpl as ConfigManager registered");
	}

}
