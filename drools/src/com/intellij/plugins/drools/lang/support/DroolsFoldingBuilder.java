// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.support;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class DroolsFoldingBuilder implements FoldingBuilder, DumbAware {

  @Override
  public FoldingDescriptor @NotNull [] buildFoldRegions(final @NotNull ASTNode node, final @NotNull Document document) {
    final PsiElement element = node.getPsi();
    if (element instanceof DroolsFile file) {
      List<FoldingDescriptor> descriptors = new ArrayList<>();
      for (DroolsRuleStatement rule : file.getRules()) {
        addRuleFoldingDescriptors(descriptors, rule);
      }

      for (DroolsDeclareStatement declareStatement : file.getDeclarations()) {
        addDeclareFoldingDescriptors(descriptors, declareStatement);
      }

      for (DroolsQueryStatement queryStatement : file.getQueries()) {
        addQueryFoldingDescriptors(descriptors, queryStatement);
      }

      for (DroolsFunctionStatement functionStatement : file.getFunctions()) {
        DroolsBlock block = functionStatement.getBlock();
        if (block != null) {
          final int start = block.getTextRange().getStartOffset() + 1;
          final int end = block.getTextRange().getEndOffset() - 1;
          if (start + 1 < end) {
            descriptors.add(new FoldingDescriptor(functionStatement.getNode(), new TextRange(start, end)));
          }
        }
      }
      return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }
    return FoldingDescriptor.EMPTY_ARRAY;
  }

  private static void addRuleFoldingDescriptors(final List<FoldingDescriptor> descriptors, final DroolsRuleStatement rule) {
    if (!rule.textContains('\n')) return;

    PsiElement nameElement = rule.getRuleName();

    final int start = nameElement.getTextRange().getEndOffset();
    final int end = rule.getTextRange().getEndOffset();
    if (start + 1 < end) {
      descriptors.add(new FoldingDescriptor(rule.getNode(), new TextRange(start, end)));
    }
  }

  private static void addDeclareFoldingDescriptors(final List<FoldingDescriptor> descriptors, final DroolsDeclareStatement statement) {
    if (!statement.textContains('\n')) return;

    DroolsTypeDeclaration declaration = statement.getTypeDeclaration();
    if (declaration != null) {
      PsiElement nameElement = declaration.getTypeName();

      final int start = nameElement.getTextRange().getEndOffset();
      final int end = statement.getTextRange().getEndOffset();
      if (start + 1 < end) {
        descriptors.add(new FoldingDescriptor(statement.getNode(), new TextRange(start, end)));
      }
    }
  }

  private static void addQueryFoldingDescriptors(final List<FoldingDescriptor> descriptors, final DroolsQueryStatement statement) {
    if (!statement.textContains('\n')) return;

    PsiElement nameElement = statement.getStringId();

    final int start = nameElement.getTextRange().getEndOffset();
    final int end = statement.getTextRange().getEndOffset();
    if (start + 1 < end) {
      descriptors.add(new FoldingDescriptor(statement.getNode(), new TextRange(start, end)));
    }
  }

  @Override
  public String getPlaceholderText(final @NotNull ASTNode node) {
    if (node.getElementType() == DroolsTokenTypes.RULE_STATEMENT) return "<~>";

    return "...";
  }

  @Override
  public boolean isCollapsedByDefault(final @NotNull ASTNode node) {
    return false;
  }
}
