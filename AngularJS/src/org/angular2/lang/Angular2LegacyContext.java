// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.webSymbols.framework.WebFrameworkContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class Angular2LegacyContext implements WebFrameworkContext {

  @NotNull
  @Override
  public CachedValueProvider.Result<@Nullable Integer> isEnabled(@NotNull PsiDirectory directory) {
    Set<Object> dependencies = new HashSet<>();
    for (Angular2ContextProvider provider: Angular2ContextProvider.ANGULAR_CONTEXT_PROVIDER_EP.getExtensionList()) {
      var result = provider.isAngular2Context(directory);
      if (result.getValue() == Boolean.TRUE) {
        return new CachedValueProvider.Result<>(0, result.getDependencyItems());
      }
      dependencies.addAll(Arrays.asList(result.getDependencyItems()));
    }
    if (dependencies.isEmpty()) {
      dependencies.add(ModificationTracker.NEVER_CHANGED);
    }
    return new CachedValueProvider.Result<>(null, dependencies.toArray());
  }
}
