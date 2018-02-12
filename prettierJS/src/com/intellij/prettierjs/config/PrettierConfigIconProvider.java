package com.intellij.prettierjs.config;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.prettierjs.PrettierUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PrettierConfigIconProvider extends IconProvider implements DumbAware {
  @Nullable
  @Override
  public Icon getIcon(@NotNull PsiElement element, int flags) {
    return PrettierUtil.isConfigFile(element)
           ? PrettierUtil.ICON : null;
  }
}
