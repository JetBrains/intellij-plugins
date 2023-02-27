/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
  @NotNull
  public static String getName(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getName(property);
  }

  @NotNull
  public static String getName(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getName(block);
  }

  @NotNull
  public static String getFullName(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getFullName(block);
  }

  @NotNull
  public static HCLExpression getNameElement(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getNameElement(property);
  }

  public static HCLElement @NotNull [] getNameElements(@NotNull HCLBlock block) {
    return HCLPsiImplUtils.INSTANCE.getNameElements(block);
  }

  @Nullable
  public static HCLExpression getValue(@NotNull HCLProperty property) {
    return HCLPsiImplUtils.INSTANCE.getValue(property);
  }

  @Nullable
  public static HCLObject getObject(@NotNull HCLBlock block) {
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

  @NotNull
  public static List<Pair<TextRange, String>> getTextFragments(@NotNull HCLStringLiteral literal) {
    return JavaUtil.getTextFragments(literal);
  }

  @Nullable
  public static HCLProperty findProperty(@NotNull HCLObject object, @NotNull String name) {
    return HCLPsiImplUtils.INSTANCE.findProperty(object, name);
  }

  @NotNull
  public static List<HCLBlock> getBlockList(@NotNull HCLObject object) {
    return PsiTreeUtil.getChildrenOfTypeAsList(object, HCLBlock.class);
  }

  @NotNull
  public static List<HCLExpression> getElements(@NotNull HCLObject object) {
    return PsiTreeUtil.getChildrenOfTypeAsList(object, HCLExpression.class);
  }

  @NotNull
  public static String getValue(@NotNull HCLStringLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  public static char getQuoteSymbol(@NotNull HCLStringLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getQuoteSymbol(literal);
  }

  @NotNull
  public static String getValue(@NotNull HCLHeredocLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  public static boolean isIndented(@NotNull HCLHeredocLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.isIndented(literal);
  }

  @Nullable
  public static Integer getIndentation(@NotNull HCLHeredocLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getIndentation(literal);
  }

  @Nullable
  public static Integer getMinimalIndentation(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getMinimalIndentation(content);
  }

  @NotNull
  public static String getValue(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getValue(content, 0);
  }

  @NotNull
  public static List<String> getLines(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getLines(content);
  }
  @NotNull
  public static List<CharSequence> getLinesRaw(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getLinesRaw(content);
  }

  public static int getLinesCount(@NotNull HCLHeredocContent content) {
    return HCLPsiImplUtils.INSTANCE.getLinesCount(content);
  }

  @NotNull
  public static List<Pair<TextRange, String>> getTextFragments(@NotNull HCLHeredocContent literal) {
    return JavaUtil.getTextFragments(literal);
  }

  @NotNull
  public static String getName(@NotNull HCLHeredocMarker marker) {
    return HCLPsiImplUtils.INSTANCE.getName(marker);
  }

  public static boolean getValue(@NotNull HCLBooleanLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  @NotNull
  public static Number getValue(@NotNull HCLNumberLiteral literal) {
    return HCLPsiImplUtils.INSTANCE.getValue(literal);
  }

  @NotNull
  public static String getId(@NotNull HCLIdentifier identifier) {
    return HCLPsiImplUtils.INSTANCE.getId(identifier);
  }

  @Nullable
  public static HCLIdentifier getVar1(@NotNull HCLForIntro intro) {
    // May be null in case of incomplete 'for', e.g. '[for :]'
    return PsiTreeUtil.getChildOfType(intro, HCLIdentifier.class);
  }

  @Nullable
  public static HCLIdentifier getVar2(@NotNull HCLForIntro intro) {
    final HCLIdentifier var1 = getVar1(intro);
    final PsiElement next = PsiTreeUtil.skipSiblingsForward(var1, PsiWhiteSpace.class);
    if (next != null && next.getNode().getElementType() == HCLElementTypes.COMMA) {
      return PsiTreeUtil.getNextSiblingOfType(next, HCLIdentifier.class);
    }
    return null;
  }

  @Nullable
  public static HCLExpression getContainer(@NotNull HCLForIntro intro) {
    final HCLIdentifier var1 = getVar1(intro);
    final HCLIdentifier var2 = getVar2(intro);
    // May be null in case of incomplete 'for', e.g. '[for a in ]'
    return PsiTreeUtil.getNextSiblingOfType(var2 != null ? var2 : var1, HCLExpression.class);
  }

  @NotNull
  public static HCLExpression getExpression(@NotNull HCLForArrayExpression expression) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getChildOfType(expression, HCLExpression.class);
  }

  @NotNull
  public static HCLExpression getKey(@NotNull HCLForObjectExpression expression) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getChildOfType(expression, HCLExpression.class);
  }

  @NotNull
  public static HCLExpression getValue(@NotNull HCLForObjectExpression expression) {
    //noinspection ConstantConditions
    return PsiTreeUtil.getNextSiblingOfType(getKey(expression), HCLExpression.class);
  }

  public static boolean isGrouping(@NotNull HCLForObjectExpression expression) {
    return expression.getNode().findChildByType(HCLElementTypes.OP_ELLIPSIS) != null;
  }

  @NotNull
  public static IElementType getOperationSign(HCLUnaryExpression expression) {
    return expression.getNode().getChildren(HCLTokenTypes.getHCL_UNARY_OPERATORS())[0].getElementType();
  }

  @NotNull
  public static IElementType getOperationSign(HCLBinaryExpression expression) {
    return expression.getNode().getFirstChildNode().getTreeNext().getElementType();
  }

  @NotNull
  public static HCLIdentifier getMethod(HCLMethodCallExpression expression) {
    return expression.getCallee();
  }

  public static PsiReference @NotNull [] getReferences(@NotNull HCLSelectExpression select) {
   return ReferenceProvidersRegistry.getReferencesFromProviders(select);
  }

  @Nullable
  public static PsiReference getReference(@NotNull HCLSelectExpression select) {
    PsiReference[] refs = getReferences(select);
    return refs.length != 0 ? refs[0] : null;
  }

}
