package com.immrhy.learningandroidapptool.utils.function;

/**
 * @author Andrey Pavlenko
 */
public interface CheckedFunction<T, R, E extends Throwable> {
	R apply(T t) throws E;
}
