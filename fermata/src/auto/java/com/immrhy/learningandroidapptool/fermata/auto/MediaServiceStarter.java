package com.immrhy.learningandroidapptool.fermata.auto;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.immrhy.learningandroidapptool.fermata.media.service.FermataMediaServiceConnection;
import com.immrhy.learningandroidapptool.utils.log.Log;

public class MediaServiceStarter extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Received intent: ", intent);
		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
			Log.i("Connected to bluetooth");
			FermataMediaServiceConnection.connect(null).onSuccess(c -> {
				Log.i("Media service started");
			}).onFailure(Log::e);
		}
	}
}
