// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.library.JSBundledLibraryUrls;
import com.intellij.lang.javascript.library.JSPredefinedLibraryProvider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class ActionScriptLibraryProvider extends JSPredefinedLibraryProvider {

  private static final List<String> NAMES = List.of(JavaScriptIndex.ECMASCRIPT_JS2, "E4X.js2");

  private static final JSBundledLibraryUrls URLS = new JSBundledLibraryUrls("ActionScript", NAMES, (relativePath) -> {
    return ActionScriptLibraryProvider.class.getResource(relativePath);
  });

  public static @NotNull Set<VirtualFile> getActionScriptPredefinedLibraryFiles() {
    return URLS.getFiles();
  }

  @Override
  public @NotNull Set<VirtualFile> getRequiredLibraryFilesToIndex() {
    return getActionScriptPredefinedLibraryFiles();
  }
}
