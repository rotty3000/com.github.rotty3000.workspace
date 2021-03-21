package com.github.rotty3000.spring.osgi.loader;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Component(immediate = true)
public class OSGiAnnotationConfigApplicationContext extends AnnotationConfigApplicationContext {

	private volatile Optional<ConfigurableApplicationContext> context;

	@Activate
	void activate(final Map<String, Object> properties) {
		context = tccl(() -> new AnnotationConfigApplicationContext(), getClass().getClassLoader());
	}

	@Deactivate
	void deactivate() {
		context.ifPresent(ConfigurableApplicationContext::close);
	}

	static <T> Optional<T> tccl(Supplier<T> supplier, ClassLoader loader) {
		Thread currentThread = Thread.currentThread();
		ClassLoader current = currentThread.getContextClassLoader();

		try {
			currentThread.setContextClassLoader(loader);

			return Optional.of(supplier.get());
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			currentThread.setContextClassLoader(current);
		}

		return Optional.empty();
	}

}
