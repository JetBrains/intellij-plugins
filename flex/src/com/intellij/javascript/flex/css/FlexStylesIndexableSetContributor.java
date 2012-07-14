package com.intellij.javascript.flex.css;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * User: ksafonov
 */
public class FlexStylesIndexableSetContributor extends IndexableSetContributor {

  private static final Set<VirtualFile> FILES;

  static {
    URL libFileUrl = FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as");
    VirtualFile file = VfsUtil.findFileByURL(libFileUrl);
    FILES = Collections.singleton(file);
  }

  @Override
  public Set<VirtualFile> getAdditionalRootsToIndex() {
    return FILES;
  }

  public static GlobalSearchScope enlarge(final GlobalSearchScope scope) {
    return scope.union(new GlobalSearchScope() {
      @Override
      public boolean contains(final VirtualFile file) {
        return FILES.contains(file);
      }

      @Override
      public int compare(final VirtualFile file1, final VirtualFile file2) {
        return scope.compare(file1, file2);
      }

      @Override
      public boolean isSearchInModuleContent(@NotNull final Module aModule) {
        return scope.isSearchInModuleContent(aModule);
      }

      @Override
      public boolean isSearchInLibraries() {
        return scope.isSearchInLibraries();
      }
    });
  }
}
