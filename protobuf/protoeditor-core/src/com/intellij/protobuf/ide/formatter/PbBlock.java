/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.protobuf.lang.psi.PbStatement;
import com.intellij.protobuf.lang.psi.PbTextElementType;
import com.intellij.protobuf.lang.psi.ProtoBlockBody;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A formatting block for protobuf elements. */
public class PbBlock extends AbstractBlock {

  private final SpacingBuilder spacingBuilder;

  PbBlock(
      @NotNull ASTNode node,
      @Nullable Wrap wrap,
      @Nullable Alignment alignment,
      SpacingBuilder spacingBuilder) {
    super(node, wrap, alignment);
    this.spacingBuilder = spacingBuilder;
  }

  @Override
  protected List<Block> buildChildren() {
    if (isLeaf()) {
      return Collections.emptyList();
    }

    List<Block> blocks = new ArrayList<>();
    for (ASTNode child : myNode.getChildren(null)) {
      if (isEmpty(child)) {
        continue;
      }
      if (child.getElementType() instanceof PbTextElementType) {
        blocks.add(new PbTextBlock(child, myWrap, myAlignment, spacingBuilder));
      } else {
        child = deepestLeaf(child);
        blocks.add(new PbBlock(child, myWrap, myAlignment, spacingBuilder));
      }
    }
    return blocks;
  }

  @Override
  public Indent getIndent() {
    if (isEmpty(myNode)) {
      return null;
    }
    PsiElement psi = myNode.getPsi();
    PsiElement parent = psi.getParent();
    // Block children except for the start and end tokens are indented.
    if (parent instanceof ProtoBlockBody) {
      ProtoBlockBody block = (ProtoBlockBody) parent;
      if (psi.equals(block.getStart()) || psi.equals(block.getEnd())) {
        // The start and end tokens are not indented.
        return Indent.getNoneIndent();
      } else {
        // Everything else in a block body is indented.
        return Indent.getNormalIndent();
      }
    }
    // Blocks handle their own indenting.
    if (psi instanceof ProtoBlockBody) {
      return Indent.getNoneIndent();
    }
    // Semicolons are not indented.
    if (ProtoTokenTypes.SEMI.equals(myNode.getElementType())) {
      return Indent.getNoneIndent();
    }
    // Comments outside of a block are not indented.
    if (ProtoTokenTypes.BLOCK_COMMENT.equals(myNode.getElementType())
        || ProtoTokenTypes.LINE_COMMENT.equals(myNode.getElementType())) {
      return Indent.getNoneIndent();
    }
    // Leaves and statement children get continuation indents.
    if (isLeaf() || parent instanceof PbStatement) {
      return Indent.getContinuationWithoutFirstIndent();
    }
    // For everything remaining, no indent.
    return Indent.getNoneIndent();
  }

  @Nullable
  @Override
  public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }

  @Override
  public Indent getChildIndent() {
    PsiElement psi = myNode.getPsi();
    if (psi instanceof PbStatement) {
      return Indent.getContinuationWithoutFirstIndent();
    } else if (psi instanceof ProtoBlockBody) {
      return Indent.getNormalIndent();
    } else {
      return Indent.getNoneIndent();
    }
  }

  private boolean isEmpty(ASTNode node) {
    return node.getElementType() == TokenType.WHITE_SPACE || node.getTextLength() == 0;
  }

  private ASTNode deepestLeaf(ASTNode node) {
    // Find the deepest node with the same text range
    while (node.getFirstChildNode() != null
        && node.getTextRange().equals(node.getFirstChildNode().getTextRange())) {
      node = node.getFirstChildNode();
    }
    return node;
  }
}
