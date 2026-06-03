package com.immrhy.learningandroidapptool.fermata.addon.cast;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import com.immrhy.learningandroidapptool.fermata.addon.AddonInfo;
import com.immrhy.learningandroidapptool.fermata.addon.FermataAddon;
import com.immrhy.learningandroidapptool.fermata.addon.FermataMediaServiceAddon;
import com.immrhy.learningandroidapptool.fermata.addon.FermataToolAddon;
import com.immrhy.learningandroidapptool.fermata.media.service.MediaSessionCallback;
import com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate;
import com.immrhy.learningandroidapptool.fermata.ui.fragment.MediaLibFragment;
import com.immrhy.learningandroidapptool.fermata.ui.fragment.ToolBarMediator;
import com.immrhy.learningandroidapptool.utils.app.App;
import com.immrhy.learningandroidapptool.utils.io.IoUtils;
import com.immrhy.learningandroidapptool.utils.log.Log;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;
import com.immrhy.learningandroidapptool.utils.ui.view.ToolBarView;

/**
 * @author Andrey Pavlenko
 */
@Keep
@SuppressWarnings("unused")
public class CastAddon
		implements FermataMediaServiceAddon, FermataToolAddon, SessionManagerListener<CastSession> {
	private static final AddonInfo info = FermataAddon.findAddonInfo(CastAddon.class.getName());
	@Nullable
	private MediaSessionCallback cb;
	@Nullable
	private SessionManager sessionMgr;
	@Nullable
	private MediaRouteButton routeButton;
	@Nullable
	private CastMediaEngineProvider engProvider;

	@Override
	public int getAddonId() {
		return com.immrhy.learningandroidapptool.fermata.R.id.cast_addon;
	}

	@NonNull
	@Override
	public AddonInfo getInfo() {
		return info;
	}

	@Override
	public void contributeTool(ToolBarMediator m, ToolBarView tb, ActivityFragment f) {
		if (!(f instanceof MediaLibFragment)) return;
		Context ctx = f.requireContext();
		MainActivityDelegate a = MainActivityDelegate.get(ctx);
		if (a.isCarActivity()) return;
		if (cb == null) onServiceCreate(a.getMediaSessionCallback());

		if (routeButton == null) {
			Resources.Theme theme = ctx.getTheme();
			theme.applyStyle(R.style.RouteButtonStyle, true);
			routeButton = new MediaRouteButton(ctx);
			CastButtonFactory.setUpMediaRouteButton(ctx, routeButton);
			routeButton.setAlwaysVisible(true);
		}

		m.addView(tb, routeButton, R.id.cast_button);
	}

	@Override
	public void onServiceCreate(MediaSessionCallback cb) {
		activate(cb);
	}

	@Override
	public void onServiceDestroy(MediaSessionCallback cb) {
		deactivate();
		this.cb = null;
	}

	public void onActivityPause(MainActivityDelegate a) {
		if (!a.isCarActivity()) routeButton = null;
	}

	@Override
	public void onSessionStarted(@NonNull CastSession session, @NonNull String sessionId) {
		Log.d("Cast session started: ", sessionId);
		connected(session);
	}

	@Override
	public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {
		Log.d("Cast session resumed: ", wasSuspended);
		connected(session);
	}

	@Override
	public void onSessionSuspended(@NonNull CastSession session, int reason) {
		Log.d("Cast session suspended due to : ", reason);
		disconnected();
	}

	@Override
	public void onSessionStartFailed(@NonNull CastSession session, int error) {
		Log.d("Cast session start failed: ", error);
		disconnected();
	}

	@Override
	public void onSessionResumeFailed(@NonNull CastSession castSession, int error) {
		Log.d("Cast session resume failed: ", error);
		disconnected();
	}

	@Override
	public void onSessionEnded(@NonNull CastSession session, int error) {
		Log.d("Cast session ended: ", error);
		disconnected();
	}

	@Override
	public void onSessionStarting(@NonNull CastSession castSession) {
	}

	@Override
	public void onSessionResuming(@NonNull CastSession session, @NonNull String s) {
	}

	@Override
	public void onSessionEnding(@NonNull CastSession castSession) {
		if ((cb == null) || (engProvider == null)) return;
		cb.removeCustomEngineProvider(engProvider);
	}

	@Override
	public void uninstall() {
		deactivate();
		cb = null;
		routeButton = null;
	}

	private void activate(MediaSessionCallback cb) {
		this.cb = cb;
		CastContext.getSharedInstance(cb.getMediaLib().getContext(), App.get().getExecutor())
				.addOnSuccessListener(ctx -> {
					sessionMgr = ctx.getSessionManager();
					sessionMgr.addSessionManagerListener(this, CastSession.class);
					CastSession cs = sessionMgr.getCurrentCastSession();
					if (cs != null) connected(cs);
				}).addOnFailureListener(Log::e);
	}

	private void deactivate() {
		if (sessionMgr == null) return;
		disconnected();
		sessionMgr.removeSessionManagerListener(this, CastSession.class);
		sessionMgr = null;
	}

	private void connected(CastSession session) {
		if (cb == null) return;
		RemoteMediaClient client = session.getRemoteMediaClient();
		if (client == null) return;
		IoUtils.close(engProvider);
		engProvider = new CastMediaEngineProvider(session, client, cb.getMediaLib());
		cb.setCustomEngineProvider(engProvider);
	}

	private void disconnected() {
		IoUtils.close(engProvider);
		if ((cb == null) || (engProvider == null)) return;
		cb.removeCustomEngineProvider(engProvider);
		this.engProvider = null;
	}
}
