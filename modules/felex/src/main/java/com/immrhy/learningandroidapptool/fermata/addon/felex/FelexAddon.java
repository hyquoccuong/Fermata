package com.immrhy.learningandroidapptool.fermata.addon.felex;

import static android.speech.RecognitionService.SERVICE_INTERFACE;
import static java.util.Objects.requireNonNull;
import static com.immrhy.learningandroidapptool.fermata.addon.felex.dict.DictMgr.DICT_EXT;
import static com.immrhy.learningandroidapptool.fermata.util.Utils.getAddonsCacheDir;
import static com.immrhy.learningandroidapptool.fermata.util.Utils.getAddonsFileDir;
import static com.immrhy.learningandroidapptool.fermata.util.Utils.isExternalStorageManager;
import static com.immrhy.learningandroidapptool.fermata.util.Utils.isSafSupported;
import static com.immrhy.learningandroidapptool.utils.io.FileUtils.getFileExtension;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.IdRes;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import com.immrhy.learningandroidapptool.fermata.FermataApplication;
import com.immrhy.learningandroidapptool.fermata.addon.AddonInfo;
import com.immrhy.learningandroidapptool.fermata.addon.FermataAddon;
import com.immrhy.learningandroidapptool.fermata.addon.FermataContentAddon;
import com.immrhy.learningandroidapptool.fermata.addon.MediaLibAddon;
import com.immrhy.learningandroidapptool.fermata.addon.felex.dict.DictInfo;
import com.immrhy.learningandroidapptool.fermata.addon.felex.media.FelexItem;
import com.immrhy.learningandroidapptool.fermata.addon.felex.view.FelexFragment;
import com.immrhy.learningandroidapptool.fermata.media.lib.DefaultMediaLib;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib;
import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.function.BooleanSupplier;
import com.immrhy.learningandroidapptool.utils.function.IntSupplier;
import com.immrhy.learningandroidapptool.utils.function.Supplier;
import com.immrhy.learningandroidapptool.utils.io.FileUtils;
import com.immrhy.learningandroidapptool.utils.log.Log;
import com.immrhy.learningandroidapptool.utils.misc.ChangeableCondition;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceSet;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore.Pref;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;
import com.immrhy.learningandroidapptool.utils.ui.fragment.FilePickerFragment;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFolder;

/**
 * @author Andrey Pavlenko
 */
@Keep
@SuppressWarnings("unused")
public class FelexAddon implements MediaLibAddon, FermataContentAddon {
	private static final AddonInfo info = FermataAddon.findAddonInfo(FelexAddon.class.getName());
	public static final Pref<Supplier<String>> DICT_FOLDER =
			Pref.s("FELEX_DICT_FOLDER", () -> getAddonsFileDir(info).getAbsolutePath());
	public static final Pref<Supplier<String>> CACHE_FOLDER =
			Pref.s("FELEX_CACHE_FOLDER", () -> getAddonsCacheDir(info).getAbsolutePath());
	public static final Pref<BooleanSupplier> OFFLINE_MODE = Pref.b("FELEX_OFFLINE_MODE", false);
	public static final Pref<IntSupplier> STT_SERVICE = Pref.i("FELEX_STT_SERVICE", 0);

	public static FelexAddon get() {
		return FermataApplication.get().getAddonManager().getAddon(FelexAddon.class);
	}

	@IdRes
	@Override
	public int getAddonId() {
		return com.immrhy.learningandroidapptool.fermata.R.id.felex_fragment;
	}

	@NonNull
	@Override
	public AddonInfo getInfo() {
		return info;
	}

	@Override
	public boolean isSupportedItem(MediaLib.Item i) {
		return i instanceof FelexItem;
	}

	@Override
	public MediaLib.Item getRootItem(DefaultMediaLib lib) {
		return new FelexItem.Root(lib);
	}

	@Nullable
	@Override
	public FutureSupplier<? extends MediaLib.Item> getItem(DefaultMediaLib lib,
																												 @Nullable String scheme, String id) {
		return FelexItem.getItem(lib, scheme, id);
	}

	@Override
	public boolean handleIntent(MainActivityDelegate a, Intent intent) {
		Uri u = intent.getData();
		if (u == null) return false;

		String s = u.getScheme();
		if ((s == null) || (!s.equals("file") && !s.equals("content"))) return false;

		String ext = FileUtils.getFileExtension(u.getPath());

		if ((ext != null) && DICT_EXT.regionMatches(1, ext, 0, ext.length())) {
			a.showFragment(getFragmentId(), u);
			return true;
		}

		try (InputStream in = a.getContext().getContentResolver().openInputStream(u)) {
			if (DictInfo.read(in) == null) return false;
		} catch (IOException ex) {
			Log.d(ex, "Failed to read uri: ", u);
			return false;
		}

		a.showFragment(getFragmentId(), u);
		return true;
	}

	@Nullable
	@Override
	public String getFileType(Uri uri, String displayName) {
		if (displayName == null) displayName = uri.getPath();
		String ext = getFileExtension(displayName);
		if ((ext != null) && DICT_EXT.regionMatches(1, ext, 0, ext.length())) return "text/plain";
		return FermataContentAddon.super.getFileType(uri, displayName);

	}

	public FutureSupplier<VirtualFolder> getDictFolder() {
		return getFolder(DICT_FOLDER);
	}

	public FutureSupplier<VirtualFolder> getCacheFolder() {
		return getFolder(CACHE_FOLDER);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private FutureSupplier<VirtualFolder> getFolder(Pref<Supplier<String>> p) {
		String uri = getPreferenceStore().getStringPref(p);
		if (uri.startsWith("/")) {
			if (p.getDefaultValue().get().equals(uri)) new File(uri).mkdirs();
			uri = "file:/" + uri;
		}
		return FermataApplication.get().getVfsManager().getFolder(uri);
	}

	public boolean isOfflineMode() {
		return getPreferenceStore().getBooleanPref(OFFLINE_MODE);
	}

	public void setOfflineMode(boolean offline) {
		getPreferenceStore().applyBooleanPref(OFFLINE_MODE, offline);
	}

	@Nullable
	public ComponentName getSttServiceName() {
		var services = getSttServices();
		var idx = getPreferenceStore().getIntPref(STT_SERVICE);
		if (idx >= services.size()) return null;
		var name = services.get(services.keySet().toArray(new String[0])[idx]);
		return ComponentName.unflattenFromString(requireNonNull(name));

	}

	@NonNull
	@Override
	public ActivityFragment createFragment() {
		return new FelexFragment();
	}

	@Override
	public void contributeSettings(Context ctx, PreferenceStore store, PreferenceSet set,
																 ChangeableCondition visibility) {
		contributeFolder(DICT_FOLDER, R.string.dict_folder, set, visibility);
		contributeFolder(CACHE_FOLDER, R.string.cache_folder, set, visibility);
		set.addListPref(o -> {
			var services = getSttServices();
			var names = services.keySet().toArray(new String[0]);
			if (store.getIntPref(STT_SERVICE) >= names.length) store.applyIntPref(STT_SERVICE, 0);
			o.store = store;
			o.pref = STT_SERVICE;
			o.stringValues = names;
			o.title = R.string.stt_service;
			o.subtitle = com.immrhy.learningandroidapptool.fermata.R.string.string_format;
			o.formatSubtitle = true;
			o.visibility = visibility;
		});
	}

	private void contributeFolder(Pref<Supplier<String>> p, int title, PreferenceSet set,
																ChangeableCondition visibility) {
		set.addFilePref(o -> {
			o.pref = p;
			o.store = getPreferenceStore();
			o.mode = FilePickerFragment.FOLDER | FilePickerFragment.WRITABLE;
			o.title = title;
			o.visibility = visibility;
			o.trim = true;
			o.removeBlank = true;
			o.useSaf = !isExternalStorageManager() && isSafSupported(null);
		});
	}

	private PreferenceStore getPreferenceStore() {
		return FermataApplication.get().getPreferenceStore();
	}

	private static Map<String, String> getSttServices() {
		var services = new TreeMap<String, String>();
		services.put("", "");
		var mgr = FermataApplication.get().getPackageManager();
		for (var service : mgr.queryIntentServices(new Intent(SERVICE_INTERFACE), 0)) {
			services.put(service.loadLabel(mgr).toString(),
					service.serviceInfo.packageName + "/" + service.serviceInfo.name);
		}
		return services;
	}
}
