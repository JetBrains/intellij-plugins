// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.formatter.xml.XmlBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.formatter.xml.XmlTagBlock;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.lang.html.psi.formatter.Angular2HtmlTagBlock.isAngular2AttributeElementType;

public class Angular2HtmlBlock extends XmlBlock {

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
  protected @NotNull XmlBlock createSimpleChild(@NotNull ASTNode child, @Nullable Indent indent,
                                                @Nullable Wrap wrap, @Nullable Alignment alignment, @Nullable TextRange range) {
    return new Angular2HtmlBlock(child, wrap, alignment, myXmlFormattingPolicy, indent, range, isPreserveSpace());
  }

  @Override
  protected boolean useMyFormatter(Language myLanguage, Language childLanguage, PsiElement childPsi) {
    return (childLanguage != null && childLanguage.isKindOf(Angular2HtmlLanguage.INSTANCE))
           || super.useMyFormatter(myLanguage, childLanguage, childPsi);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getElementType() instanceof Angular2EmbeddedExprTokenType
           || super.isLeaf();
  }

  @Override
  protected boolean isAttributeElementType(IElementType elementType) {
    return isAngular2AttributeElementType(elementType);
  }
}
