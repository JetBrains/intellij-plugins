/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.lang.ognl.psi.impl;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Yann C&eacute;bron
 */
abstract class OgnlVariableExpressionBase extends OgnlExpressionImpl {

  protected OgnlVariableExpressionBase(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return getText().substring(1);
      }

      @Nullable
      @Override
      public String getLocationString() {
        return null;
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return null;
      }
    };
  }


  // TODO add some fake completion variants
  @Nullable
  @Override
  public PsiReference getReference() {
    return new PsiReferenceBase<PsiElement>(this, TextRange.from(1, getTextLength() - 1), true) {
      @Nullable
      @Override
      public PsiElement resolve() {
        return OgnlVariableExpressionBase.this;
      }


      @NotNull
      @Override
      public Object[] getVariants() {
        return new LookupElement[]{
          LookupElementBuilder.create("context").withIcon(PlatformIcons.VARIABLE_ICON).withTypeText("Map<String,Object>"),
          LookupElementBuilder.create("root").withIcon(PlatformIcons.VARIABLE_ICON).withTypeText("Root Object"),
          LookupElementBuilder.create("this").withIcon(PlatformIcons.VARIABLE_ICON).withTypeText("<Current Object>")
        };
      }
    };
  }
}
