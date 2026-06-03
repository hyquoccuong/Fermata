package com.immrhy.learningandroidapptool.utils.net.http;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.log.Log;
import com.immrhy.learningandroidapptool.utils.net.NetChannel;

/**
 * @author Andrey Pavlenko
 */
public interface HttpResponseHandler {

	FutureSupplier<?> handleResponse(HttpResponse resp);

	default void onFailure(NetChannel channel, Throwable fail) {
		channel.close();
		Log.d(fail, "Failed to receive HTTP response");
	}
}
