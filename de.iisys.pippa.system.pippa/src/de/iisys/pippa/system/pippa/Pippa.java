package de.iisys.pippa.system.pippa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.dispatcher.Dispatcher;
import de.iisys.pippa.core.executor.Executor;
import de.iisys.pippa.core.message_processor.AMessageProcessor;
import de.iisys.pippa.core.pippa_service.PippaService;
import de.iisys.pippa.core.resolver.Resolver;
import de.iisys.pippa.core.skill.Skill;
import de.iisys.pippa.core.skill_registry.SkillRegistry;

public class Pippa {

	private static final Logger log = Logger.getLogger(Pippa.class.getName());

	private Map<String, AMessageProcessor> systemComponents = new LinkedHashMap<String, AMessageProcessor>();

	private List<Skill> skills = new ArrayList<Skill>();

	private List<PippaService> pippaServices = new ArrayList<PippaService>();

	private BundleContext context = null;

	protected Pippa(BundleContext context) {

		this.context = context;

		systemComponents.put("SpeechRecognizer", null);
		systemComponents.put("SpeechAnalyser", null);
		systemComponents.put("AudioStorage", null);
		systemComponents.put("Dispatcher", null);
		systemComponents.put("SkillRegistry", null);
		systemComponents.put("Resolver", null);
		systemComponents.put("Scheduler", null);
		systemComponents.put("Executor", null);
		systemComponents.put("MessageStorage", null);
		
	}

	public void startPippa() {

		log.info("starting Pippa");

		this.loadSystemServices();

		this.chainSystemServices();

		this.threadSystemServices();

		this.loadSkillServices();

		this.chainSkillServices();

		this.threadSkillServices();

		// TODO rename
		this.loadPippaServices();

		this.threadPippaServices();

		this.showAllThreads();
		
		log.info("Pippa started");
		
	}

	private void loadSystemServices() {

		log.info("loading system services: " + systemComponents.keySet());

		Collection<ServiceReference<AMessageProcessor>> serviceReferences = null;

		for (String serviceName : systemComponents.keySet()) {

			try {
				// get all service references registered under the given name (which should be
				// 1)
				serviceReferences = context.getServiceReferences(AMessageProcessor.class, "(name=" + serviceName + ")");

				// get actual object from reference
				AMessageProcessor service = context
						.getService(((List<ServiceReference<AMessageProcessor>>) serviceReferences).get(0));

				// store object with service name (class-name)
				systemComponents.put(serviceName, service);

			} catch (InvalidSyntaxException e) {
				log.info("Error loading Service " + serviceName + ": " + e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private void chainSystemServices() {

		log.info("chaining system services");

		systemComponents.get("SpeechRecognizer")
				.setOutgoingQueue(systemComponents.get("SpeechAnalyser").getIncomingQueue());
		systemComponents.get("SpeechAnalyser")
				.setOutgoingQueue(systemComponents.get("AudioStorage").getIncomingQueue());
		systemComponents.get("AudioStorage").setOutgoingQueue(systemComponents.get("Dispatcher").getIncomingQueue());
		systemComponents.get("Dispatcher").setOutgoingQueue(systemComponents.get("SkillRegistry").getIncomingQueue());

		Resolver resolver = (Resolver) systemComponents.get("Resolver");
		resolver.setDispatcherQueue(systemComponents.get("Dispatcher").getIncomingQueue());
		resolver.setExecutorQueue(systemComponents.get("Executor").getIncomingQueue());
		resolver.setSkillRegistryQueue(systemComponents.get("SkillRegistry").getIncomingQueue());
		resolver.setSchedulerQueue(systemComponents.get("Scheduler").getIncomingQueue());

		systemComponents.get("Scheduler").setOutgoingQueue(systemComponents.get("Resolver").getIncomingQueue());
		systemComponents.get("Executor").setOutgoingQueue(systemComponents.get("MessageStorage").getIncomingQueue());
		
		Executor executor = (Executor)systemComponents.get("Executor");
		executor.setSkillRegistryQueue(systemComponents.get("SkillRegistry").getIncomingQueue());

	}

	private void threadSystemServices() {

		log.info("threading system services");

		for (Entry<String, AMessageProcessor> chainLink : systemComponents.entrySet()) {
			Thread t = new Thread((Runnable) chainLink.getValue(), "--- " + chainLink.getKey());
			t.start();
		}

	}

	private void loadSkillServices() {
		
		log.info("loading skill services");

		Collection<ServiceReference<Skill>> serviceReferences = null;
		
		Skill skill = null;
		
		try {
		
			// get all service references
			serviceReferences = context.getServiceReferences(Skill.class, null);
			
			for (ServiceReference<Skill> serviceReference : serviceReferences) {

				// get actual object from reference
				skill = context.getService(serviceReference);

				// store object
				skills.add(skill);

				// register skill within registry
				this.registerSkill(skill);

				// register skills regexes within dispatcher
				this.registerRegex(skill);

			}

		} catch (InvalidSyntaxException e) {
			log.info("Error loading Service " + skill.getClass().getSimpleName() + ": " + e);
			// TODO Auto-generated catch block
		}

	}

	private void chainSkillServices() {

		log.info("chaining skill services");

		// link all outgoing queues of skills to the resolvers incoming queue for
		for (Skill skill : this.skills) {
			((AMessageProcessor) skill).setOutgoingQueue(systemComponents.get("Resolver").getIncomingQueue());
		}

	}

	private void threadSkillServices() {

		log.info("threading skill services");

		for (Skill skill : skills) {
			Thread t = new Thread((Runnable) skill, "+++ " + skill.getClass().getSimpleName());
			t.start();
		}

	}

	private void loadPippaServices() {

		log.info("loading pippa_service services");

		Collection<ServiceReference<PippaService>> serviceReferences = null;
		PippaService service = null;
		
		try {
			// get all service references
			serviceReferences = context.getServiceReferences(PippaService.class, null);

			for (ServiceReference<PippaService> serviceReference : serviceReferences) {

				// get actual object from reference
				service = context.getService(serviceReference);

				this.pippaServices.add(service);

			}

		} catch (InvalidSyntaxException e) {
			log.info("Error loading service " + service.getClass().getSimpleName() + ": " + e);
			// TODO Auto-generated catch block
		}

	}

	private void threadPippaServices() {

		log.info("threading pippa_service services");

		for (PippaService service : this.pippaServices) {
			if (service instanceof Runnable) {
				Thread t = new Thread((Runnable) service, "~~~ " + service.getClass().getSimpleName());
				t.start();
			}
		}

	}

	private void registerSkill(Skill skill) {
		((SkillRegistry) this.systemComponents.get("SkillRegistry")).registerSkill(skill);
	}

	private void registerRegex(Skill skill) {
		((Dispatcher) this.systemComponents.get("Dispatcher")).registerRegexes(skill);
	}

	private void deregisterSkill(Skill skill) {
		// TODO
	}

	private void deregisterRegex(Skill skill) {
		// TODO
	}
	
	private void showAllThreads() {
		
		System.out.println(" ");
		System.out.println("*****THREADS******");
		System.out.println("** --- Pippa-System");
		System.out.println("** ~~~ Pippa-Services");
		System.out.println("** +++ Skills");
		System.out.println("******************");
		System.out.println(" ");
		
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread t : threadSet) {
			System.out.println(t.getName());
		}
	}

}
