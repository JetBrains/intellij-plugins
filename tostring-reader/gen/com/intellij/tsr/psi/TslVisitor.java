// This is a generated file. Not intended for manual editing.
package com.intellij.tsr.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class TslVisitor extends PsiElementVisitor {

  public void visitBooleanLiteral(@NotNull TslBooleanLiteral o) {
    visitValue(o);
  }

  public void visitFallbackStringLiteral(@NotNull TslFallbackStringLiteral o) {
    visitValue(o);
  }

  public void visitList(@NotNull TslList o) {
    visitValue(o);
  }

  public void visitMap(@NotNull TslMap o) {
    visitPsiElement(o);
  }

  public void visitMapItem(@NotNull TslMapItem o) {
    visitPsiElement(o);
  }

  public void visitMapKey(@NotNull TslMapKey o) {
    visitPsiElement(o);
  }

  public void visitNullLiteral(@NotNull TslNullLiteral o) {
    visitValue(o);
  }

  public void visitNumberLiteral(@NotNull TslNumberLiteral o) {
    visitValue(o);
  }

  public void visitObject(@NotNull TslObject o) {
    visitValue(o);
  }

  public void visitObjectBrace(@NotNull TslObjectBrace o) {
    visitObject(o);
  }

  public void visitObjectBracket(@NotNull TslObjectBracket o) {
    visitObject(o);
  }

  public void visitObjectId(@NotNull TslObjectId o) {
    visitValue(o);
  }

  public void visitObjectName(@NotNull TslObjectName o) {
    visitPsiElement(o);
  }

  public void visitObjectParenth(@NotNull TslObjectParenth o) {
    visitObject(o);
  }

  public void visitObjectRef(@NotNull TslObjectRef o) {
    visitValue(o);
  }

  public void visitPropertyKey(@NotNull TslPropertyKey o) {
    visitPsiElement(o);
  }

  public void visitPropertyKeyValue(@NotNull TslPropertyKeyValue o) {
    visitPsiElement(o);
  }

  public void visitStringLiteral(@NotNull TslStringLiteral o) {
    visitValue(o);
  }

  public void visitValue(@NotNull TslValue o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
