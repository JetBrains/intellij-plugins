// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.BlockEx;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JadeCommentBlock implements Block, BlockEx {
  private static final List<Block> EMPTY_BLOCK_LIST = new ArrayList<>();

  private final ASTNode myNode;
  private final int myStartOffset;
  private final int myEndOffset;
  private final Indent myIndent;

  public JadeCommentBlock(final ASTNode node, final int startOffset, final int endOffset, final Indent indent) {
    myNode = node;
    myStartOffset = startOffset;
    myEndOffset = endOffset;
    myIndent = indent;
  }

  @Override
  public @NotNull TextRange getTextRange() {
    return new TextRange(myNode.getStartOffset() + myStartOffset, myNode.getStartOffset() + myEndOffset);
  }

  @Override
  public @NotNull List<Block> getSubBlocks() {
    return EMPTY_BLOCK_LIST;
  }

  @Override
  public @Nullable Wrap getWrap() {
    return null;
  }

  @Override
  public @Nullable Indent getIndent() {
    return myIndent;
  }

  @Override
  public @Nullable Alignment getAlignment() {
    return null;
  }

  @Override
  public @Nullable Spacing getSpacing(final @Nullable Block child1, final @NotNull Block child2) {
    return null;
  }

  @Override
  public @NotNull ChildAttributes getChildAttributes(final int newChildIndex) {
    return null;
  }

  @Override
  public boolean isIncomplete() {
    return false;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }
}
