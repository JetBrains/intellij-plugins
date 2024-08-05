// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

public final class FlexStylesIndexableSetContributor extends IndexableSetContributor {
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

  @Override
  public @NotNull Set<VirtualFile> getAdditionalRootsToIndex() {
    return ourFiles.getValue();
  }

  public static @NotNull GlobalSearchScope enlarge(final @NotNull GlobalSearchScope scope) {
    return scope.union(new DelegatingGlobalSearchScope(scope) {
      @Override
      public boolean contains(final @NotNull VirtualFile file) {
        return ourFiles.getValue().contains(file);
      }
    });
  }
}