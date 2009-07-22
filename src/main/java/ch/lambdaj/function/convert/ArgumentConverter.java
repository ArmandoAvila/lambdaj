// Modified or written by Ex Machina SAGL for inclusion with lambdaj.
// Copyright (c) 2009 Mario Fusco, Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package ch.lambdaj.function.convert;

import static ch.lambdaj.function.argument.ArgumentsFactory.*;
import ch.lambdaj.function.argument.*;

/**
 * @author Mario Fusco
 */
public class ArgumentConverter<F, T> implements Converter<F, T>{

	private Argument<T> argument;
	
	public ArgumentConverter(Argument<T> argument) {
		this.argument = argument;
	}
	
	public ArgumentConverter(T argument) {
		this(actualArgument(argument));
	}
	
	public T convert(F from) {
		return argument.evaluate(from);
	}

}