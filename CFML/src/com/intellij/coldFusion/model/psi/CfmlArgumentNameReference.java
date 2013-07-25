/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.psi;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.coldFusion.UI.CfmlLookUpItemUtil;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.impl.CfmlNamedAttributeImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

public class CfmlArgumentNameReference extends CfmlReferenceExpression implements PsiElement {
  public CfmlArgumentNameReference(@NotNull final ASTNode node) {
    super(node);
  }

  public String getFunctionName() {
    CfmlFunctionCallExpression parentOfType = PsiTreeUtil.getParentOfType(this, CfmlFunctionCallExpression.class);
    if (parentOfType != null) {
      return parentOfType.getFunctionName();
    }
    return "";
  }

  // ColdFusion does not allow dublicates in functions' names
  @Nullable
  private CfmlFunction resolveToFunction() {
    CfmlFunctionCallExpression parentOfType = PsiTreeUtil.getParentOfType(this, CfmlFunctionCallExpression.class);
    if (parentOfType != null) {
      CfmlReference referenceExpression = parentOfType.getReferenceExpression();
      PsiElement resolve = referenceExpression.resolve();
      if (resolve instanceof CfmlNamedAttributeImpl) {
        resolve = resolve.getParent();
      }
      if (resolve instanceof CfmlFunction) {
        return ((CfmlFunction)resolve);
      }
    }
    return null;
  }

  @Nullable
  private CfmlParameter[] getFunctionParameters() {
    CfmlFunction cfmlFunction = resolveToFunction();
    if (cfmlFunction != null) {
      return cfmlFunction.getParameters();
    }
    return null;
  }

  @Override
  protected ResolveResult[] resolveInner() {
    PsiElement nextSibling = getNextSibling();
    while (nextSibling instanceof PsiWhiteSpace) {
      nextSibling = nextSibling.getNextSibling();
    }
    if (nextSibling != null && nextSibling.getNode().getElementType() != CfmlTokenTypes.ASSIGN) {
      return super.resolveInner();
    }
    CfmlParameter[] functionParameters = getFunctionParameters();
    if (functionParameters != null) {
      Collection<ResolveResult> result = new LinkedList<ResolveResult>();
      String referenceName = getReferenceName();
      for (CfmlParameter param : functionParameters) {
        if (referenceName.equals(param.getName())) {
          result.add(new PsiElementResolveResult(param));
        }
      }
      return result.toArray(ResolveResult.EMPTY_ARRAY);
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  protected CfmlReferenceExpression parseReference(String newText) {
    return CfmlPsiUtil.createReferenceExpression(newText, getProject());
  }

  @Override
  protected PsiElement getSeparator() {
    return findChildByType(CfscriptTokenTypes.POINT);
  }

  @NotNull
  @Override
  public String getReferenceName() {
    PsiElement referenceNameElement = getReferenceNameElement();
    if (referenceNameElement != null) {
      return referenceNameElement.getText();
    }
    return "";
  }

  @Override
  protected PsiElement getReferenceNameElement() {
    return findChildByType(CfscriptTokenTypes.IDENTIFIER);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    Collection<LookupElement> result = new LinkedList<LookupElement>();
    Object[] superResult = ArrayUtil.EMPTY_OBJECT_ARRAY;

    String functionName = getFunctionName();
    if (CfmlUtil.isPredefinedFunction(functionName, getProject())) {
      CfmlFunctionDescription cfmlFunctionDescription =
        CfmlLangInfo.getInstance(getProject()).getFunctionParameters().get(functionName.toLowerCase());
      for (CfmlFunctionDescription.CfmlParameterDescription param : cfmlFunctionDescription.getParameters()) {
        result.add(TailTypeDecorator.withTail(
          LookupElementBuilder.create(param.getName()).withCaseSensitivity(false),
          TailType.createSimpleTailType('=')));
      }
    }
    else {
      CfmlArgumentList parentArgumentsList = PsiTreeUtil.getParentOfType(this, CfmlArgumentList.class);
      if (parentArgumentsList != null) {
        CfmlExpression[] arguments = parentArgumentsList.getArguments();
        if (arguments.length == 1) {
          result.add(LookupElementBuilder.create("argumentCollection").withCaseSensitivity(false));
        }
      }
    }

    PsiElement nextSibling = getNextSibling();
    while (nextSibling instanceof PsiWhiteSpace) {
      nextSibling = nextSibling.getNextSibling();
    }
    if (nextSibling != null && nextSibling.getNode().getElementType() != CfmlTokenTypes.ASSIGN) {
      superResult = super.getVariants();
    }

    CfmlParameter[] functionParameters = getFunctionParameters();
    if (functionParameters != null) {
      for (CfmlParameter param : functionParameters) {
        result.add(CfmlLookUpItemUtil.namedElementToLookupItem(param, null));
      }
    }

    if (!result.isEmpty() || superResult.length > 0) {
      return ArrayUtil.mergeArrays(superResult, ContainerUtil.map2Array(result, Object.class, new Function<LookupElement, Object>() {
        @Override
        public Object fun(LookupElement lookupElement) {
          return lookupElement;
        }
      }));
    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @NotNull
  @Override
  public String getName() {
    PsiElement referenceNameElement = getReferenceNameElement();
    return referenceNameElement != null ? referenceNameElement.getText() : "";
  }

  public String toString() {
    return "Argument " + getName();
  }
}
