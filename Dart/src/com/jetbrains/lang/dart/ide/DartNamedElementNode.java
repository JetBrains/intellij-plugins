// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.PsiElementMemberChooserObject;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class DartNamedElementNode extends PsiElementMemberChooserObject implements ClassMember {
  public DartNamedElementNode(final DartComponent dartComponent) {
    super(dartComponent, buildPresentationText(dartComponent), dartComponent.getIcon(Iconable.ICON_FLAG_VISIBILITY));
  }

  private static @Nullable @Nls String buildPresentationText(DartComponent dartComponent) {
    final ItemPresentation presentation = dartComponent.getPresentation();
    if (presentation == null) {
      return dartComponent.getName();
    }
    if (dartComponent instanceof DartClass) {
      final String location = presentation.getLocationString();
      if (location != null && !location.isEmpty()) {
        return dartComponent.getName() + " " + location;
      }
      return dartComponent.getName();
    }
    else {
      return presentation.getPresentableText();
    }
  }

  @Nullable
  @Override
  public MemberChooserObject getParentNodeDelegate() {
    final DartComponent result = PsiTreeUtil.getParentOfType(getPsiElement(), DartComponent.class);
    return result == null ? null : new DartNamedElementNode(result);
  }
}
