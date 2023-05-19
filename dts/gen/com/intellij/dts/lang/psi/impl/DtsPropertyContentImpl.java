// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsPropertyContentImpl extends com.intellij.extapi.psi.ASTWrapperPsiElement implements com.intellij.dts.lang.psi.DtsPropertyContent {

  public DtsPropertyContentImpl(@org.jetbrains.annotations.NotNull com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsByteArray> getByteArrayList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsByteArray.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsCellArray> getCellArrayList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsCellArray.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsPHandle> getPHandleList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsPHandle.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsPpMacro> getPpMacroList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsPpMacro.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsString> getStringList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsString.class);
  }

}
