package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbIconProvider extends IconProvider {

  @Nullable
  @Override
  public Icon getIcon(@NotNull PsiElement element, @Iconable.IconFlags int flags) {
    if (element instanceof HbPsiFile) {
      return HandlebarsIcons.Handlebars_icon;
    }

    return null;
  }
}
