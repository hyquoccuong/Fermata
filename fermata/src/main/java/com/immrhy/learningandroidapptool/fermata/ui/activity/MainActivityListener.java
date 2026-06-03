package com.immrhy.learningandroidapptool.fermata.ui.activity;

import com.immrhy.learningandroidapptool.utils.ui.activity.ActivityDelegate;
import com.immrhy.learningandroidapptool.utils.ui.activity.ActivityListener;

/**
 * @author Andrey Pavlenko
 */
public interface MainActivityListener extends ActivityListener {
	byte MODE_CHANGED = (byte) (LAST << 1);

	void onActivityEvent(MainActivityDelegate a, long e);

	@Override
	default void onActivityEvent(ActivityDelegate a, long e) {
		onActivityEvent((MainActivityDelegate) a, e);
	}
}
