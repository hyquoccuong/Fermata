package com.immrhy.learningandroidapptool.fermata.media.lib;

import static com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate.INTENT_ACTION_UPDATE;
import static com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate.intentUriToAction;
import static com.immrhy.learningandroidapptool.fermata.ui.activity.MainActivityDelegate.intentUriToId;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.immrhy.learningandroidapptool.fermata.media.service.FermataMediaServiceConnection;
import com.immrhy.learningandroidapptool.utils.log.Log;

/**
 * @author Andrey Pavlenko
 */
public class AtvUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent i) {
		if (!INTENT_ACTION_UPDATE.equals(intentUriToAction(i.getData()))) return;
		Log.d("Intent received: ", intentUriToId(i.getData()));
		FermataMediaServiceConnection.connect(null).onSuccess(b -> {
			b.getMediaSessionCallback().getMediaLib().getAtvInterface(a -> a.update(i));
			b.disconnect();
		});
	}
}
