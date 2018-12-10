package de.iisys.pippa.skill.system_access;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.iisys.pippa.core.skill.Skill;

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

		Skill systemAccessSkill = new SystemAccessSkillImpl();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("name", "SystemAccess");

		Activator.context.registerService(Skill.class, systemAccessSkill, properties);
		System.out.println("SystemAccessSkillImpl as Skill registered");
	}

}
