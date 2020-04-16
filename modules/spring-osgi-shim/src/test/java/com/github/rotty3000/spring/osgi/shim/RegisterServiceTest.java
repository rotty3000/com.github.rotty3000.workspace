package com.github.rotty3000.spring.osgi.shim;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

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
@ContextConfiguration(classes = RegisterServiceTest.class)
public class RegisterServiceTest extends TestBase {

	/*
	 * Create your business bean! Any ol' bean will do.
	 */
	@Bean
	public Foo foo() {
		return new FooImpl();
	}

	/* ***************************************************************************************************
	 * SCENARIO 1: Register a bean as a service
	 *
	 * Get the bean and turn it into an automatically managed OSGi service.
	 * Unregistration is done automatically. Give it a name if you need to
	 * reference the registration yourself, otherwise it's not necessary.
	 */
	@Bean(name = "fooService")
	public ServiceRegistration<Foo> fooService(
		@Autowired Foo foo,
		@Autowired OSGiShim shim) {

		return shim.registerService(foo);
	}

	/*
	 * If you've named the bean you can get it if needed.
	 * e.g. for testing purposes.
	 */
	@Resource(name = "fooService")
    ServiceRegistration<Foo> fooServiceRegistration;

    @Test
    public void testFooServiceRegistration() {
    	assertNotNull(fooServiceRegistration);
    	assertNotNull(fooServiceRegistration.getReference());
    	assertEquals(4, fooServiceRegistration.getReference().getPropertyKeys().length);
    	assertArrayEquals(new String[] {AutoCloseable.class.getName(), Foo.class.getName()}, (String[])fooServiceRegistration.getReference().getProperty("objectClass"));
    	assertNull(fooServiceRegistration.getReference().getProperty("baz"));
    }

	/* ***************************************************************************************************
	 * SCENARIO 2: Register a bean as a service with service properties
	 */
	@Bean(name = "foo2Service")
	public ServiceRegistration<Foo> foo2Service(
		@Autowired Foo foo,
		@Autowired OSGiShim shim) {

		return shim.registerService(foo, Collections.singletonMap("baz", "boff"));
	}

	@Resource(name = "foo2Service")
    ServiceRegistration<Foo> foo2ServiceRegistration;

    @Test
    public void testFoo2ServiceRegistration() {
    	assertNotNull(foo2ServiceRegistration);
    	assertNotNull(foo2ServiceRegistration.getReference());
    	assertEquals(5, foo2ServiceRegistration.getReference().getPropertyKeys().length);
    	assertArrayEquals(new String[] {AutoCloseable.class.getName(), Foo.class.getName()}, (String[])foo2ServiceRegistration.getReference().getProperty("objectClass"));
    	assertEquals("boff", foo2ServiceRegistration.getReference().getProperty("baz"));
    }

	/* ***************************************************************************************************
	 * SCENARIO 3: Register a bean as a service using a narrower set of types.
	 *
	 * (default uses _directly implemented_ interfaces only, or else the bean's exact class)
	 */
	@Bean(name = "foo3Service")
	public ServiceRegistration<?> foo3Service(
		@Autowired Foo foo,
		@Autowired OSGiShim shim) {

		return shim.registerService(foo, Arrays.asList(AutoCloseable.class.getName()));
	}

	/*
	 * If you've named it you can get the registration if needed.
	 * e.g. for testing purposes.
	 */
	@Resource(name = "foo3Service")
    ServiceRegistration<AutoCloseable> foo3ServiceRegistration;

    @Test
    public void testFoo3ServiceRegistration() {
    	assertNotNull(foo3ServiceRegistration);
    	assertNotNull(foo3ServiceRegistration.getReference());
    	assertEquals(4, foo3ServiceRegistration.getReference().getPropertyKeys().length);
    	assertArrayEquals(new String[] {AutoCloseable.class.getName()}, (String[])foo3ServiceRegistration.getReference().getProperty("objectClass"));
    }

}
