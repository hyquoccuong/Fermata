package com.immrhy.learningandroidapptool.fermata.vfs.gdrive;

import android.content.Context;

import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.fermata.vfs.VfsProviderBase;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.function.Supplier;
import com.immrhy.learningandroidapptool.utils.pref.BasicPreferenceStore;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.ui.activity.AppActivity;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFileSystem;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualResource;
import com.immrhy.learningandroidapptool.utils.vfs.gdrive.GdriveFileSystem;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completedNull;
import static com.immrhy.learningandroidapptool.utils.async.Completed.completedVoid;
import static com.immrhy.learningandroidapptool.utils.vfs.gdrive.GdriveFileSystem.Provider.GOOGLE_TOKEN;

/**
 * @author Andrey Pavlenko
 */
public class Provider extends VfsProviderBase {

	@Override
	public FutureSupplier<VirtualFileSystem> createFileSystem(
			Context ctx, Supplier<FutureSupplier<? extends AppActivity>> activitySupplier, PreferenceStore ps) {
		BasicPreferenceStore store = new BasicPreferenceStore();
		store.applyStringPref(GOOGLE_TOKEN, ctx.getString(com.immrhy.learningandroidapptool.fermata.R.string.default_web_client_id));
		return new GdriveFileSystem.Provider(activitySupplier).createFileSystem(store);
	}

	@Override
	protected boolean addRemoveSupported() {
		return false;
	}

	@Override
	protected FutureSupplier<? extends VirtualResource> addFolder(MainActivityDelegate a, VirtualFileSystem fs) {
		return completedNull();
	}

	@Override
	protected FutureSupplier<Void> removeFolder(MainActivityDelegate a, VirtualFileSystem fs, VirtualResource folder) {
		return completedVoid();
	}
}
