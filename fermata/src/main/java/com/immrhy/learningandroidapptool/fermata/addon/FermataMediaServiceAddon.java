package com.immrhy.learningandroidapptool.fermata.addon;

import com.immrhy.learningandroidapptool.fermata.media.service.FermataMediaService;
import com.immrhy.learningandroidapptool.fermata.media.service.MediaSessionCallback;

/**
 * @author Andrey Pavlenko
 */
public interface FermataMediaServiceAddon extends FermataAddon {

	default void onServiceCreate(MediaSessionCallback cb) {
	}

	default void onServiceDestroy(MediaSessionCallback cb) {
	}
}
