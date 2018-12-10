package de.iisys.pippa.core.service_loader;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.iisys.pippa.core.logger.PippaLogger;
import de.iisys.pippa.core.status.StatusAccess;

public final class PippaServiceLoader {

	@SuppressWarnings("unchecked")
	static public Object getService(Class<?> serviceClass, String serviceName, BundleContext context)
			throws InvalidSyntaxException {

		Collection<?> serviceReferences = null;
		serviceReferences = context.getServiceReferences(serviceClass, "(name=" + serviceName + ")");
		return context.getService(((List<ServiceReference<?>>) serviceReferences).get(0));

	}

	static public Object getService(Class<?> serviceClass, BundleContext context) {

		ServiceReference<?> serviceReference = context.getServiceReference(serviceClass);
		return context.getService(serviceReference);

	}

	static public Logger getLogger(BundleContext context) {

		try {
			ServiceReference<PippaLogger> serviceReference = context.getServiceReference(PippaLogger.class);
			PippaLogger service = context.getService(serviceReference);
			return service.getLogger();
		} catch (Exception e) {
			return Logger.getAnonymousLogger();
		}

	}

	static public StatusAccess getStatus(BundleContext context) {

		try {
			ServiceReference<StatusAccess> serviceReference = context.getServiceReference(StatusAccess.class);
			StatusAccess service = context.getService(serviceReference);
			return service;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}
