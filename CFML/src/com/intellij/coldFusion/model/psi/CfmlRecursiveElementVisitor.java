package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

public class CfmlRecursiveElementVisitor extends PsiElementVisitor {

  public static class Stop extends RuntimeException {
    public static final Stop DONE = new Stop();

    public Throwable fillInStackTrace() {
      return this;
    }
  }

  public void visitElement(final PsiElement element) {
    element.acceptChildren(this);
  }

  public void visitCfmlFunction(CfmlFunction function) {
    visitElement(function);
  }

  public void visitCfmlComponent(CfmlComponent component) {
    visitElement(component);
  }

  public void visitCfmlTag(CfmlTag tag) {
    visitElement(tag);
  }

  @Override
  public void visitFile(PsiFile file) {
    if (file instanceof CfmlFile) {
      file.acceptChildren(this);
    }
  }
}
