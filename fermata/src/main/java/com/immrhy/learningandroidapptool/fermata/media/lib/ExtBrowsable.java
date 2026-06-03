package com.immrhy.learningandroidapptool.fermata.media.lib;

import androidx.annotation.Nullable;

import java.util.List;

import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.BrowsableItem;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualResource;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completedEmptyList;

/**
 * @author Andrey Pavlenko
 */
public abstract class ExtBrowsable extends BrowsableItemBase {

	public ExtBrowsable(String id, @Nullable BrowsableItem parent, @Nullable VirtualResource resource) {
		super(id, parent, resource);
	}

	@Override
	protected FutureSupplier<List<MediaLib.Item>> listChildren() {
		return completedEmptyList();
	}

	@Override
	public boolean isExternal() {
		return true;
	}
}
