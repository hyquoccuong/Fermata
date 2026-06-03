package com.immrhy.learningandroidapptool.utils.net.http;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.immrhy.learningandroidapptool.utils.function.CheckedConsumer;
import com.immrhy.learningandroidapptool.utils.function.Function;
import com.immrhy.learningandroidapptool.utils.net.ByteBufferArraySupplier;

/**
 * @author Andrey Pavlenko
 */
public interface HttpRequestBuilder extends HttpHeaderBuilder {

	static HttpRequestBuilder create() {
		return new HttpMessageBuilder();
	}

	static HttpRequestBuilder create(int initCapacity) {
		return new HttpMessageBuilder(initCapacity);
	}

	static ByteBufferArraySupplier supplier(Function<HttpRequestBuilder, ByteBuffer[]> builder) {
		return HttpMessageBuilder.supplier(builder);
	}

	HttpMessageBuilder setRequest(CharSequence uri);

	HttpMessageBuilder setRequest(CharSequence uri, HttpMethod m);

	HttpMessageBuilder setRequest(CharSequence uri, HttpMethod m, HttpVersion version);

	ByteBuffer[] build();

	ByteBuffer[] build(ByteBuffer payload);

	<E extends Throwable> ByteBuffer[] build(CheckedConsumer<OutputStream, E> payloadWriter) throws E;
}
