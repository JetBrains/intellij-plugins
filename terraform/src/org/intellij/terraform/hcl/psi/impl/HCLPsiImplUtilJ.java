// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.terraform.hcl.HCLElementTypes;
import org.intellij.terraform.hcl.HCLTokenTypes;
import org.intellij.terraform.hcl.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class HCLPsiImplUtilJ {
  public static @NotNull String getName(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getName(property);
  }

  public static @NotNull String getName(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getName(block);
  }

  public static @NotNull String getFullName(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getFullName(block);
  }

  public static @NotNull HCLExpression getNameElement(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getNameElement(property);
  }

  public static HCLElement @NotNull [] getNameElements(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getNameElements(block);
  }

  public static @Nullable HCLExpression getValue(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getValue(property);
  }

  public static @Nullable HCLObject getObject(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getObject(block);
  }

  public static boolean isQuotedString(@NotNull HCLLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.isQuotedString(literal);
  }

  public static @NotNull ItemPresentation getPresentation(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getPresentation(property);
  }

  public static @NotNull ItemPresentation getPresentation(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getPresentation(block);
  }

  public static @NotNull ItemPresentation getPresentation(@NotNull HCLArray array) {
    return HCLPsiImplUtils.INSTANCE.getPresentation(array);
  }

  public static @NotNull ItemPresentation getPresentation(@NotNull HCLObject o) {
    return HCLPsiImplUtils.INSTANCE.getPresentation(o);
  }

  public static @NotNull List<Pair<TextRange, String>> getTextFragments(@NotNull HCLStringLiteral literal) {
    return JavaUtil.getTextFragments(literal);
  }

  public static @Nullable HCLProperty findProperty(@NotNull HCLObject object, @NotNull String name) {
    return HCLPsiImplUtils.INSTANCE.findProperty(object, name);
  }

  public static @NotNull List<HCLBlock> getBlockList(@NotNull HCLObject object) {
    return PsiTreeUtil.getChildrenOfTypeAsList(object, HCLBlock.class);
  }

  public static @NotNull List<HCLExpression> getElements(@NotNull HCLObject object) {
    return PsiTreeUtil.getChildrenOfTypeAsList(object, HCLExpression.class);
  }

  public static @NotNull String getValue(@NotNull HCLStringLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  public static char getQuoteSymbol(@NotNull HCLStringLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getQuoteSymbol(literal);
  }

  public static @NotNull String getValue(@NotNull HCLHeredocLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  public static boolean isIndented(@NotNull HCLHeredocLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.isIndented(literal);
  }

  public static @Nullable Integer getIndentation(@NotNull HCLHeredocLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getIndentation(literal);
  }

  public static @Nullable Integer getMinimalIndentation(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getMinimalIndentation(content);
  }

  public static @NotNull String getValue(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getValue(content, 0);
  }

  public static @NotNull List<String> getLines(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getLines(content);
  }
  public static @NotNull List<CharSequence> getLinesRaw(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getLinesRaw(content);
  }

  public static int getLinesCount(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getLinesCount(content);
  }

  public static @NotNull List<Pair<TextRange, String>> getTextFragments(@NotNull HCLHeredocContent literal) {
    return JavaUtil.getTextFragments(literal);
  }

  public static @NotNull String getName(@NotNull HCLHeredocMarker marker) {
    return HCLPsiImplUtils.INSTANCE.getName(marker);
  }

  public static boolean getValue(@NotNull HCLBooleanLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  public static @NotNull Number getValue(@NotNull HCLNumberLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  public static @NotNull String getId(@NotNull HCLIdentifier identifier) {
    return HCLPsiImplUtils.INSTANCE.getId(identifier);
  }

  public static @Nullable HCLIdentifier getVar1(@NotNull HCLForIntro intro) {
    // May be null in case of incomplete 'for', e.g. '[for :]'
    return PsiTreeUtil.getChildOfType(intro, HCLIdentifier.class);
  }

  public static @Nullable HCLIdentifier getVar2(@NotNull HCLForIntro intro) {
    final HCLIdentifier var1 = getVar1(intro);
    final PsiElement next = PsiTreeUtil.skipSiblingsForward(var1, PsiWhiteSpace.class);
    if (next != null && next.getNode().getElementType() == HCLElementTypes.COMMA) {
      return PsiTreeUtil.getNextSiblingOfType(next, HCLIdentifier.class);
    }
    return null;
  }

  public static @Nullable HCLExpression getContainer(@NotNull HCLForIntro intro) {
    final HCLIdentifier var1 = getVar1(intro);
    final HCLIdentifier var2 = getVar2(intro);
    // May be null in case of incomplete 'for', e.g. '[for a in ]'
    return PsiTreeUtil.getNextSiblingOfType(var2 != null ? var2 : var1, HCLExpression.class);
  }

  public static @NotNull HCLExpression getExpression(@NotNull HCLForArrayExpression expression) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getChildOfType(expression, HCLExpression.class);
  }

  public static @NotNull HCLExpression getKey(@NotNull HCLForObjectExpression expression) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getChildOfType(expression, HCLExpression.class);
  }

  public static @NotNull HCLExpression getValue(@NotNull HCLForObjectExpression expression) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getNextSiblingOfType(getKey(expression), HCLExpression.class);
  }

  public static boolean isGrouping(@NotNull HCLForObjectExpression expression) {
    return expression.getNode().findChildByType(HCLElementTypes.OP_ELLIPSIS) != null;
  }

  public static @NotNull IElementType getOperationSign(HCLUnaryExpression expression) {
    return expression.getNode().getChildren(HCLTokenTypes.getHCL_UNARY_OPERATORS())[0].getElementType();
  }

  public static @NotNull IElementType getOperationSign(HCLBinaryExpression expression) {
    return expression.getNode().getFirstChildNode().getTreeNext().getElementType();
  }

  public static @NotNull HCLIdentifier getMethod(HCLMethodCallExpression expression) {
    return expression.getCallee();
  }

  public static PsiReference @NotNull [] getReferences(@NotNull HCLSelectExpression select) {
   return ReferenceProvidersRegistry.getReferencesFromProviders(select);
  }

  public static @Nullable PsiReference getReference(@NotNull HCLSelectExpression select) {
    PsiReference[] refs = getReferences(select);
    return refs.length != 0 ? refs[0] : null;
  }

}
