package com.immrhy.learningandroidapptool.fermata.vfs;

import android.content.Context;

import java.util.List;

import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.function.Supplier;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.ui.activity.AppActivity;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFileSystem;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFolder;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualResource;

/**
 * @author Andrey Pavlenko
 */
public interface VfsProvider {

	FutureSupplier<? extends VirtualFileSystem> createFileSystem(
			Context ctx, Supplier<FutureSupplier<? extends AppActivity>> activitySupplier,
			PreferenceStore ps);

	FutureSupplier<? extends VirtualResource> select(MainActivityDelegate a, List<? extends VirtualFileSystem> fs);
}
