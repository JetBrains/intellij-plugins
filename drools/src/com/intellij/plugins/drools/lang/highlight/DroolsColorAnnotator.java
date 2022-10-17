// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.highlight;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.plugins.drools.lang.psi.DroolsFunction;
import com.intellij.plugins.drools.lang.psi.DroolsNameId;
import com.intellij.plugins.drools.lang.psi.DroolsReference;
import com.intellij.plugins.drools.lang.psi.DroolsRuleName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;


public class DroolsColorAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement node, @NotNull AnnotationHolder holder) {
    if (node instanceof DroolsReference) {
      if (PsiTreeUtil.getParentOfType(node, DroolsRuleName.class) != null) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(DroolsSyntaxHighlighterColors.RULE).create();
        return;
      }

      final PsiElement resolve = ((DroolsReference)node).resolve();
      if (resolve instanceof PsiField) {
        final PsiModifierList modifierList = ((PsiField)resolve).getModifierList();
        if (modifierList != null && modifierList.hasModifierProperty(PsiModifier.PUBLIC) && modifierList.hasModifierProperty(PsiModifier.STATIC)) {
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(DroolsSyntaxHighlighterColors.PUBLIC_STATIC_FIELD).create();
        }
      }
    } else  if (PsiTreeUtil.getParentOfType(node, DroolsNameId.class) != null) {
      final TextAttributesKey attribute = PsiTreeUtil.getParentOfType(node, DroolsFunction.class) != null
                                          ? DroolsSyntaxHighlighterColors.FUNCTION
                                          : DroolsSyntaxHighlighterColors.LOCAL_VARIABLE;
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(attribute).create();
    }
  }
}