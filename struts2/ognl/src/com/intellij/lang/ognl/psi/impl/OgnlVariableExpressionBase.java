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

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.psi.resolve.OgnlResolveUtil;
import com.intellij.lang.ognl.psi.resolve.variable.OgnlVariableReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
abstract class OgnlVariableExpressionBase extends OgnlExpressionImpl {

  protected OgnlVariableExpressionBase(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    return new OgnlVariableReferencePsiReference(this);
  }

  private static final class OgnlVariableReferencePsiReference extends PsiReferenceBase.Poly<PsiElement> {

    private static final Function<OgnlVariableReference, PsiElementResolveResult> RESOLVE_FUNCTION =
      reference -> new PsiElementResolveResult(reference.getNavigationElement());
    private static final Function<OgnlVariableReference, Object> VARIANT_FUNCTION =
      element -> LookupElementBuilder.create(element.getNavigationElement(), element.getName())
        .withIcon(element.getIcon(0))
        .withTailText(" (" + element.getOriginInfo() + ")", true)
        .withTypeText(element.getType().getPresentableText());

    private OgnlVariableReferencePsiReference(OgnlVariableExpressionBase element) {
      super(element, TextRange.from(1, element.getTextLength() - 1), true);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
      final String name = getValue();

      final CommonProcessors.CollectProcessor<OgnlVariableReference> processor =
        new CommonProcessors.CollectProcessor<>() {
          @Override
          protected boolean accept(OgnlVariableReference reference) {
            return reference.getName().equals(name);
          }
        };
      OgnlResolveUtil.processVariables(getElement(), processor);
      return ContainerUtil.map2Array(processor.getResults(), PsiElementResolveResult.class, RESOLVE_FUNCTION);
    }

    @Override
    public Object @NotNull [] getVariants() {
      final CommonProcessors.CollectProcessor<OgnlVariableReference> processor =
        new CommonProcessors.CollectProcessor<>();
      OgnlResolveUtil.processVariables(getElement(), processor);

      return ContainerUtil.map2Array(processor.getResults(), VARIANT_FUNCTION);
    }
  }
}
