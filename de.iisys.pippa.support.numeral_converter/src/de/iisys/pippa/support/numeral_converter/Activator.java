package de.iisys.pippa.support.numeral_converter;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.numeral_converter.NumeralConverter;

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
	
	/**
	 * registers the 
	 */
	private void register() {

		NumeralConverterImpl numeralConverter = new NumeralConverterImpl();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "NumeralConverter");

		Activator.context.registerService(NumeralConverter.class, numeralConverter, properties);
		
	}

}
