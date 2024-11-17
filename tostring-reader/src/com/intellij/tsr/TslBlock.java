// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.tsr.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.psi.formatter.FormatterUtil.isWhitespaceOrEmpty;
import static com.intellij.tsr.TslUtils.isCompactPropertyBlock;

final class TslBlock implements ASTBlock {

  private final ASTNode node;
  private final TslBlock parent;
  private final PsiElement psiElement;
  private final Alignment alignment;
  private final Indent indent;
  private final Wrap wrap;
  private final SpacingBuilder spacingBuilder;

  private List<Block> subBlocks;

  public TslBlock(@NotNull ASTNode node,
                  @Nullable TslBlock parent,
                  @Nullable Alignment alignment,
                  @Nullable Indent indent,
                  @Nullable Wrap wrap,
                  @NotNull SpacingBuilder spacingBuilder) {
    this.node = node;
    this.parent = parent;
    this.psiElement = node.getPsi();
    this.alignment = alignment;
    this.indent = indent;
    this.wrap = wrap;
    this.spacingBuilder = spacingBuilder;
  }

  @Override
  public @Nullable ASTNode getNode() {
    return node;
  }

  @Override
  public @NotNull TextRange getTextRange() {
    return node.getTextRange();
  }

  @Override
  public @NotNull List<Block> getSubBlocks() {
    if (subBlocks == null) {
      var children = node.getChildren(null);
      subBlocks = new ArrayList<>(children.length);

      for (var child : children) {
        if (isWhitespaceOrEmpty(child)) continue;

        subBlocks.add(makeSubBlock(child));
      }
    }
    return subBlocks;
  }

  private Block makeSubBlock(ASTNode child) {
    Indent indent = Indent.getNoneIndent();
    Wrap wrap = null;

    if (isTslBlock(node)) {
      if (child.getElementType() == TslTokenTypes.COMMA) {
        wrap = Wrap.createWrap(WrapType.NONE, true);
      } else if (!TslTokenSets.BRACES.contains(child.getElementType())
          && !TslTokenSets.BLOCK_IDENTIFIERS.contains(child.getElementType())) {
        indent = Indent.getNormalIndent();

        if (!isCompactPropertyBlock(psiElement)) {
          wrap = Wrap.createWrap(CommonCodeStyleSettings.WRAP_ALWAYS, true);
        }
      }
    }

    if (!isCompactPropertyBlock(psiElement)) {
      if (child.getElementType() == TslTokenTypes.MAP_ITEM
          || child.getElementType() == TslTokenTypes.PROPERTY_KEY_VALUE) {
        wrap = Wrap.createWrap(CommonCodeStyleSettings.WRAP_ALWAYS, true);
      }

      if (child.getElementType() == TslTokenTypes.RBRACE
          || child.getElementType() == TslTokenTypes.RPARENTH
          || child.getElementType() == TslTokenTypes.RBRACKET) {
        wrap = Wrap.createWrap(CommonCodeStyleSettings.WRAP_ALWAYS, true);
      }
    }

    return new TslBlock(child, this, null, indent, wrap, spacingBuilder);
  }

  @Override
  public @Nullable Wrap getWrap() {
    return wrap;
  }

  @Override
  public @Nullable Indent getIndent() {
    return indent;
  }

  @Override
  public @Nullable Alignment getAlignment() {
    return alignment;
  }

  @Override
  public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
    if (node.getPsi() instanceof PsiFile) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }

    if (isTslBlock(node)) {
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }

    return new ChildAttributes(null, null);
  }

  private boolean isTslBlock(ASTNode node) {
    return node.getElementType() == TslTokenTypes.LIST
        || node.getElementType() == TslTokenTypes.MAP
        || node.getElementType() == TslTokenTypes.OBJECT_BRACE
        || node.getElementType() == TslTokenTypes.OBJECT_BRACKET
        || node.getElementType() == TslTokenTypes.OBJECT_PARENTH;
  }

  @Override
  public boolean isIncomplete() {
    var lastChildNode = node.getLastChildNode();
    if (node.getElementType() == TslTokenTypes.MAP) {
      return lastChildNode != null && lastChildNode.getElementType() != TslTokenTypes.RBRACE;
    }
    if (node.getElementType() == TslTokenTypes.LIST) {
      return lastChildNode != null && lastChildNode.getElementType() != TslTokenTypes.RBRACKET;
    }
    if (node.getElementType() == TslTokenTypes.OBJECT_PARENTH) {
      return lastChildNode != null && lastChildNode.getElementType() != TslTokenTypes.RPARENTH;
    }
    if (node.getElementType() == TslTokenTypes.OBJECT_BRACE) {
      return lastChildNode != null && lastChildNode.getElementType() != TslTokenTypes.RBRACE;
    }
    if (node.getElementType() == TslTokenTypes.OBJECT_BRACKET) {
      return lastChildNode != null && lastChildNode.getElementType() != TslTokenTypes.RBRACKET;
    }
    return false;
  }

  @Override
  public boolean isLeaf() {
    return node instanceof TslObjectName
        || node instanceof TslObjectId
        || node instanceof TslObjectRef
        || node instanceof TslFallbackStringLiteral
        || node.getFirstChildNode() == null;
  }
}