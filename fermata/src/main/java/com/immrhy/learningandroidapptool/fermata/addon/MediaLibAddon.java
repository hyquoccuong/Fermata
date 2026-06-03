package com.immrhy.learningandroidapptool.fermata.addon;

import androidx.annotation.Nullable;

import com.immrhy.learningandroidapptool.fermata.media.lib.DefaultMediaLib;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Item;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;

/**
 * @author Andrey Pavlenko
 */
public interface MediaLibAddon extends FermataFragmentAddon {

	boolean isSupportedItem(Item i);

	Item getRootItem(DefaultMediaLib lib);

	@Nullable
	FutureSupplier<? extends Item> getItem(DefaultMediaLib lib, @Nullable String scheme, String id);
}
