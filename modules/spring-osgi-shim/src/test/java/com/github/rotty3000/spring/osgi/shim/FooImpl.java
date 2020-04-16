package com.github.rotty3000.spring.osgi.shim;

public class FooImpl implements AutoCloseable, Foo {

	@Override
	public void close() throws Exception {
	}

	@Override
	public String sayHello(String to) {
		return "Hey there, " + to + "!";
	}

}
