package com.immrhy.learningandroidapptool.utils.holder;


import com.immrhy.learningandroidapptool.utils.function.IntConsumer;
import com.immrhy.learningandroidapptool.utils.function.IntSupplier;

/**
 * @author Andrey Pavlenko
 */
public class IntHolder implements IntConsumer, IntSupplier {
	public int value;

	public IntHolder() {
	}

	public IntHolder(int value) {
		this.value = value;
	}

	public int get() {
		return value;
	}

	@Override
	public int getAsInt() {
		return get();
	}

	public void set(int value) {
		this.value = value;
	}

	@Override
	public void accept(int t) {
		set(t);
	}

	@Override
	public String toString() {
		return String.valueOf(get());
	}
}
