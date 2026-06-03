package com.immrhy.learningandroidapptool.fermata.media.lib;

import androidx.annotation.NonNull;

import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.BrowsableItem;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualResource;

/**
 * @author Andrey Pavlenko
 */
public class ExtPlayable extends PlayableItemBase {

	public ExtPlayable(String id, @NonNull BrowsableItem parent, @NonNull VirtualResource resource) {
		super(id, parent, resource);
	}

	@Override
	public boolean isVideo() {
		return false;
	}

	@Override
	public String getOrigId() {
		return getId();
	}

	@Override
	public boolean isExternal() {
		return true;
	}
}
