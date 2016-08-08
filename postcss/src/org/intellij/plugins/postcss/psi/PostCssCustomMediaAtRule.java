package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.CssAtRule;
import com.intellij.psi.css.CssOneLineStatement;

public interface PostCssCustomMediaAtRule extends CssAtRule, CssOneLineStatement {

  PostCssCustomMedia getCustomMedia();

}