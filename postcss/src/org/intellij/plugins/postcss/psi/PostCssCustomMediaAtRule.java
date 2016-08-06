package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssOneLineStatement;
import com.intellij.psi.css.impl.CssTokenImpl;

public interface PostCssCustomMediaAtRule extends CssAtRule, CssOneLineStatement {

  CssTokenImpl getCustomMedia();

}