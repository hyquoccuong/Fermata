package com.immrhy.learningandroidapptool.utils.net;

import javax.net.ssl.SSLEngine;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;

/**
 * @author Andrey Pavlenko
 */
public interface SslChannel extends NetChannel {

	static FutureSupplier<? extends SslChannel> create(NetChannel channel, SSLEngine engine) {
		return SslChannelImpl.create(channel, engine);
	}
}
