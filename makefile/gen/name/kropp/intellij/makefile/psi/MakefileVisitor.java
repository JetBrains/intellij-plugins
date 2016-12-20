// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class MakefileVisitor extends PsiElementVisitor {

  public void visitCommands(@NotNull MakefileCommands o) {
    visitPsiElement(o);
  }

  public void visitConditional(@NotNull MakefileConditional o) {
    visitPsiElement(o);
  }

  public void visitDependencies(@NotNull MakefileDependencies o) {
    visitPsiElement(o);
  }

  public void visitDependency(@NotNull MakefileDependency o) {
    visitPsiElement(o);
  }

  public void visitElse_(@NotNull MakefileElse_ o) {
    visitPsiElement(o);
  }

  public void visitElsebranch(@NotNull MakefileElsebranch o) {
    visitPsiElement(o);
  }

  public void visitEndif(@NotNull MakefileEndif o) {
    visitPsiElement(o);
  }

  public void visitIfeq(@NotNull MakefileIfeq o) {
    visitPsiElement(o);
  }

  public void visitInclude(@NotNull MakefileInclude o) {
    visitPsiElement(o);
  }

  public void visitRule(@NotNull MakefileRule o) {
    visitPsiElement(o);
  }

  public void visitTarget(@NotNull MakefileTarget o) {
    visitNamedElement(o);
  }

  public void visitTargetLine(@NotNull MakefileTargetLine o) {
    visitPsiElement(o);
  }

  public void visitThenbranch(@NotNull MakefileThenbranch o) {
    visitPsiElement(o);
  }

  public void visitVariable(@NotNull MakefileVariable o) {
    visitPsiElement(o);
  }

  public void visitVariableName(@NotNull MakefileVariableName o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull MakefileNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
