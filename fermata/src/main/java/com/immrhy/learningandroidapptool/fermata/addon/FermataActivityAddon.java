package com.immrhy.learningandroidapptool.fermata.addon;

import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;

/**
 * @author Andrey Pavlenko
 */
public interface FermataActivityAddon extends FermataAddon {

	default void onActivityCreate(MainActivityDelegate a) {
	}

	default void onActivityDestroy(MainActivityDelegate a) {
	}

	default void onActivityResume(MainActivityDelegate a) {
	}

	default void onActivityPause(MainActivityDelegate a) {
	}
}
