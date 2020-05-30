package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class GherkinRecursiveElementVisitor extends GherkinElementVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(@NotNull PsiElement element) {
    element.acceptChildren(this);
  }
}
