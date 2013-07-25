package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author vnikolaenko
 */
public interface CfmlNamedElement extends PsiNameIdentifierOwner {
  boolean isTrulyDeclaration();
}
