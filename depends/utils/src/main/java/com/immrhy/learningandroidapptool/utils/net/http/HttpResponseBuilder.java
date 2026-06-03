package com.immrhy.learningandroidapptool.utils.net.http;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.immrhy.learningandroidapptool.utils.function.CheckedConsumer;
import com.immrhy.learningandroidapptool.utils.function.Function;
import com.immrhy.learningandroidapptool.utils.net.ByteBufferArraySupplier;

/**
 * @author Andrey Pavlenko
 */
public interface HttpResponseBuilder extends HttpHeaderBuilder {

	static HttpResponseBuilder create() {
		return new HttpMessageBuilder();
	}

	static HttpResponseBuilder create(int initCapacity) {
		return new HttpMessageBuilder(initCapacity);
	}

	static ByteBufferArraySupplier supplier(Function<HttpResponseBuilder, ByteBuffer[]> builder) {
		return HttpMessageBuilder.supplier(builder);
	}

	 HttpMessageBuilder setStatusOk(HttpVersion version);

	 HttpMessageBuilder setStatusPartial(HttpVersion version);

	 HttpMessageBuilder setStatus(HttpVersion version, CharSequence status);

	ByteBuffer[] build();

	ByteBuffer[] build(ByteBuffer payload);

	<E extends Throwable> ByteBuffer[] build(CheckedConsumer<OutputStream, E> payloadWriter) throws E;
}
