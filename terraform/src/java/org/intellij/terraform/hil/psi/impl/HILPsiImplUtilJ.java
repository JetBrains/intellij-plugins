// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.terraform.hcl.psi.HCLPsiUtil;
import org.intellij.terraform.hil.HILElementTypes;
import org.intellij.terraform.hil.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class HILPsiImplUtilJ {

  public static ILExpression getQualifier(ILMethodCallExpression expression) {
    return HILPsiImplUtils.INSTANCE.getQualifier(expression);
  }

  @Nullable
  public static ILVariable getMethod(ILMethodCallExpression expression) {
    return HILPsiImplUtils.INSTANCE.getMethod(expression);
  }

  @NotNull
  public static String getUnquotedText(ILLiteralExpression literal){
    return HILPsiImplUtils.INSTANCE.getUnquotedText(literal);
  }

  @NotNull
  public static IElementType getOperationSign(ILUnaryExpression expression) {
    return HILPsiImplUtils.INSTANCE.getOperationSign(expression);
  }

  @NotNull
  public static IElementType getOperationSign(ILBinaryExpression expression) {
    return expression.getNode().getFirstChildNode().getTreeNext().getElementType();
  }

  public static PsiReference @NotNull [] getReferences(@NotNull ILSelectExpression select) {
    return ReferenceProvidersRegistry.getReferencesFromProviders(select);
  }

  @Nullable
  public static PsiReference getReference(@NotNull ILSelectExpression select) {
    PsiReference[] refs = getReferences(select);
    return refs.length != 0 ? refs[0] : null;
  }

  @NotNull
  public static ILVariable getVar1(@NotNull ILTemplateForStatement stmt) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getChildOfType(stmt, ILVariable.class);
  }

  @Nullable
  public static ILVariable getVar2(@NotNull ILTemplateForStatement stmt) {
    final ILVariable var1 = getVar1(stmt);
    final PsiElement next = PsiTreeUtil.skipSiblingsForward(var1, PsiWhiteSpace.class);
    if (next != null && next.getNode().getElementType() == HILElementTypes.COMMA) {
      return PsiTreeUtil.getNextSiblingOfType(next, ILVariable.class);
    }
    return null;
  }

  @NotNull
  public static ILExpression getContainer(@NotNull ILTemplateForStatement stmt) {
    final ILVariable var1 = getVar1(stmt);
    final ILVariable var2 = getVar2(stmt);
    //noinspection ConstantConditions
    return PsiTreeUtil.getNextSiblingOfType(var2 != null ? var2 : var1, ILExpression.class);
  }

  @NotNull
  public static String getName(@NotNull ILProperty property) {
    return StringUtil.unescapeStringCharacters(HCLPsiUtil.stripQuotes(property.getNameElement().getText()));
  }

  @NotNull
  public static ILExpression getNameElement(@NotNull ILProperty property) {
    PsiElement firstChild = property.getFirstChild();
    if (!(firstChild instanceof ILExpression))
      throw new IllegalStateException("Excepted expression, got " + firstChild.getClass().getName());
    return ((ILExpression) firstChild);
  }

  @Nullable
  public static ILExpression getValue(@NotNull ILProperty property) {
    return PsiTreeUtil.getNextSiblingOfType(getNameElement(property), ILExpression.class);
  }

  @NotNull
  public static List<ILExpression> getElements(@NotNull ILObject object) {
    return PsiTreeUtil.getChildrenOfTypeAsList(object, ILExpression.class);
  }
}
