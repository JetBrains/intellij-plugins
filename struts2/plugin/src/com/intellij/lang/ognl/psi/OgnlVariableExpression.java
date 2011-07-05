/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.psi;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

/**
 * {@code "#varName"}.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlVariableExpression extends OgnlExpressionBase {

  public OgnlVariableExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new PresentationData(getIdentifier().getText(),
                                null,
                                PlatformIcons.VARIABLE_ICON,
                                PlatformIcons.VARIABLE_ICON,
                                null);
  }

  private PsiElement getIdentifier() {
    return findNotNullChildByType(OgnlTokenTypes.IDENTIFIER);
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    return new PsiReference[]{getReference()};
  }

  @Override
  public PsiReference getReference() {
    return new PsiReferenceBase<OgnlExpressionBase>(this, TextRange.from(1, getTextLength() - 1)) {
      @Override
      public PsiElement resolve() {
        return myElement;
      }

      @NotNull
      @Override
      public Object[] getVariants() {
        return new LookupElement[]{
            LookupElementBuilder.create("context")
                                .setIcon(PlatformIcons.VARIABLE_ICON)
                                .setTypeText("Map<String,Object>"),
            LookupElementBuilder.create("root")
                                .setIcon(PlatformIcons.VARIABLE_ICON)
                                .setTypeText("Root Object"),
            LookupElementBuilder.create("this")
                                .setIcon(PlatformIcons.VARIABLE_ICON)
                                .setTypeText("<Current Object>")
        };
      }

    };
  }

  @Override
  public PsiType getType() {
    return null;
  }

}