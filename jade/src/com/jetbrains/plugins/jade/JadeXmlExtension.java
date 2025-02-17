package com.jetbrains.plugins.jade;

import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.util.XmlUtil;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import com.jetbrains.plugins.jade.psi.references.JadeTagNameReference;

public final class JadeXmlExtension extends DefaultXmlExtension {
  @Override
  public boolean isAvailable(PsiFile file) {
    return file instanceof JadeFileImpl;
  }

  @Override
  public TagNameReference createTagNameReference(ASTNode nameElement, boolean startTagFlag) {
    return new JadeTagNameReference(nameElement);
  }

  @Override
  public String[][] getNamespacesFromDocument(XmlDocument parent, boolean declarationsExist) {
    final String[][] document = super.getNamespacesFromDocument(parent, declarationsExist);
    final String doctype = ExternalResourceManagerEx.getInstanceEx().getDefaultHtmlDoctype(parent.getProject());
    final String realDoctype = XmlUtil.XHTML_URI.equals(doctype) ? "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" : XmlUtil.HTML_URI;
    if (document == null || document[0][1].endsWith("html5.rnc")) {
      return new String[][]{new String[]{"", realDoctype}};
    }
    return document;
  }

  @Override
  public boolean useXmlTagInsertHandler() {
    return false;
  }
}
