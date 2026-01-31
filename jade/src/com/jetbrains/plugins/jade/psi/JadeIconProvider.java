// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.ide.IconProvider;
import com.intellij.jade.icons.JadeIcons;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.jetbrains.plugins.jade.JadeToPugTransitionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class JadeIconProvider extends IconProvider {
  @Override
  public @Nullable Icon getIcon(@NotNull PsiElement element, @Iconable.IconFlags int flags) {
    if (!(element instanceof JadeFileImpl)) {
      return null;
    }

    return JadeToPugTransitionHelper.isPugElement(element) ? JadeIcons.Pug : JadeIcons.Jade;
  }
}
