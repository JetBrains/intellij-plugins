package org.angularjs.codeInsight;

import com.intellij.lang.Language;
import com.intellij.psi.css.EmbeddedCssProvider;
import org.angularjs.html.Angular2HTMLLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSEmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return language.isKindOf(Angular2HTMLLanguage.INSTANCE);
  }
}
