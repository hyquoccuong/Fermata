package com.immrhy.learningandroidapptool.utils.vfs.local;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completed;
import static com.immrhy.learningandroidapptool.utils.async.Completed.failed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.immrhy.learningandroidapptool.utils.async.Completed;
import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.io.AsyncInputStream;
import com.immrhy.learningandroidapptool.utils.io.AsyncOutputStream;
import com.immrhy.learningandroidapptool.utils.io.FileUtils;
import com.immrhy.learningandroidapptool.utils.io.IoUtils;
import com.immrhy.learningandroidapptool.utils.io.RandomAccessChannel;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFile;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFolder;

/**
 * @author Andrey Pavlenko
 */
class LocalFile extends LocalResource implements VirtualFile {

	LocalFile(File file) {
		super(file);
	}

	LocalFile(File file, VirtualFolder parent) {
		super(file, parent);
	}

	@Override
	public FutureSupplier<Long> getLength() {
		return getVirtualFileSystem().getLength(getLocalFile());
	}

	@Override
	public boolean isLocalFile() {
		return true;
	}

	@NonNull
	@Override
	public FutureSupplier<Void> copyTo(VirtualFile to) {
		File toFile = to.getLocalFile();

		if (toFile != null) {
			try {
				FileUtils.copy(file, toFile);
				return Completed.completedNull();
			} catch (IOException ex) {
				return failed(ex);
			}
		}

		return VirtualFile.super.copyTo(to);
	}

	@NonNull
	@Override
	public FutureSupplier<Boolean> moveTo(VirtualFile to) {
		File toFile = to.getLocalFile();
		LocalFileSystem fs = getVirtualFileSystem();
		fs.closeCachedChannels(file);

		if (toFile != null) {
			try {
				fs.closeCachedChannels(toFile);
				FileUtils.move(file, toFile);
				return completed(true);
			} catch (IOException ex) {
				return failed(ex);
			}
		}

		return VirtualFile.super.moveTo(to);
	}

	@NonNull
	@Override
	public FutureSupplier<VirtualFile> rename(CharSequence name) {
		try {
			VirtualFolder p = getParent().peek();
			if (p == null) return failed(new IOException());
			File toFile = new File(p.getLocalFile(), name.toString());
			LocalFileSystem fs = getVirtualFileSystem();
			fs.closeCachedChannels(file, toFile);
			FileUtils.move(file, toFile);
			return completed(new LocalFile(toFile, p));
		} catch (IOException ex) {
			return failed(ex);
		}
	}

	@Override
	public AsyncInputStream getInputStream(long offset) throws IOException {
		FileInputStream in = new FileInputStream(file);
		IoUtils.skip(in, offset);
		return AsyncInputStream.from(in, getInputBufferLen());
	}

	@Override
	public AsyncOutputStream getOutputStream() throws IOException {
		return AsyncOutputStream.from(new FileOutputStream(file), getOutputBufferLen());
	}

	@Nullable
	@Override
	public RandomAccessChannel getChannel(String mode) {
		return getVirtualFileSystem().getChannel(getLocalFile(), mode);
	}
}
