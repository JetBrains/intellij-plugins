// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

public class MakefileVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull MakefileBlock o) {
    visitPsiElement(o);
  }

  public void visitCommand(@NotNull MakefileCommand o) {
    visitPsiElement(o);
  }

  public void visitComment(@NotNull MakefileComment o) {
    visitPsiElement(o);
  }

  public void visitCondition(@NotNull MakefileCondition o) {
    visitPsiElement(o);
  }

  public void visitConditional(@NotNull MakefileConditional o) {
    visitPsiElement(o);
  }

  public void visitConditionalElse(@NotNull MakefileConditionalElse o) {
    visitPsiElement(o);
  }

  public void visitDefine(@NotNull MakefileDefine o) {
    visitPsiElement(o);
  }

  public void visitDirective(@NotNull MakefileDirective o) {
    visitPsiElement(o);
  }

  public void visitDirectory(@NotNull MakefileDirectory o) {
    visitPsiElement(o);
  }

  public void visitDocComment(@NotNull MakefileDocComment o) {
    visitPsiElement(o);
  }

  public void visitExport(@NotNull MakefileExport o) {
    visitPsiElement(o);
  }

  public void visitFilename(@NotNull MakefileFilename o) {
    visitPsiElement(o);
  }

  public void visitFunction(@NotNull MakefileFunction o) {
    visitPsiLanguageInjectionHost(o);
  }

  public void visitFunctionName(@NotNull MakefileFunctionName o) {
    visitPsiElement(o);
  }

  public void visitFunctionParam(@NotNull MakefileFunctionParam o) {
    visitPsiElement(o);
  }

  public void visitIdentifier(@NotNull MakefileIdentifier o) {
    visitPsiElement(o);
  }

  public void visitInclude(@NotNull MakefileInclude o) {
    visitPsiElement(o);
  }

  public void visitInlineCommand(@NotNull MakefileInlineCommand o) {
    visitPsiElement(o);
  }

  public void visitNormalPrerequisites(@NotNull MakefileNormalPrerequisites o) {
    visitPsiElement(o);
  }

  public void visitOrderOnlyPrerequisites(@NotNull MakefileOrderOnlyPrerequisites o) {
    visitPsiElement(o);
  }

  public void visitOverride(@NotNull MakefileOverride o) {
    visitPsiElement(o);
  }

  public void visitPattern(@NotNull MakefilePattern o) {
    visitPsiElement(o);
  }

  public void visitPrerequisite(@NotNull MakefilePrerequisite o) {
    visitPsiElement(o);
  }

  public void visitPrerequisites(@NotNull MakefilePrerequisites o) {
    visitPsiElement(o);
  }

  public void visitPrivatevar(@NotNull MakefilePrivatevar o) {
    visitPsiElement(o);
  }

  public void visitRecipe(@NotNull MakefileRecipe o) {
    visitPsiLanguageInjectionHost(o);
  }

  public void visitRule(@NotNull MakefileRule o) {
    visitPsiElement(o);
  }

  public void visitString(@NotNull MakefileString o) {
    visitPsiElement(o);
  }

  public void visitSubstitution(@NotNull MakefileSubstitution o) {
    visitPsiLanguageInjectionHost(o);
  }

  public void visitTarget(@NotNull MakefileTarget o) {
    visitNamedElement(o);
    // visitNavigationItem(o);
  }

  public void visitTargetLine(@NotNull MakefileTargetLine o) {
    visitPsiElement(o);
  }

  public void visitTargetPattern(@NotNull MakefileTargetPattern o) {
    visitPsiElement(o);
  }

  public void visitTargets(@NotNull MakefileTargets o) {
    visitPsiElement(o);
  }

  public void visitUndefine(@NotNull MakefileUndefine o) {
    visitPsiElement(o);
  }

  public void visitVariable(@NotNull MakefileVariable o) {
    visitNamedElement(o);
    // visitNavigationItem(o);
  }

  public void visitVariableAssignment(@NotNull MakefileVariableAssignment o) {
    visitPsiElement(o);
  }

  public void visitVariableUsage(@NotNull MakefileVariableUsage o) {
    visitPsiElement(o);
  }

  public void visitVariableValue(@NotNull MakefileVariableValue o) {
    visitPsiElement(o);
  }

  public void visitVpath(@NotNull MakefileVpath o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull MakefileNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiLanguageInjectionHost(@NotNull PsiLanguageInjectionHost o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
