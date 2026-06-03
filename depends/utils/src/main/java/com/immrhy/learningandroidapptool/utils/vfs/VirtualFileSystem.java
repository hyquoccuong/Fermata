package com.immrhy.learningandroidapptool.utils.vfs;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Set;

import com.immrhy.learningandroidapptool.utils.async.FutureSupplier;
import com.immrhy.learningandroidapptool.utils.pref.PreferenceStore;
import com.immrhy.learningandroidapptool.utils.resource.Rid;

import static com.immrhy.learningandroidapptool.utils.async.Completed.completedEmptyList;

/**
 * @author Andrey Pavlenko
 */
public interface VirtualFileSystem {

	@NonNull
	Provider getProvider();

	@NonNull
	FutureSupplier<? extends VirtualResource> getResource(Rid rid);

	default FutureSupplier<? extends VirtualFile> getFile(Rid rid) {
		return getResource(rid).map(r -> (r instanceof VirtualFile) ? (VirtualFile) r : null);
	}

	default FutureSupplier<? extends VirtualFolder> getFolder(Rid rid) {
		return getResource(rid).map(r -> (r instanceof VirtualFile) ? (VirtualFolder) r : null);
	}

	@NonNull
	default FutureSupplier<List<VirtualFolder>> getRoots() {
		return completedEmptyList();
	}

	default boolean isSupportedResource(Rid rid) {
		return getProvider().getSupportedSchemes().contains(rid.getScheme());
	}

	interface Provider {

		@NonNull
		Set<String> getSupportedSchemes();

		@NonNull
		FutureSupplier<VirtualFileSystem> createFileSystem(PreferenceStore ps);
	}
}
