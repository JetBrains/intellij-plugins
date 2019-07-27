// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.CachedValueProvider;
import org.jetbrains.annotations.NotNull;

public interface Angular2ContextProvider {

  ExtensionPointName<Angular2ContextProvider> ANGULAR_CONTEXT_PROVIDER_EP =
    ExtensionPointName.create("org.angular2.contextProvider");

  @NotNull
  CachedValueProvider.Result<Boolean> isAngular2Context(@NotNull PsiDirectory directory);
}
