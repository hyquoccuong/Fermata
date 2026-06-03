package com.immrhy.learningandroidapptool.utils.vfs.sftp;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completed;
import static com.immrhy.learningandroidapptool.utils.io.AsyncInputStream.readInputStream;
import static com.immrhy.learningandroidapptool.utils.io.IoUtils.emptyByteBuffer;

import androidx.annotation.NonNull;

import com.jcraft.jsch.SftpATTRS;

import java.io.InputStream;
import java.nio.ByteBuffer;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.async.ObjectPool.PooledObject;
import com.immrhy.learningandroidapptool.utils.io.AsyncInputStream;
import com.immrhy.learningandroidapptool.utils.io.IoUtils;
import com.immrhy.learningandroidapptool.utils.log.Log;
import com.immrhy.learningandroidapptool.utils.net.ByteBufferSupplier;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFile;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFolder;
import com.immrhy.learningandroidapptool.utils.vfs.sftp.SftpRoot.SftpSession;

/**
 * @author Andrey Pavlenko
 */
class SftpFile extends SftpResource implements VirtualFile {
	private FutureSupplier<Long> length;

	SftpFile(SftpRoot root, String path) {
		super(root, path);
	}

	SftpFile(@NonNull SftpRoot root, @NonNull String path, VirtualFolder parent) {
		super(root, path, parent);
	}

	@Override
	public FutureSupplier<Long> getLength() {
		if (length != null) return length;
		return length = lstat().map(SftpATTRS::getSize).onSuccess(len -> length = completed(len));
	}

	@Override
	public AsyncInputStream getInputStream(long offset) {
		return new AsyncInputStream() {
			private final FutureSupplier<PooledObject<SftpSession>> getSession = getRoot().getSession();
			long pos = offset;
			SftpSession session;
			InputStream stream;

			@Override
			public FutureSupplier<ByteBuffer> read(ByteBufferSupplier dst) {
				SftpSession s = session;
				InputStream in = stream;

				if ((s != null) && (in != null)) {
					ByteBuffer b = dst.getByteBuffer();
					FutureSupplier<ByteBuffer> r = readInputStream(in, b, b.remaining());
					if (!r.isFailed()) pos += r.getOrThrow().remaining();
					s.restartTimer();
					return r;
				}

				return getSession.then(o -> {
					SftpSession session = this.session = o.get();
					if (session == null) return completed(emptyByteBuffer());

					InputStream is = stream = session.getChannel().get(getPath(), null, pos);
					ByteBuffer b = dst.getByteBuffer();
					FutureSupplier<ByteBuffer> r = readInputStream(is, b, b.remaining());
					if (!r.isFailed()) pos += r.getOrThrow().remaining();
					session.restartTimer();
					return r;
				});
			}

			@Override
			public void close() {
				IoUtils.close(stream);
				PooledObject<SftpSession> o = getSession.peek();
				if (o != null) o.release();
				else getSession.cancel();
			}
		};
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@NonNull
	@Override
	public FutureSupplier<Boolean> delete() {
		return getRoot().useChannel(ch -> {
			try {
				ch.rm(getPath());
				return true;
			} catch (Exception ex) {
				Log.e(ex, "Failed to delete file ", getPath());
				return false;
			}
		});
	}
}
