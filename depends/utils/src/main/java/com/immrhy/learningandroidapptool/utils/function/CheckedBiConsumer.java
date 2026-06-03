package com.immrhy.learningandroidapptool.utils.function;

/**
 * @author Andrey Pavlenko
 */
public interface CheckedBiConsumer<T, U, E extends Throwable> {
	void accept(T t, U u) throws E;
}
