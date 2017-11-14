package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;

/**
 * @author yole
 */
public class GherkinRecursiveElementVisitor extends GherkinElementVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}
