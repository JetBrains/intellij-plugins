// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.library.JSPredefinedLibraryProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionScriptLibraryProvider extends JSPredefinedLibraryProvider {

  private static final Map<String, Ref<VirtualFile>> ourLibFileCache = new ConcurrentHashMap<>();

  private static @Nullable VirtualFile getPredefinedLibFile(@NotNull String libFileName) {
    Ref<VirtualFile> fileRef = getCachedFileRef(libFileName);
    if (fileRef != null) return fileRef.get();
    VirtualFile file = findFileByURL(libFileName);
    return JSLibraryUtil.cacheFile(libFileName, file, ourLibFileCache);
  }

  private static VirtualFile findFileByURL(String libFileName) {
    URL libFileUrl = ActionScriptLibraryProvider.class.getResource(libFileName);
    if (libFileUrl == null) {
      Logger.getInstance(ActionScriptLibraryProvider.class).error("Cannot find " + libFileName + ", the installation is possibly broken.");
      return null;
    }
    VirtualFile file = VfsUtil.findFileByURL(libFileUrl);
    if (file == null || !file.isValid()) {
      Logger.getInstance(ActionScriptLibraryProvider.class)
        .warn("Cannot find virtual file " + libFileName + " by url " + libFileUrl.toExternalForm());
      return null;
    }

    return file;
  }

  private static @Nullable Ref<VirtualFile> getCachedFileRef(@NotNull String fileName) {
    Ref<VirtualFile> ref = ourLibFileCache.get(fileName);
    VirtualFile file = ref != null ? ref.get() : null;
    if (file != null && !file.isValid()) {
      ourLibFileCache.remove(fileName);
      ref = null;
    }
    return ref;
  }

  public static @NotNull Set<VirtualFile> getActionScriptPredefinedLibraryFiles() {
    Set<VirtualFile> files = new HashSet<>(2);
    ContainerUtil.addIfNotNull(files, getPredefinedLibFile(JavaScriptIndex.ECMASCRIPT_JS2));
    ContainerUtil.addIfNotNull(files, getPredefinedLibFile("E4X.js2"));
    return files;
  }

  @Override
  public @NotNull Set<VirtualFile> getRequiredLibraryFilesToIndex() {
    return getActionScriptPredefinedLibraryFiles();
  }
}
