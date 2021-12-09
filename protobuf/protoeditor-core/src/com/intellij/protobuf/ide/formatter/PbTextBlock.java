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
import com.intellij.psi.tree.TokenSet;
import com.intellij.protobuf.lang.psi.PbTextFile;
import com.intellij.protobuf.lang.psi.ProtoBlockBody;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A formatter block for prototext elements. */
public class PbTextBlock extends AbstractBlock {
  private static final TokenSet UNINDENTED_SYMBOLS =
      TokenSet.create(ProtoTokenTypes.SEMI, ProtoTokenTypes.COMMA);

  private final SpacingBuilder spacingBuilder;

  public PbTextBlock(
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
      blocks.add(new PbTextBlock(child, myWrap, myAlignment, spacingBuilder));
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
    // Top-level children are not indented.
    if (parent instanceof PbTextFile) {
      return Indent.getNoneIndent();
    }
    // Semicolons and commas aren't indented.
    if (UNINDENTED_SYMBOLS.contains(myNode.getElementType())) {
      return Indent.getNoneIndent();
    }
    // Everything else gets a continuation indent.
    return Indent.getContinuationWithoutFirstIndent();
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
    // Top-level children are not indented.
    if (psi instanceof PbTextFile) {
      return Indent.getNoneIndent();
    }
    // Block children are indented.
    if (psi instanceof ProtoBlockBody) {
      return Indent.getNormalIndent();
    }
    // Everything else gets a continuation indent.
    return Indent.getContinuationWithoutFirstIndent();
  }

  private static boolean isEmpty(ASTNode node) {
    return node.getElementType() == TokenType.WHITE_SPACE || node.getTextLength() == 0;
  }
}
