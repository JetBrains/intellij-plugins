// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.structure;

import com.dmarcotte.handlebars.psi.HbPsiElement;
import com.dmarcotte.handlebars.psi.HbStatements;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class HbTreeElement extends PsiTreeElementBase<HbPsiElement> {

  private final HbPsiElement myElement;

  private HbTreeElement(HbPsiElement psiElement) {
    super(psiElement);
    myElement = psiElement;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    return getStructureViewTreeElements(myElement);
  }

  static List<StructureViewTreeElement> getStructureViewTreeElements(PsiElement psiElement) {
    List<StructureViewTreeElement> children = new ArrayList<>();
    for (PsiElement childElement : psiElement.getChildren()) {
      if (!(childElement instanceof HbPsiElement)) {
        continue;
      }

      if (childElement instanceof HbStatements) {
        // HbStatments elements transparently wrap other elements, so we don't add
        // this element to the tree, but we add its children
        children.addAll(new HbTreeElement((HbPsiElement)childElement).getChildrenBase());
      }

      for (Class suitableClass : HbStructureViewModel.ourSuitableClasses) {
        if (ReflectionUtil.isAssignable(suitableClass, childElement.getClass())) {
          children.add(new HbTreeElement((HbPsiElement)childElement));
          break;
        }
      }
    }
    return children;
  }

  @Override
  public @Nullable String getPresentableText() {
    return myElement.getName();
  }

  @Override
  public Icon getIcon(boolean open) {
    return myElement.getIcon(0);
  }
}
