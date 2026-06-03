package com.immrhy.learningandroidapptool.fermata.addon.tv;

import static java.util.Objects.requireNonNull;
import static com.immrhy.learningandroidapptool.utils.async.Completed.completed;
import static com.immrhy.learningandroidapptool.utils.function.ResultConsumer.Cancel.isCancellation;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.concurrent.CancellationException;

import com.immrhy.learningandroidapptool.fermata.addon.AddonManager;
import com.immrhy.learningandroidapptool.fermata.addon.tv.m3u.TvM3uFile;
import com.immrhy.learningandroidapptool.fermata.addon.tv.m3u.TvM3uFileSystem;
import com.immrhy.learningandroidapptool.fermata.addon.tv.m3u.TvM3uFileSystemProvider;
import com.immrhy.learningandroidapptool.fermata.addon.tv.m3u.TvM3uItem;
import com.immrhy.learningandroidapptool.fermata.media.lib.DefaultMediaLib;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.BrowsableItem;
import com.immrhy.learningandroidapptool.fermata.media.lib.MediaLib.Item;
import com.immrhy.learningandroidapptool.fermata.media.service.FermataServiceUiBinder;
import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.fermata.ui.fragment.MediaLibFragment;
import com.immrhy.learningandroidapptool.fermata.ui.view.MediaItemMenuHandler;
import com.immrhy.learningandroidapptool.utils.app.App;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.log.Log;
import com.immrhy.learningandroidapptool.utils.ui.UiUtils;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;
import com.immrhy.learningandroidapptool.utils.ui.menu.OverlayMenu;
import com.immrhy.learningandroidapptool.utils.ui.menu.OverlayMenuItem;
import com.immrhy.learningandroidapptool.utils.ui.view.FloatingButton;

/**
 * @author Andrey Pavlenko
 */
public class TvFragment extends MediaLibFragment {

	@Override
	protected ListAdapter createAdapter(FermataServiceUiBinder b) {
		return new TvAdapter(getMainActivity(), getRootItem());
	}

	@Override
	public CharSequence getFragmentTitle() {
		return getResources().getString(com.immrhy.learningandroidapptool.fermata.R.string.addon_name_tv);
	}

	@Override
	public int getFragmentId() {
		return com.immrhy.learningandroidapptool.fermata.R.id.tv_fragment;
	}

	@Override
	public FloatingButton.Mediator getFloatingButtonMediator() {
		return TvFloatingButtonMediator.instance;
	}

	public void navBarItemReselected(int itemId) {
		getAdapter().setParent(getRootItem());
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) return;

		TvAdapter a = getAdapter();
		if (a != null) a.animateAddButton(a.getParent());
	}

	@Override
	public void switchingTo(@NonNull ActivityFragment newFragment) {
		super.switchingTo(newFragment);
		getMainActivity().getFloatingButton().clearAnimation();
	}

	public void addSource() {
		TvM3uFileSystemProvider prov = new TvM3uFileSystemProvider();
		prov.select(getMainActivity(), Collections.singletonList(TvM3uFileSystem.getInstance())).main()
				.onFailure(this::failedToAddSource).onSuccess(this::addM3uSource);
	}

	public TvRootItem getRootItem() {
		return requireNonNull(AddonManager.get().getAddon(TvAddon.class)).getRootItem(
				(DefaultMediaLib) getMainActivity().getLib());
	}

	@Override
	public void contributeToContextMenu(OverlayMenu.Builder b, MediaItemMenuHandler h) {
		if (!(h.getItem() instanceof TvM3uItem)) return;
		b.addItem(com.immrhy.learningandroidapptool.fermata.R.id.edit, com.immrhy.learningandroidapptool.fermata.R.drawable.edit,
						com.immrhy.learningandroidapptool.fermata.R.string.edit).setData(h.getItem())
				.setHandler(this::contextMenuItemSelected);
		b.addItem(com.immrhy.learningandroidapptool.fermata.R.id.delete, com.immrhy.learningandroidapptool.fermata.R.drawable.delete,
						com.immrhy.learningandroidapptool.fermata.R.string.delete).setData(h.getItem())
				.setHandler(this::contextMenuItemSelected);
		super.contributeToContextMenu(b, h);
	}

	private boolean contextMenuItemSelected(OverlayMenuItem item) {
		int id = item.getItemId();
		if (id == com.immrhy.learningandroidapptool.fermata.R.id.edit) {
			TvM3uItem i = item.getData();
			new TvM3uFileSystemProvider().edit(getMainActivity(), i.getResource())
					.onCompletion((ok, err) -> {
						if ((err != null) && !(err instanceof CancellationException)) {
							Log.e(err, "Failed to edit TV source ", i);
							UiUtils.showAlert(getContext(), err.getLocalizedMessage());
						}
						getMainActivity().showFragment(getFragmentId());
						if ((ok != null) && ok) i.refresh().thenRun(this::refresh);
					});
		} else if (id == com.immrhy.learningandroidapptool.fermata.R.id.delete) {
			TvRootItem root = getRootItem();
			root.removeItem(item.getData()).onSuccess(v -> getAdapter().setParent(root));
		}
		return true;
	}

	@Override
	public void contributeToNavBarMenu(OverlayMenu.Builder builder) {
		super.contributeToNavBarMenu(builder);
		if (isRootItem()) return;
		TvAdapter a = getAdapter();

		if (a.getListView().isSelectionActive() && a.hasSelectable() && a.hasSelected()) {
			OverlayMenu.Builder b = builder.withSelectionHandler(this::navBarMenuItemSelected);
			b.addItem(com.immrhy.learningandroidapptool.fermata.R.id.favorites_add, com.immrhy.learningandroidapptool.fermata.R.drawable.favorite,
					com.immrhy.learningandroidapptool.fermata.R.string.favorites_add);
			getMainActivity().addPlaylistMenu(b, completed(a.getSelectedItems()));
		}
	}

	@Override
	protected boolean isSupportedItem(Item i) {
		return getRootItem().isChildItemId(i.getId());
	}

	@Override
	protected boolean isRefreshSupported() {
		return true;
	}

	private void addM3uSource(TvM3uFile m3u) {
		MainActivityDelegate a = getMainActivity();
		if (m3u != null) getRootItem().addSource(m3u);
		getAdapter().setParent(getRootItem());
		a.showFragment(getFragmentId());
	}

	private void failedToAddSource(Throwable ex) {
		getMainActivity().showFragment(com.immrhy.learningandroidapptool.fermata.R.id.tv_fragment);
		if (isCancellation(ex)) return;

		App.get().getHandler().post(() -> {
			String msg = ex.getLocalizedMessage();
			UiUtils.showAlert(getContext(),
					getString(R.string.err_failed_to_add_tv_source, (msg != null) ? msg : ex.toString()));
		});
	}

	private boolean isRootItem() {
		BrowsableItem p = getAdapter().getParent();
		return (p == null) || (p instanceof TvRootItem);
	}

	private class TvAdapter extends ListAdapter {

		TvAdapter(MainActivityDelegate activity, BrowsableItem parent) {
			super(activity, parent);
			animateAddButton(parent);
		}

		@Override
		public FutureSupplier<?> setParent(BrowsableItem parent, boolean userAction) {
			return super.setParent(parent, userAction).onSuccess(v -> animateAddButton(parent));
		}

		public boolean isLongPressDragEnabled() {
			return isRootItem();
		}

		@Override
		protected void onItemDismiss(int position) {
			BrowsableItem i = getAdapter().getParent();
			if (i instanceof TvRootItem) ((TvRootItem) i).removeItem(position);
			super.onItemDismiss(position);
		}

		@Override
		protected boolean onItemMove(int fromPosition, int toPosition) {
			BrowsableItem i = getAdapter().getParent();
			if (i instanceof MediaLib.Folders) ((MediaLib.Folders) i).moveItem(fromPosition, toPosition);
			return super.onItemMove(fromPosition, toPosition);
		}

		private void animateAddButton(BrowsableItem parent) {
			if (!(parent instanceof TvRootItem)) return;

			parent.getUnsortedChildren().onSuccess(c -> {
				if (!c.isEmpty()) return;

				FloatingButton fb = getMainActivity().getFloatingButton();
				fb.requestFocus();
				Animation shake =
						AnimationUtils.loadAnimation(getContext(), com.immrhy.learningandroidapptool.utils.R.anim.shake_y_20);
				fb.startAnimation(shake);
			});
		}
	}
}
