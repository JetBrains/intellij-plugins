package org.angularjs.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSRecursiveVisitor extends AngularJSElementVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}