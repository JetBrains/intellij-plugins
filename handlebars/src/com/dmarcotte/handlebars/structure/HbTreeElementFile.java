// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.structure;

import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

class HbTreeElementFile extends PsiTreeElementBase<HbPsiFile> {

  private final HbPsiFile myFile;

  HbTreeElementFile(@NotNull HbPsiFile psiFile) {
    super(psiFile);
    this.myFile = psiFile;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    return HbTreeElement.getStructureViewTreeElements(myFile);
  }

  @Override
  public @Nullable String getPresentableText() {
    return myFile.getName();
  }
}
