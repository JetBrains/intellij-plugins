// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.config;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.prettierjs.PrettierUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PrettierConfigIconProvider extends IconProvider implements DumbAware {
  @Override
  public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
    return PrettierUtil.isConfigFile(element)
           ? PrettierUtil.ICON : null;
  }
}
