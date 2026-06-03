package com.immrhy.learningandroidapptool.utils.function;

/**
 * @author Andrey Pavlenko
 */
public interface CheckedSupplier<R, E extends Throwable> {
	R get() throws E;
}
