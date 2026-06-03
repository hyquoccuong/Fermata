package com.immrhy.learningandroidapptool.fermata.addon;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.immrhy.learningandroidapptool.fermata.BuildConfig;
import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.utils.misc.ChangeableCondition;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceSet;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;

/**
 * @author Andrey Pavlenko
 */
public interface FermataAddon {

	@IdRes
	int getAddonId();

	@NonNull
	AddonInfo getInfo();

	default void contributeSettings(Context ctx, PreferenceStore store, PreferenceSet set,
																	ChangeableCondition visibility) {
	}

	default void install() {
	}

	default void uninstall() {
		stop();
	}

	default void start() {
	}

	default void stop() {
	}

	default boolean handleIntent(MainActivityDelegate a, Intent intent) {
		return false;
	}

	@NonNull
	static AddonInfo findAddonInfo(String name) {
		boolean cn = name.indexOf('.') > 0;
		for (AddonInfo ai : BuildConfig.ADDONS) {
			if (name.equals(cn ? ai.className : ai.moduleName)) return ai;
		}
		throw new RuntimeException("Addon not found: " + name);
	}
}
