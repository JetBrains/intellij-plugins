package com.jetbrains.actionscript.profiler.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class AllSearchScope extends GlobalSearchScope {
  @Override
  public boolean contains(VirtualFile file) {
    return true;
  }

  @Override
  public int compare(VirtualFile file1, VirtualFile file2) {
    return 0;
  }

  @Override
  public boolean isSearchInModuleContent(@NotNull Module aModule) {
    return true;
  }

  @Override
  public boolean isSearchInLibraries() {
    return true;
  }

  @Override
  public String getDisplayName() {
    return "All";
  }
}
