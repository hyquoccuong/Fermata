package com.immrhy.learningandroidapptool.utils.vfs.smb;

import androidx.annotation.NonNull;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.vfs.NetResourceBase;
import com.immrhy.learningandroidapptool.utils.vfs.VirtualFolder;

/**
 * @author Andrey Pavlenko
 */
class SmbResource extends NetResourceBase<SmbRoot> {
	private String smbPath;

	SmbResource(@NonNull SmbRoot root, @NonNull String path) {
		super(root, path);
	}

	SmbResource(@NonNull SmbRoot root, @NonNull String path, VirtualFolder parent) {
		super(root, path, parent);
	}

	@NonNull
	@Override
	public FutureSupplier<Boolean> exists() {
		return getRoot().useShare(s ->
				isFile() ? s.fileExists(smbPath()) : s.folderExists(smbPath())
		);
	}

	@Override
	protected FutureSupplier<Long> loadLastModified() {
		return getRoot().useShare(s -> {
			FileAllInformation info = s.getFileInformation(smbPath());
			return info.getBasicInformation().getChangeTime().toEpochMillis();
		});
	}

	String smbPath() {
		if (smbPath == null) {
			smbPath = SmbFileSystem.smbPath(getPath(), (getRoot() != this));
		}
		return smbPath;
	}
}
