package org.angularjs.codeInsight;

import com.intellij.lang.Language;
import com.intellij.psi.css.EmbeddedCssProvider;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSEmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return language.isKindOf(Angular2HtmlLanguage.INSTANCE);
  }
}
