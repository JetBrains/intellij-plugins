package org.intellij.plugins.postcss.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.css.CssDescriptorOwner;
import com.intellij.psi.css.CssNamedElement;

public interface PostCssCustomSelector extends CssNamedElement, CssDescriptorOwner, PsiNameIdentifierOwner {
}