// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.css;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

public class FlexStylesIndexableSetContributor extends IndexableSetContributor {
  private static final Logger LOG = Logger.getInstance(FlexStylesIndexableSetContributor.class);

  private static final NotNullLazyValue<Set<VirtualFile>> ourFiles = NotNullLazyValue.atomicLazy(() -> {
    URL libFileUrl = FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as");
    VirtualFile file = VfsUtil.findFileByURL(libFileUrl);
    if (file != null) {
      return Collections.singleton(file);
    }
    else {
      LOG.error("Cannot find FlexStyles.as file by URL " + libFileUrl);
      return Collections.emptySet();
    }
  });

  @NotNull
  @Override
  public Set<VirtualFile> getAdditionalRootsToIndex() {
    return ourFiles.getValue();
  }

  @NotNull
  public static GlobalSearchScope enlarge(@NotNull final GlobalSearchScope scope) {
    return scope.union(new DelegatingGlobalSearchScope(scope) {
      @Override
      public boolean contains(@NotNull final VirtualFile file) {
        return ourFiles.getValue().contains(file);
      }
    });
  }
}