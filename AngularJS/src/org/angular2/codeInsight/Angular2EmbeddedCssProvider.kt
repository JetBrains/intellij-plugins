// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.Language
import com.intellij.psi.css.EmbeddedCssProvider
import org.angular2.lang.html.Angular2HtmlLanguage

/**
 * @author Dennis.Ushakov
 */
class Angular2EmbeddedCssProvider : EmbeddedCssProvider() {
  override fun enableEmbeddedCssFor(language: Language): Boolean {
    return language.isKindOf(Angular2HtmlLanguage.INSTANCE)
  }
}
