/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.formatting;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
class OgnlBlock implements ASTBlock {

  private final ASTNode astNode;
  private final SpacingBuilder spacingBuilder;
  private List<Block> blocks;

  OgnlBlock(final ASTNode astNode,
            final SpacingBuilder spacingBuilder) {
    this.astNode = astNode;
    this.spacingBuilder = spacingBuilder;
  }

  @Override
  public ASTNode getNode() {
    return astNode;
  }

  @NotNull
  @Override
  public TextRange getTextRange() {
    return astNode.getTextRange();
  }

  @NotNull
  @Override
  public List<Block> getSubBlocks() {
    if (blocks == null) {
      blocks = buildSubBlocks();
    }
    return new ArrayList<>(blocks);
  }

  private List<Block> buildSubBlocks() {
    final List<Block> myBlocks = new ArrayList<>();
    for (ASTNode child = astNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (child.getTextRange().getLength() == 0) {
        continue;
      }

      if (child.getElementType() == TokenType.WHITE_SPACE) {
        continue;
      }

      myBlocks.add(new OgnlBlock(child, spacingBuilder));
    }
    return Collections.unmodifiableList(myBlocks);
  }

  @Override
  public Wrap getWrap() {
    return null;
  }

  @Override
  public Indent getIndent() {
    return Indent.getNoneIndent();
  }

  @Override
  public Alignment getAlignment() {
    return null;
  }

  @Override
  public Spacing getSpacing(final Block child1, @NotNull final Block child2) {
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @NotNull
  @Override
  public ChildAttributes getChildAttributes(final int newChildIndex) {
    return new ChildAttributes(Indent.getNoneIndent(), null);
  }

  @Override
  public boolean isIncomplete() {
    return false;
  }

  @Override
  public boolean isLeaf() {
    return astNode.getFirstChildNode() == null;
  }

}
