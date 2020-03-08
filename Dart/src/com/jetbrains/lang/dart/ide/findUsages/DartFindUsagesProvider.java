// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartLibraryNameElement;
import org.jetbrains.annotations.NotNull;

public class DartFindUsagesProvider implements FindUsagesProvider {

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PsiNamedElement;
  }

  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  @NotNull
  public String getType(@NotNull final PsiElement element) {
    if (element instanceof DartLibraryNameElement) {
      return DartBundle.message("find.usages.type.library");
    }
    final DartComponentType type = DartComponentType.typeOf(element.getParent());
    if (type == null) return DartBundle.message("find.usages.type.reference");
    if (type == DartComponentType.LOCAL_VARIABLE) return DartBundle.message("find.usages.type.local.variable");
    if (type == DartComponentType.GLOBAL_VARIABLE) return DartBundle.message("find.usages.type.top.level.variable");
    return StringUtil.toLowerCase(type.toString());
  }

  @Override
  @NotNull
  public String getDescriptiveName(@NotNull final PsiElement element) {
    if (element instanceof PsiNamedElement) {
      return StringUtil.notNullize(((PsiNamedElement)element).getName());
    }
    return "";
  }

  @Override
  @NotNull
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    final String name = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null;
    return name != null ? name : element.getText();
  }
}
