package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.references.JadeFileReferenceSet;
import org.jetbrains.annotations.NotNull;

public class JadeFilePathImpl extends CompositePsiElement {

  public JadeFilePathImpl() {
    super(JadeElementTypes.FILE_PATH);
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return new JadeFileReferenceSet(this).getAllReferences();
  }
}
