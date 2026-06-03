package com.immrhy.learningandroidapptool.fermata.media.service;

import androidx.annotation.NonNull;

import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Item;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;

/**
 * @author Andrey Pavlenko
 */
public interface MediaSessionCallbackAssistant {

	default void startVoiceAssistant(){}

	@NonNull
	default FutureSupplier<MediaLib.PlayableItem> getPrevPlayable(Item i) {
		return i.getPrevPlayable();
	}

	@NonNull
	default FutureSupplier<MediaLib.PlayableItem> getNextPlayable(Item i) {
		return i.getNextPlayable();
	}
}
