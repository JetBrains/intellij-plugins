package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.CfmlComponentReference;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 * @date 16.02.11
 */
public class CfmlImportImpl extends CfmlTagImpl implements CfmlImport {
  public CfmlImportImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public boolean isImported(String componentName) {
    String importString = getImportString();
    return importString != null ? importString.endsWith(componentName) : false;
  }

  @Override
  public String getImportString() {
    PsiElement taglib = getAttributeValueElement("taglib");
    return taglib != null ? taglib.getText() : null;
  }

  @Override
  public String getPrefix() {
    PsiElement taglib = getAttributeValueElement("prefix");
    return taglib != null ? taglib.getText() : null;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    PsiElement taglib = getAttributeValueElement("taglib");
    if (taglib != null) {
      return new PsiReference[] {new CfmlComponentReference(taglib.getNode(), this)};
    }
    return super.getReferences();
  }
}
