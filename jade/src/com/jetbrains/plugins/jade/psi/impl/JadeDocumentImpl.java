package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.HtmlDocumentImpl;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlProlog;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JadeDocumentImpl extends HtmlDocumentImpl {

  public JadeDocumentImpl() {
    super(JadeElementTypes.DOCUMENT);
  }

  @Override
  public @Nullable XmlProlog getProlog() {
    final PsiElement prolog = findElementByTokenType(XmlElementType.XML_DOCTYPE);
    return prolog instanceof XmlProlog ? (XmlProlog)prolog : null;
  }

  @Override
  public XmlTag getRootTag() {
    return (XmlTag)findElementByTokenType(JadeElementTypes.TAG);
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }
}
