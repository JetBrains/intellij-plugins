// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
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

  private CfmlParameter @Nullable [] getFunctionParameters() {
    CfmlFunction cfmlFunction = resolveToFunction();
    if (cfmlFunction != null) {
      return cfmlFunction.getParameters();
    }
    return null;
  }

  @Override
  protected ResolveResult @NotNull [] resolveInner() {
    PsiElement nextSibling = getNextSibling();
    while (nextSibling instanceof PsiWhiteSpace) {
      nextSibling = nextSibling.getNextSibling();
    }
    if (nextSibling != null && nextSibling.getNode().getElementType() != CfmlTokenTypes.ASSIGN) {
      return super.resolveInner();
    }
    CfmlParameter[] functionParameters = getFunctionParameters();
    if (functionParameters != null) {
      Collection<ResolveResult> result = new LinkedList<>();
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
    return super.parseReference(newText);
  }

  @Override
  protected PsiElement getSeparator() {
    return super.getSeparator();
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

  @Override
  public Object @NotNull [] getVariants() {
    Collection<LookupElement> result = new LinkedList<>();
    Object[] superResult = ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    String functionName = getFunctionName();
    if (CfmlUtil.isPredefinedFunction(functionName, getProject())) {
      CfmlFunctionDescription cfmlFunctionDescription =
        CfmlLangInfo.getInstance(getProject()).getFunctionParameters().get(StringUtil.toLowerCase(functionName));
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
      return ArrayUtil.mergeArrays(superResult, ContainerUtil.map2Array(result, Object.class,
                                                                        lookupElement -> lookupElement));
    }
    return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
  }

  @NotNull
  @Override
  public String getName() {
    PsiElement referenceNameElement = getReferenceNameElement();
    return referenceNameElement != null ? referenceNameElement.getText() : "";
  }

  @Override
  public String toString() {
    return "Argument " + getName();
  }
}
