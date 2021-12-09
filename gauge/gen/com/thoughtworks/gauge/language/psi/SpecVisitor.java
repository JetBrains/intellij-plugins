// This is a generated file. Not intended for manual editing.
package com.thoughtworks.gauge.language.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.thoughtworks.gauge.language.SpecFile;
import org.jetbrains.annotations.NotNull;

public class SpecVisitor extends PsiElementVisitor {

  public void visitArg(@NotNull SpecArg o) {
    visitPsiElement(o);
  }

  public void visitDynamicArg(@NotNull SpecDynamicArg o) {
    visitPsiElement(o);
  }

  public void visitKeyword(@NotNull SpecKeyword o) {
    visitPsiElement(o);
  }

  public void visitScenario(@NotNull SpecScenario o) {
    visitPsiElement(o);
  }

  public void visitSpecDetail(@NotNull SpecDetail o) {
    visitPsiElement(o);
  }

  public void visitStaticArg(@NotNull SpecStaticArg o) {
    visitPsiElement(o);
  }

  public void visitStep(@NotNull SpecStep o) {
    visitNamedElement(o);
  }

  public void visitTable(@NotNull SpecTable o) {
    visitPsiElement(o);
  }

  public void visitTableBody(@NotNull SpecTableBody o) {
    visitPsiElement(o);
  }

  public void visitTableHeader(@NotNull SpecTableHeader o) {
    visitPsiElement(o);
  }

  public void visitTableRowValue(@NotNull SpecTableRowValue o) {
    visitPsiElement(o);
  }

  public void visitTags(@NotNull SpecTags o) {
    visitPsiElement(o);
  }

  public void visitTeardown(@NotNull SpecTeardown o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull SpecNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

  public void visitSpecFile(SpecFile file) { visitFile(file);}
}
