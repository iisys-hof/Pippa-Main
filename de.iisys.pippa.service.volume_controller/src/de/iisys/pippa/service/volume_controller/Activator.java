package de.iisys.pippa.service.volume_controller;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.pippa_service.PippaService;
import de.iisys.pippa.core.volume_controller.VolumeController;

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

	/**
	 * registers the
	 */
	private void register() {

		VolumeControllerImpl volumeController = new VolumeControllerImpl();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "VolumeController");

		String[] classNames = new String[] { VolumeController.class.getName(), PippaService.class.getName() };

		Activator.context.registerService(classNames, volumeController, properties);

	}

}
