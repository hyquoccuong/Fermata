package com.immrhy.learningandroidapptool.utils.misc;

import com.immrhy.learningandroidapptool.utils.BuildConfig;

/**
 * @author Andrey Pavlenko
 */
public class TestUtils {
	static volatile boolean logExceptions = true;

	public static boolean isTestMode() {
		return BuildConfig.D && TestMode.isTestMode;
	}

	public static void enableTestMode() {
		System.setProperty("com.immrhy.learningandroidapptool.utils.testMode", "true");
	}

	public static void enableExceptionLogging(boolean enabled) {
		logExceptions = enabled;
	}

	public static boolean logExceptions() {
		return logExceptions;
	}

	private static final class TestMode {
		static final boolean isTestMode = Boolean.getBoolean("com.immrhy.learningandroidapptool.utils.testMode");
	}
}
