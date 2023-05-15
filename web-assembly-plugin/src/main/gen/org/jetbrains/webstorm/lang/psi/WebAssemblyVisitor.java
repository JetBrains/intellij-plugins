// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class WebAssemblyVisitor extends PsiElementVisitor {

  public void visitAligneq(@NotNull WebAssemblyAligneq o) {
    visitPsiElement(o);
  }

  public void visitBlockinstr(@NotNull WebAssemblyBlockinstr o) {
    visitPsiElement(o);
  }

  public void visitBlocktype(@NotNull WebAssemblyBlocktype o) {
    visitPsiElement(o);
  }

  public void visitCallIndirectInstr(@NotNull WebAssemblyCallIndirectInstr o) {
    visitReferencedElement(o);
  }

  public void visitCallInstr(@NotNull WebAssemblyCallInstr o) {
    visitReferencedElement(o);
  }

  public void visitComment(@NotNull WebAssemblyComment o) {
    visitPsiElement(o);
  }

  public void visitData(@NotNull WebAssemblyData o) {
    visitNamedReferencedElement(o);
  }

  public void visitElem(@NotNull WebAssemblyElem o) {
    visitNamedReferencedElement(o);
  }

  public void visitElemDropInstr(@NotNull WebAssemblyElemDropInstr o) {
    visitReferencedElement(o);
  }

  public void visitElemlist(@NotNull WebAssemblyElemlist o) {
    visitReferencedElement(o);
  }

  public void visitExport(@NotNull WebAssemblyExport o) {
    visitReferencedElement(o);
  }

  public void visitExportdesc(@NotNull WebAssemblyExportdesc o) {
    visitPsiElement(o);
  }

  public void visitFoldeinstr(@NotNull WebAssemblyFoldeinstr o) {
    visitPsiElement(o);
  }

  public void visitFunc(@NotNull WebAssemblyFunc o) {
    visitNamedElement(o);
  }

  public void visitFunctype(@NotNull WebAssemblyFunctype o) {
    visitPsiElement(o);
  }

  public void visitGlobal(@NotNull WebAssemblyGlobal o) {
    visitNamedElement(o);
  }

  public void visitGlobalInstr(@NotNull WebAssemblyGlobalInstr o) {
    visitReferencedElement(o);
  }

  public void visitGlobaltype(@NotNull WebAssemblyGlobaltype o) {
    visitPsiElement(o);
  }

  public void visitIdx(@NotNull WebAssemblyIdx o) {
    visitPsiElement(o);
  }

  public void visitImport(@NotNull WebAssemblyImport o) {
    visitNamedElement(o);
  }

  public void visitImportdesc(@NotNull WebAssemblyImportdesc o) {
    visitPsiElement(o);
  }

  public void visitInlineData(@NotNull WebAssemblyInlineData o) {
    visitPsiElement(o);
  }

  public void visitInlineElem(@NotNull WebAssemblyInlineElem o) {
    visitPsiElement(o);
  }

  public void visitInlineExport(@NotNull WebAssemblyInlineExport o) {
    visitPsiElement(o);
  }

  public void visitInlineImport(@NotNull WebAssemblyInlineImport o) {
    visitPsiElement(o);
  }

  public void visitInstr(@NotNull WebAssemblyInstr o) {
    visitPsiElement(o);
  }

  public void visitLexerTokens(@NotNull WebAssemblyLexerTokens o) {
    visitPsiElement(o);
  }

  public void visitLocal(@NotNull WebAssemblyLocal o) {
    visitNamedElement(o);
  }

  public void visitLocalInstr(@NotNull WebAssemblyLocalInstr o) {
    visitReferencedElement(o);
  }

  public void visitMem(@NotNull WebAssemblyMem o) {
    visitNamedElement(o);
  }

  public void visitMemoryIdxInstr(@NotNull WebAssemblyMemoryIdxInstr o) {
    visitReferencedElement(o);
  }

  public void visitMemtype(@NotNull WebAssemblyMemtype o) {
    visitPsiElement(o);
  }

  public void visitModule(@NotNull WebAssemblyModule o) {
    visitPsiElement(o);
  }

  public void visitModulefield(@NotNull WebAssemblyModulefield o) {
    visitPsiElement(o);
  }

  public void visitOffseteq(@NotNull WebAssemblyOffseteq o) {
    visitPsiElement(o);
  }

  public void visitParam(@NotNull WebAssemblyParam o) {
    visitNamedElement(o);
  }

  public void visitPlaininstr(@NotNull WebAssemblyPlaininstr o) {
    visitPsiElement(o);
  }

  public void visitRefFuncInstr(@NotNull WebAssemblyRefFuncInstr o) {
    visitReferencedElement(o);
  }

  public void visitResult(@NotNull WebAssemblyResult o) {
    visitPsiElement(o);
  }

  public void visitStart(@NotNull WebAssemblyStart o) {
    visitReferencedElement(o);
  }

  public void visitTable(@NotNull WebAssemblyTable o) {
    visitNamedElement(o);
  }

  public void visitTableCopyInstr(@NotNull WebAssemblyTableCopyInstr o) {
    visitReferencedElement(o);
  }

  public void visitTableIdxInstr(@NotNull WebAssemblyTableIdxInstr o) {
    visitReferencedElement(o);
  }

  public void visitTableInitInstr(@NotNull WebAssemblyTableInitInstr o) {
    visitReferencedElement(o);
  }

  public void visitTabletype(@NotNull WebAssemblyTabletype o) {
    visitPsiElement(o);
  }

  public void visitType(@NotNull WebAssemblyType o) {
    visitNamedElement(o);
  }

  public void visitTypeuse(@NotNull WebAssemblyTypeuse o) {
    visitPsiElement(o);
  }

  public void visitTypeuseTyperef(@NotNull WebAssemblyTypeuseTyperef o) {
    visitReferencedElement(o);
  }

  public void visitValtype(@NotNull WebAssemblyValtype o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull WebAssemblyNamedElement o) {
    visitPsiElement(o);
  }

  public void visitNamedReferencedElement(@NotNull WebAssemblyNamedReferencedElement o) {
    visitPsiElement(o);
  }

  public void visitReferencedElement(@NotNull WebAssemblyReferencedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
