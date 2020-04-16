package com.github.rotty3000.spring.osgi.shim;

import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Works when the bean is inside a bundle with the spring context and running in
 * an OSGi Framework.
 *
 * If not running inside of OSGi, for instance where the framework is embedded
 * and spring is a peer or parent of the framework, a bean could be provided
 * that uses the `org.osgi.framework.launch.Framework.getBundleContext()` method
 * of the embedded framework instance.
 *
 * In a WAB (Web Application Bundle) the bean could get the BundleContext by
 * calling `servletContext.getAttribute("osgi-bundlecontext")`.
 */
@Service
@Scope("osgi")
public class DefaultBundleContextFunction implements FactoryBean<Function<Class<?>, BundleContext>> {

	@Override
	public Function<Class<?>, BundleContext> getObject() throws Exception {
		return c -> FrameworkUtil.getBundle(c).getBundleContext();
	}

	@Override
	public Class<?> getObjectType() {
		return Function.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
