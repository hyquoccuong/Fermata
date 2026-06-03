package com.immrhy.learningandroidapptool.utils.function;

/**
 * @author Andrey Pavlenko
 */
public interface CheckedConsumer<T, E extends Throwable> {
	void accept(T t) throws E;
}
