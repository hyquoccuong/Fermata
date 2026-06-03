package com.immrhy.learningandroidapptool.utils.vfs;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completed;
import static com.immrhy.learningandroidapptool.utils.async.Completed.completedVoid;
import static com.immrhy.learningandroidapptool.utils.async.Completed.failed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.immrhy.learningandroidapptool.utils.async.Async;
import com.immrhy.learningandroidapptool.utils.async.Completed;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.holder.BooleanHolder;
import com.immrhy.learningandroidapptool.utils.holder.Holder;
import com.immrhy.learningandroidapptool.utils.holder.LongHolder;
import com.immrhy.learningandroidapptool.utils.io.AsyncInputStream;
import com.immrhy.learningandroidapptool.utils.io.AsyncOutputStream;
import com.immrhy.learningandroidapptool.utils.io.IoUtils;
import com.immrhy.learningandroidapptool.utils.io.RandomAccessChannel;
import com.immrhy.learningandroidapptool.utils.net.ByteBufferArraySupplier;
import com.immrhy.learningandroidapptool.utils.net.ByteBufferSupplier;
import com.immrhy.learningandroidapptool.utils.net.NetChannel;

/**
 * @author Andrey Pavlenko
 */
public interface VirtualFile extends VirtualResource {
	int DEFAULT_BUFFER_LEN = 8192;

	default boolean isFile() {
		return true;
	}

	@Override
	default boolean isFolder() {
		return false;
	}

	default FutureSupplier<Long> getLength() {
		return completed(0L);
	}

	default FutureSupplier<? extends Info> getInfo() {
		File f = getLocalFile();
		return (f != null) ? completed(Info.localFileInfo(f)) : getLength().map(l -> () -> l);
	}

	@NonNull
	default FutureSupplier<Void> copyTo(VirtualFile to) {
		AsyncInputStream in = null;
		AsyncOutputStream out = null;

		try {
			AsyncInputStream is = in = getInputStream();
			AsyncOutputStream os = out = to.getOutputStream();
			BooleanHolder completed = new BooleanHolder();
			ByteBuffer buf = ByteBuffer.allocate(Math.min(getInputBufferLen(), to.getOutputBufferLen()));

			FutureSupplier<Void> rw = is.read(() -> buf).then(read -> {
				if (read.hasRemaining()) {
					buf.flip();
					return os.write(buf);
				} else {
					completed.set(true);
					return Completed.completedVoid();
				}
			});

			FutureSupplier<Void> copy = rw.thenIterate(r -> completed.get() ? null : rw);
			copy.onCompletion((r, f) -> IoUtils.close(is, os));
			return copy;
		} catch (Throwable ex) {
			IoUtils.close(in, out);
			return failed(ex);
		}
	}

	@NonNull
	default FutureSupplier<Boolean> moveTo(VirtualFile to) {
		return copyTo(to).then(v -> delete());
	}

	@NonNull
	default FutureSupplier<VirtualFile> rename(CharSequence name) {
		return getParent().then(p -> p.createFile(name).then(f -> moveTo(f).map(b -> f)));
	}

	default FutureSupplier<Void> transferTo(NetChannel channel, long off, long len,
																					@Nullable ByteBufferSupplier header) {
		return transferTo(channel, off, len, (header != null) ? header.asArray() : null);
	}

	default FutureSupplier<Void> transferTo(NetChannel channel, long off, long len,
																					@Nullable ByteBufferArraySupplier header) {
		RandomAccessChannel rac = getChannel();
		if (rac != null) return channel.send(rac, off, len, header);

		AsyncInputStream vis = null;

		try {
			AsyncInputStream in = vis = getInputStream(off);
			LongHolder remain = new LongHolder((len < 0) ? Long.MAX_VALUE : len);
			Holder<ByteBufferArraySupplier> hdr = (header != null) ? new Holder<>(header) : null;

			return Async.iterate(() -> {
				if (remain.value <= 0) return null;

				return in.read(() -> allocateInputBuffer(remain.value)).then(buf -> {
					int read = buf.remaining();

					if (read > 0) {
						remain.value -= read;
						ByteBufferArraySupplier bbs;

						if ((hdr == null) || (hdr.value == null)) {
							bbs = () -> new ByteBuffer[]{buf};
						} else {
							bbs = ByteBufferArraySupplier.wrap(hdr.value, () -> new ByteBuffer[]{buf});
							hdr.value = null;
						}

						return channel.write(bbs);
					} else {
						remain.value = 0;
						return completedVoid();
					}
				});
			}).thenRun(vis::close);
		} catch (Throwable ex) {
			IoUtils.close(vis);
			return failed(ex);
		}
	}

	default AsyncInputStream getInputStream() throws IOException {
		return getInputStream(0);
	}

	default AsyncInputStream getInputStream(long offset) throws IOException {
		throw new IOException();
	}

	default AsyncOutputStream getOutputStream() throws IOException {
		throw new IOException();
	}

	@Nullable
	default RandomAccessChannel getChannel() {
		return getChannel("r");
	}

	@Nullable
	default RandomAccessChannel getChannel(String mode) {
		return null;
	}

	default int getInputBufferLen() {
		return DEFAULT_BUFFER_LEN;
	}

	default int getOutputBufferLen() {
		return DEFAULT_BUFFER_LEN;
	}

	default ByteBuffer allocateInputBuffer(long max) {
		return ByteBuffer.allocate((int) Math.min(getInputBufferLen(), max));
	}

	default ByteBuffer allocateOutputBuffer(long max) {
		return ByteBuffer.allocate((int) Math.min(getOutputBufferLen(), max));
	}

	interface Info {
		static Info localFileInfo(File f) {
			return new Info() {
				@Override
				public long getLength() {
					return f.length();
				}

				@NonNull
				@Override
				public File getLocalFile() {
					return f;
				}
			};
		}

		long getLength();

		@Nullable
		default File getLocalFile() {
			return null;
		}

		@Nullable
		default String getContentEncoding() {
			return null;
		}

		@Nullable
		default String getCharacterEncoding() {
			return null;
		}
	}
}
