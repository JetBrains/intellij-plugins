package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

public class MarkdownCodeFencePluginCacheProvider {
  @NotNull private final VirtualFile myFile;
  @NotNull private final Collection<File> myAliveCachedFiles = ContainerUtil.newHashSet();

  public MarkdownCodeFencePluginCacheProvider(@NotNull VirtualFile file) {
    myFile = file;
  }

  @NotNull
  public Collection<File> getAliveCachedFiles() {
    return myAliveCachedFiles;
  }

  @NotNull
  public VirtualFile getFile() {
    return myFile;
  }

  public void addAliveCachedFile(@NotNull File file) {
    myAliveCachedFiles.add(file);
  }

  //need to override `equals()`/`hasCode()` to scan cache for the latest `cacheProvider` only, see 'MarkdownCodeFencePluginCache.registerCacheProvider()'
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MarkdownCodeFencePluginCacheProvider provider = (MarkdownCodeFencePluginCacheProvider)o;
    return Objects.equals(myFile, provider.myFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myFile);
  }
}