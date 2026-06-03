package com.immrhy.learningandroidapptool.utils.holder;

import com.immrhy.learningandroidapptool.utils.function.LongConsumer;
import com.immrhy.learningandroidapptool.utils.function.LongSupplier;

/**
 * @author Andrey Pavlenko
 */
public class LongHolder implements LongConsumer, LongSupplier {
	public long value;

	public LongHolder() {
	}

	public LongHolder(long value) {
		this.value = value;
	}

	public long get() {
		return value;
	}

	@Override
	public long getAsLong() {
		return get();
	}

	public void set(long value) {
		this.value = value;
	}

	@Override
	public void accept(long t) {
		set(t);
	}

	@Override
	public String toString() {
		return String.valueOf(get());
	}
}
