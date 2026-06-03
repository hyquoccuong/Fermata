package com.immrhy.learningandroidapptool.utils.net.http;


import androidx.annotation.Nullable;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;

/**
 * @author Andrey Pavlenko
 */
public interface HttpRequestHandler {

	FutureSupplier<?> handleRequest(HttpRequest req);

	interface Provider {
		@Nullable
		HttpRequestHandler getHandler(CharSequence path, HttpMethod method, HttpVersion version);
	}
}
