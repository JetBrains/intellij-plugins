// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.jhipster.psi.JdlEntityFieldMapping;
import com.intellij.jhipster.psi.JdlFieldType;
import com.intellij.jhipster.psi.JdlOptionNameValue;
import com.intellij.jhipster.psi.JdlTokenSets;
import com.intellij.jhipster.psi.JdlTokenTypes;
import com.intellij.jhipster.psi.JdlValue;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.jhipster.JdlCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE;
import static com.intellij.jhipster.psi.JdlTokenSets.BLOCK_IDENTIFIERS;
import static com.intellij.jhipster.psi.JdlTokenSets.BLOCK_KEYWORDS;
import static com.intellij.jhipster.psi.JdlTokenSets.COMMENTS;
import static com.intellij.jhipster.psi.JdlTokenSets.TOP_LEVEL_BLOCKS;
import static com.intellij.psi.formatter.FormatterUtil.isWhitespaceOrEmpty;

final class JdlBlock implements ASTBlock {

  private final ASTNode node;
  private final JdlBlock parent;
  private final PsiElement psiElement;
  private final Alignment alignment;
  private final Indent indent;
  private final Wrap wrap;
  private final Wrap childWrap;
  private final SpacingBuilder spacingBuilder;

  private final JdlCodeStyleSettings jdlCodeStyleSettings;
  private final Alignment propertyValueAlignment;

  private List<Block> subBlocks;

  public JdlBlock(@NotNull ASTNode node,
                  @Nullable JdlBlock parent,
                  @NotNull JdlCodeStyleSettings jdlCodeStyleSettings,
                  @Nullable Alignment alignment,
                  @Nullable Indent indent,
                  @Nullable Wrap wrap,
                  @NotNull SpacingBuilder spacingBuilder) {
    this.node = node;
    this.parent = parent;
    this.psiElement = node.getPsi();
    this.jdlCodeStyleSettings = jdlCodeStyleSettings;
    this.alignment = alignment;
    this.indent = indent;
    this.wrap = wrap;
    this.spacingBuilder = spacingBuilder;

    if (isJdlCodeBlock(node)) {
      childWrap = Wrap.createWrap(jdlCodeStyleSettings.BLOCK_WRAPPING, true);
    }
    else {
      childWrap = null;
    }

    this.propertyValueAlignment = isJdlCodeBlock(node) ? Alignment.createAlignment(true) : null;
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
      int propertyAlignment = jdlCodeStyleSettings.PROPERTY_ALIGNMENT;
      int fieldAlignment = jdlCodeStyleSettings.FIELD_ALIGNMENT;

      var children = node.getChildren(null);
      subBlocks = new ArrayList<>(children.length);

      for (var child : children) {
        if (isWhitespaceOrEmpty(child)) continue;
        if (child.getElementType() == JdlTokenTypes.NEWLINE) continue;

        subBlocks.add(makeSubBlock(child, propertyAlignment, fieldAlignment));
      }
    }
    return subBlocks;
  }

  private Block makeSubBlock(ASTNode child, int propertyAlignment, int fieldAlignment) {
    Indent indent = Indent.getNoneIndent();
    Alignment alignment = null;
    Wrap wrap = null;

    if (isJdlCodeBlock(node)) {
      if (child.getElementType() == JdlTokenTypes.COMMA) {
        wrap = Wrap.createWrap(WrapType.NONE, true);
      }
      else if (!JdlTokenSets.BRACES.contains(child.getElementType())
               && !BLOCK_KEYWORDS.contains(child.getElementType())
               && !BLOCK_IDENTIFIERS.contains(child.getElementType())) {

        if (!COMMENTS.contains(child.getElementType())) {
          wrap = childWrap;
        }

        if (!isEntityAnnotation(child)) {
          indent = Indent.getNormalIndent();
        }
      }
    }
    else if (child.getPsi() instanceof JdlValue
             && parent != null
             && psiElement instanceof JdlOptionNameValue
             && propertyAlignment == ALIGN_PROPERTY_ON_VALUE) {
      alignment = parent.propertyValueAlignment;
    }
    else if (child.getPsi() instanceof JdlFieldType
             && parent != null
             && psiElement instanceof JdlEntityFieldMapping
             && fieldAlignment == ALIGN_PROPERTY_ON_VALUE) {
      alignment = parent.propertyValueAlignment;
    }

    return new JdlBlock(child, this, jdlCodeStyleSettings, alignment, indent, wrap, spacingBuilder);
  }

  private boolean isEntityAnnotation(ASTNode child) {
    if (child.getElementType() != JdlTokenTypes.ANNOTATION) return false;

    ASTNode treeParent = child.getTreeParent();
    return treeParent.getElementType() == JdlTokenTypes.ENTITY;
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

    if (isJdlCodeBlock(node)) {
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }

    return new ChildAttributes(null, null);
  }

  @Override
  public boolean isIncomplete() {
    var lastChildNode = node.getLastChildNode();
    if (isJdlCodeBlock(node)) {
      return lastChildNode != null && lastChildNode.getElementType() != JdlTokenTypes.RBRACE;
    }
    if (node.getElementType() == JdlTokenTypes.ARRAY_LITERAL) {
      return lastChildNode != null && lastChildNode.getElementType() != JdlTokenTypes.RBRACKET;
    }
    return false;
  }

  private boolean isJdlCodeBlock(ASTNode node) {
    return TOP_LEVEL_BLOCKS.contains(node.getElementType())
           || node.getElementType() == JdlTokenTypes.CONFIG_BLOCK;
  }

  @Override
  public boolean isLeaf() {
    return node.getFirstChildNode() == null;
  }
}