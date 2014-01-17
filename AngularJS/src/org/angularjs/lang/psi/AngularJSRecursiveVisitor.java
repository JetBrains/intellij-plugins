package org.angularjs.lang.psi;

import com.intellij.psi.PsiElement;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSRecursiveVisitor extends AngularJSElementVisitor {
  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}