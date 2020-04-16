package com.github.rotty3000.spring.osgi.shim;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.function.Supplier;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.rotty3000.spring.osgi.shim.OSGiShim;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GetServiceTest.class)
public class GetServiceTest extends TestBase {

	/*
	 * Create your business bean! Any ol' silly bean will do.
	 */
	@Bean
	public Foo foo() {
		return new FooImpl();
	}

	/*
	 * For testing we'll register it so we have a service to work with.
	 */
    @Bean(name = "registerAFoo")
	public ServiceRegistration<Foo> registerAFoo(
			@Autowired Foo foo,
			@Autowired OSGiShim shim) {
		return shim.registerService(foo, Collections.singletonMap("key", "value"));
	}

	/* ***************************************************************************************************
	 * SCENARIO 1: Get OSGi service as bean
	 */
	@Bean(name = "foo1Service")
	public Supplier<Foo> foo1Service(
			// This is just to force invocation order
			@Autowired ServiceRegistration<Foo> registerAFoo,
			@Autowired OSGiShim shim) {

		return shim.getService(Foo.class);
	}

	/*
	 * If you've named the bean you can get it if needed.
	 * e.g. for testing purposes.
	 */
	@Resource(name = "foo1Service")
	Supplier<Foo> foo1Service;

    @Test
    public void testFoo1ServiceRegistration() {
    	assertNotNull(foo1Service.get());
    }

	/* ***************************************************************************************************
	 * SCENARIO 2: Get OSGi service as bean, but it's missing or no matches
	 */
	@Bean(name = "foo2Service")
	public Supplier<Foo> foo2Service(
			// This is just to force invocation order
			@Autowired ServiceRegistration<Foo> registerAFoo,
			@Autowired OSGiShim shim) {

		return shim.getService(Foo.class, "(foo=bar)");
	}

	@Resource(name = "foo2Service")
	Supplier<Foo> foo2Service;

    @Test
    public void testFoo2ServiceRegistration() {
    	assertNull(foo2Service.get());
    }

	/* ***************************************************************************************************
	 * SCENARIO 3: Get OSGi service as bean, using a filter
	 */
	@Bean(name = "foo3Service")
	public Supplier<Foo> foo3Service(
			// This is just to force invocation order
			@Autowired ServiceRegistration<Foo> registerAFoo,
			@Autowired OSGiShim shim) {
		return shim.getService(Foo.class, "(key=value)");
	}

	@Resource(name = "foo3Service")
	Supplier<Foo> foo3Service;

    @Test
    public void testFoo3ServiceRegistration() {
    	assertNotNull(foo3Service.get());
    }

}
