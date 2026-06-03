package com.immrhy.learningandroidapptool.fermata.media.lib;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completed;
import static com.immrhy.learningandroidapptool.utils.async.Completed.completedNull;
import static com.immrhy.learningandroidapptool.utils.async.Completed.failed;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.immrhy.learningandroidapptool.fermata.R;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.BrowsableItem;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Item;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Playlist;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Playlists;
import com.immrhy.learningandroidapptool.fermata.media.pref.BrowsableItemPrefs;
import com.immrhy.learningandroidapptool.fermata.media.pref.PlaylistsPrefs;
import com.immrhy.learningandroidapptool.utils.async.Async;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.collection.CollectionUtils;
import com.immrhy.learningandroidapptool.utils.holder.Holder;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.text.SharedTextBuilder;

/**
 * @author Andrey Pavlenko
 */
class DefaultPlaylists extends ItemContainer<Playlist> implements Playlists, PlaylistsPrefs {
	static final String ID = "Playlists";
	static final String SCHEME = "playlist";
	private final DefaultMediaLib lib;

	public DefaultPlaylists(DefaultMediaLib lib) {
		super(ID, null, null);
		this.lib = lib;
	}

	@Override
	protected FutureSupplier<String> buildTitle() {
		return completed(getLib().getContext().getString(R.string.playlists));
	}

	@Override
	protected FutureSupplier<String> buildSubtitle() {
		return completed("");
	}

	@NonNull
	@Override
	public DefaultMediaLib getLib() {
		return lib;
	}

	@Override
	public BrowsableItem getParent() {
		return null;
	}

	@NonNull
	@Override
	public PreferenceStore getParentPreferenceStore() {
		return getLib();
	}

	@NonNull
	@Override
	public BrowsableItem getRoot() {
		return this;
	}

	@NonNull
	@Override
	public BrowsableItemPrefs getPrefs() {
		return this;
	}

	@Override
	public FutureSupplier<List<Item>> listChildren() {
		int[] ids = getPlaylistIdsPref();
		DefaultMediaLib lib = getLib();
		List<Item> children = new ArrayList<>(ids.length);

		for (int id : ids) {
			SharedTextBuilder tb = SharedTextBuilder.get();
			tb.append(SCHEME).append(':').append(id).append(':');
			children.add(DefaultPlaylist.create(tb.releaseString(), this, id, lib));
		}

		return completed(children);
	}

	@Override
	FutureSupplier<Item> getItem(String id) {
		assert id.startsWith(SCHEME);
		FutureSupplier<Item> none = completedNull();
		Holder<FutureSupplier<Item>> h = new Holder<>(none);

		return list().then(playlists -> {
			if (playlists.isEmpty()) return completedNull();
			Iterator<Playlist> plIt = playlists.iterator();

			return Async.iterate(() -> {
				if ((h.value != none) || !plIt.hasNext()) return null;
				Playlist pl = plIt.next();
				if (!id.startsWith(pl.getId())) return completedNull();
				if (id.equals(pl.getId())) return h.value = completed(pl);

				return pl.getUnsortedChildren().then(children -> {
					if (children.isEmpty()) return completedNull();
					Iterator<Item> chIt = children.iterator();

					return Async.iterate(() -> {
						if ((h.value != none) || !chIt.hasNext()) return null;
						Item c = chIt.next();
						if (id.equals(c.getId())) return h.value = completed(c);
						return completedNull();
					});
				});
			});
		}).then(v -> h.value);
	}

	@Override
	public boolean isPlaylistsItemId(String id) {
		return isChildItemId(id);
	}

	@Override
	public FutureSupplier<Playlist> addItem(CharSequence name) {
		String n = name.toString().trim();

		if (n.isEmpty() || (n.indexOf('/') != -1)) {
			Context ctx = getLib().getContext();
			String err = ctx.getResources().getString(R.string.err_invalid_playlist_name, n);
			return failed(new IllegalArgumentException(err));
		}

		return list().then(list -> {
			if (CollectionUtils.contains(list, c -> n.equals(((Playlist) c).getName()))) {
				Context ctx = getLib().getContext();
				String err = ctx.getResources().getString(R.string.err_playlist_exists, n);
				return failed(new IllegalArgumentException(err));
			}

			int playlistId = getPlaylistsCounterPref() + 1;
			SharedTextBuilder tb = SharedTextBuilder.get();
			tb.append(SCHEME).append(':').append(playlistId).append(':');
			DefaultPlaylist pl = DefaultPlaylist.create(tb.releaseString(), this, playlistId, getLib());
			setPlaylistsCounterPref(playlistId);
			pl.setPlaylistNamePref(n);
			return super.addItem(pl).map(v -> pl);
		});
	}

	@Override
	protected String getScheme() {
		return SCHEME;
	}

	@Override
	public FutureSupplier<Void> addItem(Playlist i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FutureSupplier<Void> addItems(List<Playlist> items) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void saveChildren(List<Playlist> children) {
		setPlaylistIdsPref(CollectionUtils.map(children, (i, t, a) -> a[i] = ((DefaultPlaylist) t).getPlaylistId(), int[]::new));
	}

	@Override
	public boolean getTitleSeqNumPref() {
		return false;
	}

	@Override
	protected void itemAdded(Playlist i) {
		getLib().getAtvInterface(a -> a.addChannel(i));
	}

	@Override
	protected void itemRemoved(Playlist i) {
		super.itemRemoved(i);
		getLib().getAtvInterface(a -> a.removeChannel(i));
	}
}