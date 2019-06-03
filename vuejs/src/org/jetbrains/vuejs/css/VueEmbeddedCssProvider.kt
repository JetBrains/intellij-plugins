// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.css

import com.intellij.lang.Language
import com.intellij.psi.css.EmbeddedCssProvider
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueEmbeddedCssProvider : EmbeddedCssProvider() {
  override fun enableEmbeddedCssFor(language: Language): Boolean = language is VueLanguage
}
