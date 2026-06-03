package com.immrhy.learningandroidapptool.utils.vfs.sftp;

import androidx.annotation.NonNull;

import com.jcraft.jsch.SftpATTRS;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.vfs.NetResourceBase;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFolder;

/**
 * @author Andrey Pavlenko
 */
class SftpResource extends NetResourceBase<SftpRoot> {

	SftpResource(@NonNull SftpRoot root, @NonNull String path) {
		super(root, path);
	}

	SftpResource(@NonNull SftpRoot root, @NonNull String path, VirtualFolder parent) {
		super(root, path, parent);
	}

	@NonNull
	@Override
	public FutureSupplier<Boolean> exists() {
		return lstat().map(stat -> stat.isDir() == isFolder());
	}

	@Override
	protected FutureSupplier<Long> loadLastModified() {
		return lstat().map(s -> s.getATime() * 1000L);
	}

	FutureSupplier<SftpATTRS> lstat() {
		return lstat(getPath());
	}

	FutureSupplier<SftpATTRS> lstat(String path) {
		return getRoot().useChannel(ch -> ch.lstat(path));
	}
}
