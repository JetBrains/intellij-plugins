// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.CachedValueProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class BladeVueContextProvider implements VueContextProvider {

  @NotNull
  @Override
  public CachedValueProvider.Result<Boolean> isVueContext(@NotNull PsiDirectory directory) {
    return CachedValueProvider.Result.create(false, ModificationTracker.NEVER_CHANGED);
  }

  @Override
  public boolean isVueContextForbidden(@NotNull VirtualFile context, @NotNull Project project) {
    return context.getName().toLowerCase(Locale.ENGLISH).endsWith(".blade.php");
  }
}
