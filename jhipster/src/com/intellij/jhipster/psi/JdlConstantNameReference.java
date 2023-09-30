// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.jhipster.model.JdlDeclarationsModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveState;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

final class JdlConstantNameReference extends PsiReferenceBase<JdlId> {
  public JdlConstantNameReference(@NotNull JdlId element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    PsiFile containingFile = myElement.getContainingFile();
    if (containingFile == null) return null;

    List<JdlConstant> constantRefs = new ArrayList<>();

    String myId = myElement.getText(); // todo move ID to mixin

    // todo support files in the same directory
    containingFile.processDeclarations((element, state) -> {
      if (element instanceof JdlConstant && Objects.equals(myId, ((JdlConstant)element).getName())) {
        constantRefs.add((JdlConstant)element);
        return false;
      }
      return true;
    }, ResolveState.initial(), null, myElement);

    if (constantRefs.size() != 1) return null;

    return constantRefs.get(0);
  }

  @Override
  public Object @NotNull [] getVariants() {
    PsiFile containingFile = myElement.getContainingFile();
    if (containingFile == null) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    Collection<JdlConstant> constants = JdlDeclarationsModel.findAllJdlConstants(containingFile);
    List<LookupElement> items = new ArrayList<>();

    for (JdlConstant entity : constants) {
      items.add(LookupElementBuilder.create(entity)
                  .withIcon(JdlIconsMapping.getConstantIcon()));
    }

    return items.toArray();
  }
}
