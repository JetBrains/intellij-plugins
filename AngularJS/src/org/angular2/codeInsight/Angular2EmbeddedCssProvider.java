// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.Language;
import com.intellij.psi.css.EmbeddedCssProvider;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class Angular2EmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return language.isKindOf(Angular2HtmlLanguage.INSTANCE);
  }
}
