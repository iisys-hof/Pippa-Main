package de.iisys.pippa.system.speech_recognizer;

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
		
		SpeechRecognizer speechRecognizer = new SpeechRecognizer();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "SpeechRecognizer");
		
		Activator.context.registerService(AMessageProcessor.class, speechRecognizer, properties);
		System.out.println("SpeechRecognizer as AMessageProcessor registered");
	}
	
}
