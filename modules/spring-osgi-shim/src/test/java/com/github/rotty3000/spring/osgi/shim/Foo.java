package com.github.rotty3000.spring.osgi.shim;

public interface Foo extends AutoCloseable {
	String sayHello(String to);
}
