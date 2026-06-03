package com.immrhy.learningandroidapptool.fermata.addon.tv;

import android.view.View;

import androidx.annotation.Nullable;

import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.fermata.ui.fragment.FloatingButtonMediator;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;
import com.immrhy.learningandroidapptool.utils.ui.view.FloatingButton;

/**
 * @author Andrey Pavlenko
 */
class TvFloatingButtonMediator extends FloatingButtonMediator {
	static final TvFloatingButtonMediator instance = new TvFloatingButtonMediator();

	@Override
	public int getIcon(FloatingButton fb) {
		MainActivityDelegate a = MainActivityDelegate.get(fb.getContext());
		return (a.isVideoMode() || !a.isRootPage()) ? getBackIcon() : R.drawable.tv_add;
	}

	@Override
	public void onClick(View v) {
		MainActivityDelegate a = MainActivityDelegate.get(v.getContext());

		if (a.isVideoMode() || !a.isRootPage()) {
			a.onBackPressed();
		} else {
			ActivityFragment f = a.getActiveFragment();
			if (f instanceof TvFragment) ((TvFragment) f).addSource();
		}
	}
}
