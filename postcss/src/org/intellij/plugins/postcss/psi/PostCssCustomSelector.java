package org.intellij.plugins.postcss.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.css.CssDescriptorOwner;
import com.intellij.psi.css.CssNamedElement;
import com.intellij.psi.css.descriptor.CssElementDescriptor;

public interface PostCssCustomSelector extends CssNamedElement, CssDescriptorOwner<CssElementDescriptor>, PsiNameIdentifierOwner {
}