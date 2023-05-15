// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angular2.lang.html.Angular2HtmlLanguage

class Angular2SvgLanguage private constructor()
  : WebFrameworkHtmlDialect(Angular2HtmlLanguage.INSTANCE, "Angular2Svg") {
  override fun getDisplayName(): String {
    return "Angular SVG template"
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2SvgLanguage()
  }
}