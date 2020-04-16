package com.github.rotty3000.spring.osgi.shim;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static org.osgi.framework.FrameworkUtil.createFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class OSGiShim implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(OSGiShim.class);

	private final Function<Class<?>, BundleContext> bundleContextFunction;
	private final List<ServiceRegistration<?>> registrations = new CopyOnWriteArrayList<>();
	private final Map<ServiceKey, ServiceTracker<?, ?>> trackerLists = new ConcurrentHashMap<>();

	@Autowired
	public OSGiShim(Function<Class<?>, BundleContext> bundleContextFunction) {
		this.bundleContextFunction = requireNonNull(bundleContextFunction);
	}

	public <T> Supplier<T> getService(Class<T> clazz) {
		return getService(clazz, null);
	}

	@SuppressWarnings("unchecked")
	public <T> Supplier<T> getService(Class<T> clazz, String filterString) {
		ServiceTracker<?,?> serviceTracker = trackerLists.computeIfAbsent(
			new ServiceKey(clazz, filterString), k -> open(clazz, filterString));

		return () -> (T)(serviceTracker.isEmpty() ? null : serviceTracker.getService());
	}

	public <S> ServiceRegistration<S> registerService(S bean) {
		return registerService(bean, filteredClassNames(bean), null);
	}

	public <S> ServiceRegistration<S> registerService(S bean, List<String> serviceTypes) {
		return registerService(bean, serviceTypes, null);
	}

	public <S> ServiceRegistration<S> registerService(S bean, Map<String, Object> properties) {
		return registerService(bean, filteredClassNames(bean), properties);
	}

	@SuppressWarnings("unchecked")
	public <S> ServiceRegistration<S> registerService(S bean, List<String> serviceTypes, Map<String, Object> properties) {
		requireNonNull(bean, "bean must not be null");
		if (serviceTypes.isEmpty()) {
			throw new IllegalArgumentException("at least one service type must be specified to publish bean " + bean + " as a service");
		}
		ServiceRegistration<S> registration = null;
		try {
			return registration = (ServiceRegistration<S>)bundleContextFunction.apply(bean.getClass()).registerService(
				serviceTypes.toArray(new String[0]), bean, getServiceProperties(properties));
		}
		finally {
			if (registration != null) {
				log.debug("Registed {} as {}", bean, registration.getReference());
			}
		}
	}

	@EventListener(classes = ContextStoppedEvent.class)
	@Override
	public void close() throws Exception {
		registrations.removeIf(
			reg -> {
				try {
					reg.unregister();
				}
				catch (Exception e) {
					// ignore; this means it was already unregistered
				}
				return true;
			}
		);
		trackerLists.forEach((k, v) -> v.close());
		trackerLists.clear();
	}

	private static List<String> filteredClassNames(Object bean) {
		List<String> list = Arrays.stream(
			bean.getClass().getInterfaces()
		).map(Class::getName).filter(
			OSGiShim::filterTypes
		).collect(toCollection(ArrayList::new));

		if (list.isEmpty()) {
			list.add(bean.getClass().getName());
		}

		return list;
	}

	private static boolean filterTypes(String className) {
		return !filteredClassNames.contains(className);
	}

	private static final List<String> filteredClassNames = Arrays.asList(
		"org.springframework.aop.SpringProxy",
		"org.springframework.aop.framework.Advised",
		"org.springframework.core.DecoratingProxy",
		"org.springframework.cglib.proxy.Factory",
		Serializable.class.getName()
	);

	private Dictionary<String, Object> getServiceProperties(Map<String, Object> properties) {
		Hashtable<String, Object> copy = new Hashtable<>();
		if (properties != null) {
			properties.forEach((k,v) -> copy.put(String.valueOf(k), v));
		}
		return copy;
	}

	private <X> ServiceTracker<X, X> open(Class<X> clazz, String filterString) {
		requireNonNull(clazz, "clazz must not be null");
		ServiceTracker<X, X> serviceTracker = null;
		try {
			Filter filter = createFilter(format("(objectClass=%s)", clazz.getName()));
			if (filterString != null && !filterString.isEmpty()) {
				filter = createFilter(format("(&%s%s)", filter.toString(), filterString));
			}
			return serviceTracker = new ServiceTracker<>(bundleContextFunction.apply(clazz), filter, null);
		}
		catch (InvalidSyntaxException e) {
			log.error("Could not create tracker for {} with {}", clazz.getName(), filterString);
			throw duck(e);
		}
		finally {
			if (serviceTracker != null) {
				serviceTracker.open();
			}
			else {
				log.debug("Tracking {} with {}", clazz.getName(), filterString);
			}
		}
	}

	private static final RuntimeException duck(Throwable t) {
		OSGiShim.<RuntimeException> throwsUnchecked(t);
		throw new AssertionError("unreachable");
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwsUnchecked(Throwable throwable) throws E {
		throw (E) throwable;
	}

	private static class ServiceKey {
		private final String className;
		private final String filterString;
		public ServiceKey(Class<?> clazz, String filterString) {
			super();
			this.className = clazz.getName();
			this.filterString = filterString;
		}
		@Override
		public int hashCode() {
			return Objects.hash(className, filterString);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ServiceKey)) {
				return false;
			}
			ServiceKey other = (ServiceKey) obj;
			return Objects.equals(className, other.className) && Objects.equals(filterString, other.filterString);
		}
	}

}