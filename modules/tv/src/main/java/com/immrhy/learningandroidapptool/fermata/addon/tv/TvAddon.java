package com.immrhy.learningandroidapptool.fermata.addon.tv;

import androidx.annotation.IdRes;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.immrhy.learningandroidapptool.fermata.addon.AddonInfo;
import com.immrhy.learningandroidapptool.fermata.addon.FermataAddon;
import com.immrhy.learningandroidapptool.fermata.addon.MediaLibAddon;
import com.immrhy.learningandroidapptool.fermata.media.lib.DefaultMediaLib;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Item;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;

/**
 * @author Andrey Pavlenko
 */
@Keep
@SuppressWarnings("unused")
public class TvAddon implements MediaLibAddon {
	@NonNull
	private static final AddonInfo info = FermataAddon.findAddonInfo(TvAddon.class.getName());
	private static TvRootItem root;

	@IdRes
	@Override
	public int getAddonId() {
		return com.immrhy.learningandroidapptool.fermata.R.id.tv_fragment;
	}

	@NonNull
	@Override
	public AddonInfo getInfo() {
		return info;
	}

	@NonNull
	@Override
	public ActivityFragment createFragment() {
		return new TvFragment();
	}

	@Override
	public boolean isSupportedItem(Item i) {
		return (i instanceof TvItem);
	}

	public TvRootItem getRootItem(DefaultMediaLib lib) {
		if ((root == null) || (root.getLib() != lib)) root = new TvRootItem(lib);
		return root;
	}

	@Nullable
	@Override
	public FutureSupplier<? extends Item> getItem(DefaultMediaLib lib, @Nullable String scheme,
																								String id) {
		return getRootItem(lib).getItem(scheme, id);
	}
}
