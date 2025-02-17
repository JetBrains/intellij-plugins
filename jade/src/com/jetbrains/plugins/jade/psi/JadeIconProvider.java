package com.jetbrains.plugins.jade.psi;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.jetbrains.plugins.jade.JadeIcons;
import com.jetbrains.plugins.jade.JadeToPugTransitionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class JadeIconProvider extends IconProvider {
  @Override
  public @Nullable Icon getIcon(@NotNull PsiElement element, @Iconable.IconFlags int flags) {
    if (!(element instanceof JadeFileImpl)) {
      return null;
    }

    return JadeToPugTransitionHelper.isPugElement(element) ? JadeIcons.Pug : JadeIcons.Jade;
  }
}
