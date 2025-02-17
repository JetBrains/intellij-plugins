// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.lang.Language;
import com.intellij.psi.css.EmbeddedCssProvider;
import org.jetbrains.annotations.NotNull;

public final class JadeEmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return JadeLanguage.INSTANCE.equals(language);
  }

}
