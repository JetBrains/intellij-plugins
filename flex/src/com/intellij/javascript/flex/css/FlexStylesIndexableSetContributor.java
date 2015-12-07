package com.intellij.javascript.flex.css;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * @author ksafonov
 */
public class FlexStylesIndexableSetContributor extends IndexableSetContributor {
  private static final Logger LOG = Logger.getInstance(FlexStylesIndexableSetContributor.class);

  private static final NotNullLazyValue<Set<VirtualFile>> ourFiles = new AtomicNotNullLazyValue<Set<VirtualFile>>() {
    @NotNull
    @Override
    protected Set<VirtualFile> compute() {
      URL libFileUrl = FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as");
      VirtualFile file = VfsUtil.findFileByURL(libFileUrl);
      if (file != null) {
        return Collections.singleton(file);
      }
      else {
        LOG.error("Cannot find FlexStyles.as file by URL " + libFileUrl);
        return Collections.emptySet();
      }
    }
  };

  @NotNull
  @Override
  public Set<VirtualFile> getAdditionalRootsToIndex() {
    return ourFiles.getValue();
  }

  @NotNull
  public static GlobalSearchScope enlarge(@NotNull final GlobalSearchScope scope) {
    return scope.union(new GlobalSearchScope() {
      @Override
      public boolean contains(@NotNull final VirtualFile file) {
        return ourFiles.getValue().contains(file);
      }

      @Override
      public int compare(@NotNull final VirtualFile file1, @NotNull final VirtualFile file2) {
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