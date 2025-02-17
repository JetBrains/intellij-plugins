// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlChildRole;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.CharTable;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import org.jetbrains.annotations.NotNull;

public class JadeTagImpl extends XmlTagImpl implements HtmlTag {

  public static final String DIV = "div";

  public JadeTagImpl() {
    super(JadeElementTypes.TAG);
  }

  public JadeTagImpl(IElementType type) {
    super(type);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
  }

  @Override
  public @NotNull String getName() {
    PsiElement child = getFirstChild();
    if (child != null && child.getNode().getElementType() == XmlTokenType.XML_TAG_NAME) {
      return child.getText();
    }
    return DIV;
  }

  @Override
  public XmlAttribute getAttribute(String qname) {
    if (qname == null) return null;
    final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(this);
    final XmlAttribute[] attributes = getAttributes();

    final CharSequence charTableIndex = charTableByTree.intern(qname);
    final boolean caseSensitive = isCaseSensitive();

    for (final XmlAttribute attribute : attributes) {
      final ASTNode attrNameElement = XmlChildRole.ATTRIBUTE_NAME_FINDER.findChild(attribute.getNode());
      final CharSequence name = attrNameElement instanceof JadeFakeXmlNameElement ? charTableByTree.intern(attribute.getLocalName()) :
                                attrNameElement != null ? attrNameElement.getChars() : null;
      if (name != null && (caseSensitive && name.equals(charTableIndex) ||
                           !caseSensitive && Comparing.equal(name, charTableIndex, false))) {
        return attribute;
      }
    }
    return null;
  }

  @Override
  public String getAttributeValue(String qname) {
    if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(qname)) {
      final JadeAttributeImpl[] attributes = PsiTreeUtil.getChildrenOfType(this, JadeAttributeImpl.class);
      if (attributes != null && attributes.length > 0) {
        StringBuilder s = new StringBuilder();
        for (JadeAttributeImpl attribute : attributes) {
          final JadeAttributeValueImpl valueElement = attribute.getValueElement();
          if (valueElement == null) {
            continue;
          }

          if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attribute.getName()) || valueElement.isSyntheticClass()) {
            if (!s.isEmpty()) {
              s.append(' ');
            }
            s.append(valueElement.getValue());
          }
        }

        return s.toString();
      }
    }
    return super.getAttributeValue(qname);
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }
}
