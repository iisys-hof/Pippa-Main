package de.iisys.pippa.main.osgi_framework;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class PippaOSGiFramework {

	private static final Logger log = Logger.getLogger(PippaOSGiFramework.class.getName());

	private FrameworkFactory frameworkFactory = null;
	private Framework conciergeFramework = null;
	private BundleContext context = null;

	List<String> systemBundlesNames = new ArrayList<String>();

	List<Bundle> systemBundles = new ArrayList<Bundle>();

	List<Bundle> coreBundles = new ArrayList<Bundle>();

	List<Bundle> skillBundles = new ArrayList<Bundle>();

	List<Bundle> supportBundles = new ArrayList<Bundle>();
	
	List<Bundle> serviceBundles = new ArrayList<Bundle>();

	// TODO retrieve from some config-file
	private final String SYSTEM_BUNDLE_PATH = System.getProperty("user.home") + "/Desktop/Bundles/System/plugins";
	private final String CORE_BUNDLE_PATH = System.getProperty("user.home") + "/Desktop/Bundles/Core/plugins";
	private final String SUPPORT_BUNDLE_PATH = System.getProperty("user.home") + "/Desktop/Bundles/Support/plugins";
	private final String SKILL_BUNDLE_PATH = System.getProperty("user.home") + "/Desktop/Bundles/Skill/plugins";
	private final String SERVICE_BUNDLE_PATH = System.getProperty("user.home") + "/Desktop/Bundles/Service/plugins";

	public void start() {

		log.info("loading and starting framework and bundles");

		systemBundlesNames.add("de.iisys.pippa.system.speech_recognizer");
		systemBundlesNames.add("de.iisys.pippa.system.speech_analyser");
		systemBundlesNames.add("de.iisys.pippa.system.audio_storage");
		systemBundlesNames.add("de.iisys.pippa.system.dispatcher");
		systemBundlesNames.add("de.iisys.pippa.system.skill_registry");
		systemBundlesNames.add("de.iisys.pippa.system.resolver");
		systemBundlesNames.add("de.iisys.pippa.system.scheduler");
		systemBundlesNames.add("de.iisys.pippa.system.executor");
		systemBundlesNames.add("de.iisys.pippa.system.message_storage");

		this.startConciergeFramework();

		this.loadSupportBundles();

		this.loadSystemBundles();

		this.loadCoreBundles();

		this.loadServiceBundles();
		
		this.loadSkillBundles();

		
		
		// start all support bundles so they register themselves in the service-registry
		this.startSupportBundles();
		this.startServiceBundles();
		// start all skill bundles
		

		// start all system bundles so they register themselves in the service-registry
		this.startSystemBundles();

		this.startSkillBundles();
		
		// TODO find a better way to get the pippa bundle
		// get pippa bundle and start it after all the system bundles
		for (Bundle b : context.getBundles()) {
			if (b.getSymbolicName().equals("de.iisys.pippa.system.pippa")) {
				try {
					b.start();
				} catch (BundleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	private void startConciergeFramework() {

		log.info("loading and starting OSGi-Concierge framework");

		// Load a framework factory
		this.frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

		// Create a framework
		Map<String, String> config = new HashMap<String, String>();
		// prevent Framework from caching Bundles
		config.put("org.osgi.framework.storage.clean", "onFirstInit");
		config.put("org.osgi.framework.bootdelegation", "javax.*,sun.*");
		config.put("org.osgi.framework.system.packages.extra", "org.w3c.dom");
		//config.put("org.eclipse.concierge.debug", "true");
		//config.put("all " + SUPPORT_BUNDLE_PATH);
		this.conciergeFramework = frameworkFactory.newFramework(config);

		// Start the framework
		try {
			this.conciergeFramework.start();
		} catch (BundleException e) {
			e.printStackTrace();
		}

		// set BundleContext
		this.context = this.conciergeFramework.getBundleContext();

	}

	private void loadSystemBundles() {

		log.info("loading system bundles");

		List<String> systemBundleNames = this.getAllBundleNames(this.SYSTEM_BUNDLE_PATH);

		log.info(systemBundleNames.size() + " bundles found: " + systemBundleNames);
		
		for (String bundleName : systemBundleNames) {
			this.systemBundles.add(this.loadBundle(this.SYSTEM_BUNDLE_PATH, bundleName));
		}
		
		

	}

	private void loadCoreBundles() {

		log.info("loading core bundles");

		List<String> coreBundleNames = this.getAllBundleNames(this.CORE_BUNDLE_PATH);

		log.info(coreBundleNames.size() + " bundles found: " + coreBundleNames);

		for (String bundleName : coreBundleNames) {
			this.coreBundles.add(this.loadBundle(this.CORE_BUNDLE_PATH, bundleName));
		}

	}

	private void loadSupportBundles() {

		log.info("loading support bundles");

		List<String> supportBundleNames = this.getAllBundleNames(this.SUPPORT_BUNDLE_PATH);

		log.info(supportBundleNames.size() + " bundles found: " + supportBundleNames);

		for (String bundleName : supportBundleNames) {
			this.supportBundles.add(this.loadBundle(this.SUPPORT_BUNDLE_PATH, bundleName));
		}

	}

	private void loadSkillBundles() {

		log.info("loading skill bundles");

		List<String> skillBundleNames = this.getAllBundleNames(this.SKILL_BUNDLE_PATH);

		log.info(skillBundleNames.size() + " bundles found: " + skillBundleNames);

		for (String bundleName : skillBundleNames) {
			this.skillBundles.add(this.loadBundle(this.SKILL_BUNDLE_PATH, bundleName));
		}

	}

	private void loadServiceBundles() {

		log.info("loading service bundles");

		List<String> serviceBundleNames = this.getAllBundleNames(this.SERVICE_BUNDLE_PATH);

		log.info(serviceBundleNames.size() + " bundles found: " + serviceBundleNames);

		for (String bundleName : serviceBundleNames) {
			this.serviceBundles.add(this.loadBundle(this.SERVICE_BUNDLE_PATH, bundleName));
		}

	}
	
	private Bundle loadBundle(String folderPath, String bundleName) {

		Bundle bundle = null;

		try {
			bundle = this.context.installBundle("file:" + folderPath + "/" + bundleName);
		} catch (BundleException e) {
			log.info("Error loading Bundle " + bundleName + ": " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bundle;
	}

	private List<String> getAllBundleNames(String folderPath) {

		File folder = new File(folderPath);
		File[] arrayOfFiles = folder.listFiles();
		List<String> listOfFiles = new ArrayList<String>();

		for (int i = 0; i < arrayOfFiles.length; i++) {

			String fileName = arrayOfFiles[i].getName();

			if (arrayOfFiles[i].isFile() && fileName.substring(fileName.lastIndexOf(".") + 1).equals("jar")) {
				listOfFiles.add(fileName);
			}
		}

		return listOfFiles;
	}

	private void startSupportBundles() {

		log.info("starting support bundles");

		for (Bundle b : this.supportBundles) {
			try {
				b.start();
			} catch (BundleException e) {
				log.info("Error starting Bundle " + b.getSymbolicName() + ": " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void startServiceBundles() {

		log.info("starting service bundles");

		for (Bundle b : this.serviceBundles) {
			try {
				b.start();
			} catch (BundleException e) {
				log.info("Error starting Bundle " + b.getSymbolicName() + ": " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private void startSystemBundles() {

		log.info("starting system bundles");

		for (Bundle b : context.getBundles()) {
			if (systemBundlesNames.contains(b.getSymbolicName())) {
				try {
					b.start();
				} catch (BundleException e) {
					log.info("Error starting Bundle " + b.getSymbolicName() + ": " + e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private void startSkillBundles() {

		log.info("starting skill bundles");

		for (Bundle b : this.skillBundles) {
			try {
				b.start();
			} catch (BundleException e) {
				log.info("Error starting Bundle " + b.getSymbolicName() + ": " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
