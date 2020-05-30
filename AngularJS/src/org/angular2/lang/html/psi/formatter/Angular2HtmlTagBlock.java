// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
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
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.intellij.psi.xml.XmlElementType.XML_ATTRIBUTE;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.ALL_ATTRIBUTES;

public class Angular2HtmlTagBlock extends XmlTagBlock {

  public Angular2HtmlTagBlock(ASTNode node,
                              Wrap wrap,
                              Alignment alignment,
                              XmlFormattingPolicy policy, Indent indent, boolean preserveSpace) {
    super(node, wrap, alignment, policy, indent, preserveSpace);
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
  protected Block createSyntheticBlock(ArrayList<Block> localResult, Indent childrenIndent) {
    return new Angular2SyntheticBlock(localResult, this, Indent.getNoneIndent(), myXmlFormattingPolicy, childrenIndent);
  }

  @Override
  protected boolean useMyFormatter(Language myLanguage, Language childLanguage, PsiElement childPsi) {
    return (childLanguage != null && childLanguage.isKindOf(Angular2HtmlLanguage.INSTANCE))
           || super.useMyFormatter(myLanguage, childLanguage, childPsi);
  }

  @Override
  protected boolean isAttributeElementType(IElementType elementType) {
    return isAngular2AttributeElementType(elementType);
  }

  static boolean isAngular2AttributeElementType(IElementType elementType) {
    return elementType == XML_ATTRIBUTE || ALL_ATTRIBUTES.contains(elementType);
  }
}
