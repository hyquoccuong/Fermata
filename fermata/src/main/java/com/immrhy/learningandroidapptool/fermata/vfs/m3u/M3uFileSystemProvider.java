package com.immrhy.learningandroidapptool.fermata.vfs.m3u;

import android.content.Context;

import java.io.File;
import java.util.List;

import com.immrhy.learningandroidapptool.fermata.BuildConfig;
import com.immrhy.learningandroidapptool.fermata.R;
import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.fermata.vfs.VfsProviderBase;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.function.Supplier;
import com.immrhy.learningandroidapptool.utils.log.Log;
import com.immrhy.learningandroidapptool.utils.pref.BasicPreferenceStore;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceSet;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.ui.activity.AppActivity;
import com.immrhy.learningandroidapptool.utils.ui.fragment.FilePickerFragment;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFileSystem;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualResource;

import static com.immrhy.learningandroidapptool.fermata.vfs.m3u.M3uFile.M3U_FILE_AGE;
import static com.immrhy.learningandroidapptool.fermata.vfs.m3u.M3uFile.NAME;
import static com.immrhy.learningandroidapptool.fermata.vfs.m3u.M3uFile.URL;
import static com.immrhy.learningandroidapptool.fermata.vfs.m3u.M3uFile.VIDEO;
import static com.immrhy.learningandroidapptool.utils.async.Completed.completedNull;
import static com.immrhy.learningandroidapptool.utils.async.Completed.completedVoid;
import static com.immrhy.learningandroidapptool.utils.net.http.HttpFileDownloader.AGENT;

/**
 * @author Andrey Pavlenko
 */
public class M3uFileSystemProvider extends VfsProviderBase {

	@Override
	public FutureSupplier<? extends VirtualFileSystem> createFileSystem(
			Context ctx, Supplier<FutureSupplier<? extends AppActivity>> activitySupplier, PreferenceStore ps) {
		return M3uFileSystem.Provider.getInstance().createFileSystem(ps);
	}

	@Override
	public FutureSupplier<? extends VirtualResource> select(MainActivityDelegate a, List<? extends VirtualFileSystem> fs) {
		PreferenceSet prefs = new PreferenceSet();
		PreferenceStore ps = PrefsHolder.instance;
		prefs.addStringPref(o -> {
			o.store = ps;
			o.pref = NAME;
			o.title = R.string.m3u_playlist_name;
		});
		prefs.addFilePref(o -> {
			o.store = ps;
			o.pref = URL;
			o.mode = FilePickerFragment.FILE;
			o.title = com.immrhy.learningandroidapptool.fermata.R.string.m3u_playlist_location;
			o.maxLines = 3;
			o.stringHint = a.getString(com.immrhy.learningandroidapptool.fermata.R.string.m3u_playlist_location_hint);
		});
		prefs.addStringPref(o -> {
			o.store = ps;
			o.pref = AGENT;
			o.title = R.string.m3u_playlist_agent;
			o.stringHint = "Fermata/" + BuildConfig.VERSION_NAME;
		});
		prefs.addBooleanPref(o -> {
			o.store = ps;
			o.pref = VIDEO;
			o.title = R.string.m3u_playlist_video;
		});

		return requestPrefs(a, prefs, ps).thenRun(ps::removeBroadcastListeners).then(ok -> {
			if (!ok) return completedNull();
			return load(ps, M3uFileSystem.getInstance());
		});
	}

	@Override
	protected FutureSupplier<Void> removeFolder(MainActivityDelegate a, VirtualFileSystem fs, VirtualResource m3u) {
		if (m3u instanceof M3uFile) removePlaylist((M3uFile) m3u);
		else Log.e("Unable to delete resource ", m3u);
		return completedVoid();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean validate(PreferenceStore ps) {
		if (!allSet(ps, NAME, URL)) return false;
		String u = ps.getStringPref(URL);
		if (u.startsWith("http://")) return u.length() > 7;
		if (u.startsWith("https://")) return u.length() > 8;
		if (u.startsWith("content://")) return u.length() > 10;
		if (u.startsWith("/")) return new File(u).isFile();
		return false;
	}

	@Override
	protected boolean addRemoveSupported() {
		return false;
	}

	protected FutureSupplier<? extends M3uFile> load(PreferenceStore ps, M3uFileSystem fs) {
		M3uFile f = fs.newFile();
		setPrefs(ps, f);
		return fs.load(f).onCompletion((r, err) -> {
			if (r == null) {
				if (err != null) Log.e(err, "Failed to load playlist: ", ps.getStringPref(URL));
				else Log.e("Playlist URL not found", ps.getStringPref(URL));
				removePlaylist(f);
			}
		});
	}

	protected void setPrefs(PreferenceStore ps, M3uFile f) {
		String agent = ps.getStringPref(AGENT);
		f.setName(ps.getStringPref(NAME));
		f.setUrl(ps.getStringPref(URL));
		f.setVideo(ps.getBooleanPref(VIDEO));
		f.setMaxAge(M3U_FILE_AGE);
		if ((agent != null) && !(agent = agent.trim()).isEmpty()) f.setUserAgent(agent);
	}

	public static void removePlaylist(M3uFile f) {
		Log.d("Removing playlist ", f);
		f.delete();
	}

	private static final class PrefsHolder extends BasicPreferenceStore {
		static final PrefsHolder instance = new PrefsHolder();
	}
}
