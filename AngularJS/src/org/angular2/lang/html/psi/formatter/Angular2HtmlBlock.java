// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.formatter.xml.XmlBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.formatter.xml.XmlTagBlock;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.html.psi.formatter.Angular2HtmlTagBlock.isAngular2Attribute;

public class Angular2HtmlBlock extends XmlBlock {
  public Angular2HtmlBlock(ASTNode node,
                           Wrap wrap,
                           Alignment alignment,
                           XmlFormattingPolicy policy,
                           Indent indent, TextRange textRange) {
    super(node, wrap, alignment, policy, indent, textRange);
  }

  public Angular2HtmlBlock(ASTNode node,
                           Wrap wrap,
                           Alignment alignment,
                           XmlFormattingPolicy policy, Indent indent, TextRange textRange, boolean preserveSpace) {
    super(node, wrap, alignment, policy, indent, textRange, preserveSpace);
  }

  @Override
  protected XmlTagBlock createTagBlock(ASTNode child, Indent indent, Wrap wrap, Alignment alignment) {
    return new Angular2HtmlTagBlock(child, wrap, alignment, myXmlFormattingPolicy, indent != null ? indent : Indent.getNoneIndent(),
                                    isPreserveSpace());
  }

  @Override
  protected XmlBlock createSimpleChild(ASTNode child, Indent indent, Wrap wrap, Alignment alignment) {
    return new Angular2HtmlBlock(child, wrap, alignment, myXmlFormattingPolicy, indent, null, isPreserveSpace());
  }

  @Override
  protected Wrap chooseWrap(ASTNode child, Wrap tagBeginWrap, Wrap attrWrap, Wrap textWrap) {
    if (isAngular2Attribute(child)) {
      return attrWrap;
    }
    return super.chooseWrap(child, tagBeginWrap, attrWrap, textWrap);
  }

  @Override
  protected Alignment chooseAlignment(ASTNode child, Alignment attrAlignment, Alignment textAlignment) {
    if (isAngular2Attribute(child) && myXmlFormattingPolicy.getShouldAlignAttributes()) {
      return attrAlignment;
    }
    return super.chooseAlignment(child, attrAlignment, textAlignment);
  }

  @Override
  protected boolean useMyFormatter(Language myLanguage, Language childLanguage, PsiElement childPsi) {
    return (childLanguage == Angular2HtmlLanguage.INSTANCE)
           || super.useMyFormatter(myLanguage, childLanguage, childPsi);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getElementType() instanceof Angular2EmbeddedExprTokenType
           || super.isLeaf();
  }

  @SuppressWarnings("Duplicates")
  @Override
  public Spacing getSpacing(Block child1, @NotNull Block child2) {
    if (!(child1 instanceof AbstractBlock) || !(child2 instanceof AbstractBlock)) {
      return null;
    }

    final ASTNode node1 = ((AbstractBlock)child1).getNode();
    final IElementType type1 = node1.getElementType();
    final ASTNode node2 = ((AbstractBlock)child2).getNode();
    final IElementType type2 = node2.getElementType();

    if (isAngular2Attribute(myNode)) {
      return getSpacesInsideAttribute(type1, type2);
    }
    return super.getSpacing(child1, child2);
  }
}
