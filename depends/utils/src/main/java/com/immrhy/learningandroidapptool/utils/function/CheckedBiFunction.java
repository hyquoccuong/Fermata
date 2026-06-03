package com.immrhy.learningandroidapptool.utils.function;

/**
 * @author Andrey Pavlenko
 */
public interface CheckedBiFunction<T, U, R, E extends Throwable> {
	R apply(T t, U u) throws E;
}
