package com.github.rotty3000.spring.osgi.shim;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalMatchers.not;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Configuration
@ComponentScan
public class TestBase {

    /*
     * Fudge the BundleContext. In normal use this would be wired up by a developer.
     */
	@Bean
	public Function<Class<?>, BundleContext> bundleContext() throws Exception {
		BundleContext bundleContext = mock(BundleContext.class);
		when(bundleContext.createFilter(any())).then(a -> FrameworkUtil.createFilter((String)a.getArguments()[0]));
		when(bundleContext.registerService(any(String[].class), any(), any())).then(a -> {
			final String[] serviceTypes = (String[])a.getArguments()[0];
			@SuppressWarnings("unchecked")
			final Dictionary<String, Object> dict = a.getArguments()[2] != null ? (Dictionary<String, Object>)a.getArguments()[2] : new Hashtable<>();
			dict.put("objectClass", serviceTypes);
			dict.put("service.bundleid", 0);
			dict.put("service.id", serviceIdGenerator.incrementAndGet());
			dict.put("service.scope", (a.getArguments()[0] instanceof PrototypeServiceFactory) ? "prototype" : ((a.getArguments()[0] instanceof ServiceFactory) ? "bundle" : "singleton"));
			final ServiceRegistration<?> registration = mock(ServiceRegistration.class);
			when(registration.getReference()).then(b -> {
				final ServiceReference<?> reference = mock(ServiceReference.class);
				when(reference.getProperty(eq("service"))).then(c -> a.getArguments()[1]);
				when(reference.getProperty(not(eq("service")))).then(c -> dict.get(c.getArguments()[0]));
				when(reference.getPropertyKeys()).then(c -> Collections.list(dict.keys()).toArray(new String[0]));
				when(reference.getPropertyKeys()).then(c -> Collections.list(dict.keys()).toArray(new String[0]));
				return reference;
			});
			Arrays.stream(serviceTypes).forEach(s -> services.computeIfAbsent(s, k -> ConcurrentHashMap.newKeySet()).add(registration));
			return registration;
		});
		when(bundleContext.getServiceReferences(any(String.class), any())).then(a -> {
			final Filter filter = FrameworkUtil.createFilter((String)a.getArguments()[1]);
			if (a.getArguments()[0] != null) {
				return services.computeIfAbsent((String)a.getArguments()[0], k -> ConcurrentHashMap.newKeySet()).stream().map(ServiceRegistration::getReference).filter(filter::match).toArray(ServiceReference<?>[]::new);
			}
			return services.values().stream().flatMap(Set::stream).map(ServiceRegistration::getReference).filter(filter::match).toArray(ServiceReference<?>[]::new);
		});
		when(bundleContext.getService(any())).then(a -> {
			final ServiceReference<?> serviceReference = (ServiceReference<?>)a.getArguments()[0];
			return serviceReference.getProperty("service");
		});
		return c -> bundleContext;
	}

	public final Map<String, Set<ServiceRegistration<?>>> services = new ConcurrentHashMap<>();
	public final AtomicLong serviceIdGenerator = new AtomicLong();

}
