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

public class ActionScriptLibraryProvider extends JSPredefinedLibraryProvider {
  private static final Logger LOG = Logger.getInstance(ActionScriptLibraryProvider.class);
  private static final Map<String, Ref<VirtualFile>> ourLibFileCache = ContainerUtil.newConcurrentMap();
  private static final String[] ourActionScriptLibraries = new String[]{JavaScriptIndex.ECMASCRIPT_JS2, "E4X.js2"};

  @Nullable
  private static VirtualFile getPredefinedLibFile(@NotNull String libFileName) {
    Ref<VirtualFile> fileRef = getCachedFileRef(libFileName);
    if (fileRef != null) return fileRef.get();
    VirtualFile file = findFileByURL(libFileName);
    return JSLibraryUtil.cacheFile(libFileName, file, ourLibFileCache);
  }

  private static VirtualFile findFileByURL(String libFileName) {
    URL libFileUrl = ActionScriptLibraryProvider.class.getResource(libFileName);
    if (libFileUrl == null) {
      LOG.error("Cannot find " + libFileName + ", the installation is possibly broken.");
      return null;
    }
    VirtualFile file = VfsUtil.findFileByURL(libFileUrl);
    if (file != null && file.isValid()) {
      return file;
    }
    LOG.warn("Cannot find virtual file " + libFileName + " by url " + libFileUrl.toExternalForm());
    return null;
  }

  @Nullable
  private static Ref<VirtualFile> getCachedFileRef(@NotNull String fileName) {
    Ref<VirtualFile> ref = ourLibFileCache.get(fileName);
    VirtualFile file = ref != null ? ref.get() : null;
    if (file != null && !file.isValid()) {
      ourLibFileCache.remove(fileName);
      ref = null;
    }
    return ref;
  }

  @NotNull
  public static Set<VirtualFile> getActionScriptPredefinedLibraryFiles() {
    Set<VirtualFile> files = ContainerUtil.newHashSet(ourActionScriptLibraries.length);
    for (String fileName : ourActionScriptLibraries) {
      ContainerUtil.addIfNotNull(files, getPredefinedLibFile(fileName));
    }
    return files;
  }

  @NotNull
  @Override
  public Set<VirtualFile> getRequiredLibraryFilesToIndex() {
    Set<VirtualFile> libFiles = new HashSet<>();
    libFiles.addAll(getActionScriptPredefinedLibraryFiles());
    return libFiles;
  }
}
