// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.tsr.psi.TslTokenSets;
import com.intellij.tsr.psi.TslTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.tsr.TslUtils.isCompactPropertyBlock;

final class TslFoldingBuilder extends CustomFoldingBuilder implements DumbAware {
  @Override
  protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }

  @Override
  protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root,
                                          @NotNull Document document, boolean quick) {
    ASTNode node = root.getNode();
    if (node != null) {
      IElementType elementType = node.getElementType();
      if (isBracesFoldingParent(elementType) && !isCompactPropertyBlock(node.getPsi())) {
        ASTNode lbrace = node.findChildByType(TslTokenSets.OPEN_BRACES);
        ASTNode rbrace = node.findChildByType(TslTokenSets.CLOSE_BRACES);

        if (lbrace != null && rbrace != null) {
          TextRange textRange = node.getTextRange();
          TextRange foldingRange = new TextRange(textRange.getStartOffset() + lbrace.getStartOffsetInParent(),
              textRange.getStartOffset() + rbrace.getStartOffsetInParent() + rbrace.getTextLength());
          descriptors.add(new FoldingDescriptor(node, foldingRange));
        }
      }
    }

    for (var child : root.getChildren()) {
      buildLanguageFoldRegions(descriptors, child, document, quick);
    }
  }

  @Override
  protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
    IElementType elementType = node.getElementType();
    if (elementType == TslTokenTypes.LIST) {
      return "[...]";
    }
    if (elementType == TslTokenTypes.MAP) {
      return "{...}";
    }
    if (isBracesFoldingParent(elementType)) {
      return "(...)";
    }

    return null;
  }

  private static boolean isBracesFoldingParent(IElementType elementType) {
    return elementType == TslTokenTypes.OBJECT_BRACE
        || elementType == TslTokenTypes.OBJECT_PARENTH
        || elementType == TslTokenTypes.OBJECT_BRACKET
        || elementType == TslTokenTypes.LIST
        || elementType == TslTokenTypes.MAP;
  }
}
