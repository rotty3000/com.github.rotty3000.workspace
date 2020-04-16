package com.github.rotty3000.spring.osgi.shim;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.function.Supplier;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceRegistration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration(classes = TestBase.class),
	@ContextConfiguration(locations = "classpath:META-INF/spring-osgi.xml"),
})
public class XMLConfigTest extends TestBase {

	/* ***************************************************************************************************
	 * SCENARIO 1: Get OSGi service as bean
	 */
	@Resource(name = "getFooService")
	Supplier<Foo> getFooService;

    @Test
    public void testGetFooService() {
    	assertNotNull(getFooService.get());
    }

	/* ***************************************************************************************************
	 * SCENARIO 2: Get OSGi service as bean, when there is no service
	 */
	@Resource(name = "makeFooService")
    ServiceRegistration<Foo> makeFooServiceRegistration;

    @Test
    public void testMakeFooServiceRegistration() {
    	assertNotNull(makeFooServiceRegistration);
    	assertNotNull(makeFooServiceRegistration.getReference());
    	assertEquals(4, makeFooServiceRegistration.getReference().getPropertyKeys().length);
    	assertArrayEquals(new String[] {AutoCloseable.class.getName(), Foo.class.getName()}, (String[])makeFooServiceRegistration.getReference().getProperty("objectClass"));
    	assertNull(makeFooServiceRegistration.getReference().getProperty("baz"));
    }

}
