// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class MakefileVisitor extends PsiElementVisitor {

  public void visitCommands(@NotNull MakefileCommands o) {
    visitPsiElement(o);
  }

  public void visitDependencies(@NotNull MakefileDependencies o) {
    visitPsiElement(o);
  }

  public void visitRule(@NotNull MakefileRule o) {
    visitPsiElement(o);
  }

  public void visitTarget(@NotNull MakefileTarget o) {
    visitPsiElement(o);
  }

  public void visitTargetLine(@NotNull MakefileTargetLine o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
