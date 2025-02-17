package com.jetbrains.plugins.jade.psi.references;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.xml.TagNameReference;

public class JadeTagNameReference extends TagNameReference {
  public JadeTagNameReference(final ASTNode nameElement) {
    super(nameElement, true);
  }
}