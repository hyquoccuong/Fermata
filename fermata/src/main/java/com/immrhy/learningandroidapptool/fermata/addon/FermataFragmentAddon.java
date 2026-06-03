package com.immrhy.learningandroidapptool.fermata.addon;

import androidx.annotation.NonNull;

import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;

/**
 * @author Andrey Pavlenko
 */
public interface FermataFragmentAddon extends FermataAddon {

	@NonNull
	ActivityFragment createFragment();

	default int getFragmentId() {
		return getAddonId();
	}
}
