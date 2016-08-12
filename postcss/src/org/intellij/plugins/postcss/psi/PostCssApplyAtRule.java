package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.impl.CssTokenImpl;

public interface PostCssApplyAtRule extends PostCssOneLineAtRule {

  CssTokenImpl getCustomPropertiesSetIdentifier();
}