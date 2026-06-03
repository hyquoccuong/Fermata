package com.immrhy.learningandroidapptool.utils.net.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.function.BiFunction;
import com.immrhy.learningandroidapptool.utils.io.AsyncInputStream;
import com.immrhy.learningandroidapptool.utils.io.AsyncOutputStream;
import com.immrhy.learningandroidapptool.utils.io.AsyncPipe;
import com.immrhy.learningandroidapptool.utils.io.IoUtils;
import com.immrhy.learningandroidapptool.utils.io.MemOutputStream;
import com.immrhy.learningandroidapptool.utils.net.NetChannel;
import com.immrhy.learningandroidapptool.utils.text.TextUtils;

import static com.immrhy.learningandroidapptool.utils.async.Completed.failed;

/**
 * @author Andrey Pavlenko
 */
public interface HttpMessage {
	int MAX_PAYLOAD_LEN = MemOutputStream.MAX_SIZE;

	@NonNull
	NetChannel getChannel();

	@NonNull
	HttpVersion getVersion();

	@NonNull
	CharSequence getHeaders();

	@Nullable
	CharSequence getContentType();

	@Nullable
	CharSequence getContentEncoding();

	@Nullable
	CharSequence getTransferEncoding();

	long getContentLength();

	boolean isConnectionClose();

	@Nullable
	default CharSequence getCharset() {
		CharSequence ct = getContentType();
		if (ct == null) return null;
		int idx = TextUtils.indexOf(ct, "charset=");
		return (idx == -1) ? null : TextUtils.trim(ct.subSequence(idx + 8, ct.length()));
	}

	default <T> FutureSupplier<T> getPayload(BiFunction<ByteBuffer, Throwable, FutureSupplier<T>> consumer) {
		return getPayload(consumer, true);
	}

	default <T> FutureSupplier<T> getPayload(BiFunction<ByteBuffer, Throwable, FutureSupplier<T>> consumer, boolean decode) {
		return getPayload(consumer, decode, MAX_PAYLOAD_LEN);
	}

	<T> FutureSupplier<T> getPayload(BiFunction<ByteBuffer, Throwable, FutureSupplier<T>> consumer, boolean decode, int maxLen);

	default FutureSupplier<?> writePayload(File dest) {
		try {
			OutputStream out = new FileOutputStream(dest);
			return writePayload(out).thenRun(() -> IoUtils.close(out));
		} catch (FileNotFoundException ex) {
			return failed(ex);
		}
	}

	default FutureSupplier<?> writePayload(OutputStream out) {
		return writePayload(AsyncOutputStream.from(out));
	}

	FutureSupplier<?> writePayload(AsyncOutputStream out);

	default AsyncInputStream readPayload() {
		AsyncPipe pipe = new AsyncPipe(true);
		writePayload(pipe).onFailure(pipe::close);
		return pipe;
	}

	FutureSupplier<?> skipPayload();
}
