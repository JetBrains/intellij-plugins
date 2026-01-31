// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.xml.XmlDoctypeImpl;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlDoctype;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlProlog;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ArrayUtil;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class JadeDoctypeImpl extends XmlDoctypeImpl implements XmlProlog {
  private static final Map<String, String> URIS;

  static {
    URIS = new HashMap<>();
    URIS.put("html", null);
    URIS.put("xml", null);
    URIS.put("transitional", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
    URIS.put("strict", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
    URIS.put("frameset", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd");
    URIS.put("1.1", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
    URIS.put("basic", "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd");
    URIS.put("mobile", "http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd");
    URIS.put("plist", "http://www.apple.com/DTDs/PropertyList-1.0.dtd");
  }

  @Override
  public @Nullable String getDtdUri() {
    final String value = getValue();
    if (value == null) {
      return null;
    }
    return URIS.get(StringUtil.toLowerCase(value));
  }

  private @Nullable String getValue() {
    ASTNode node = getUrlNode();
    return node != null ? node.getText() : null;
  }

  private ASTNode getUrlNode() {
    return getNode().findChildByType(TokenSet.create(JadeTokenTypes.NUMBER, XmlElementType.XML_TEXT, XmlTokenType.XML_DATA_CHARACTERS));
  }

  @Override
  protected @Nullable PsiReference createUrlReference(PsiElement dtdUrlElement) {
    ASTNode node = getUrlNode();
    if (node == null) {
      return null;
    }
    TextRange textRange = TextRange.from(node.getPsi().getStartOffsetInParent() + 1, node.getTextLength() - 1);
    return new PsiReferenceBase.Immediate<>(dtdUrlElement, textRange, this) {
      @Override
      public Object @NotNull [] getVariants() {
        return ArrayUtil.toObjectArray(URIS.keySet());
      }
    };
  }

  @Override
  public XmlElement getDtdUrlElement() {
    return this;
  }

  @Override
  public XmlElement getNameElement() {
    return this;
  }

  @Override
  public XmlDoctype getDoctype() {
    return this;
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }
}
