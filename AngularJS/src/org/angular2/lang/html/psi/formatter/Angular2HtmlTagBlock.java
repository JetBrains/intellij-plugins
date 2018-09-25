// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.formatter.xml.XmlBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.formatter.xml.XmlTagBlock;
import org.angular2.lang.html.Angular2HtmlLanguage;

import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.ALL_ATTRIBUTES;

public class Angular2HtmlTagBlock extends XmlTagBlock {

  public Angular2HtmlTagBlock(ASTNode node,
                              Wrap wrap,
                              Alignment alignment,
                              XmlFormattingPolicy policy, Indent indent) {
    super(node, wrap, alignment, policy, indent);
  }

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
    if (isAngular2Attribute(child) && myXmlFormattingPolicy.getShouldAlignAttributes()) return attrAlignment;
    return super.chooseAlignment(child, attrAlignment, textAlignment);
  }

  static boolean isAngular2Attribute(ASTNode node) {
    return ALL_ATTRIBUTES.contains(node.getElementType());
  }

  @Override
  protected boolean useMyFormatter(Language myLanguage, Language childLanguage, PsiElement childPsi) {
    return (childLanguage == Angular2HtmlLanguage.INSTANCE)
           || super.useMyFormatter(myLanguage, childLanguage, childPsi);
  }
}
