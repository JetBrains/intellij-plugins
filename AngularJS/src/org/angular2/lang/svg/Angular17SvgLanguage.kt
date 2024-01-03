// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angular2.lang.html.Angular17HtmlLanguage
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angularjs.AngularJSBundle
import org.jetbrains.annotations.Nls

class Angular17SvgLanguage private constructor()
  : WebFrameworkHtmlDialect(Angular17HtmlLanguage.INSTANCE, "Angular17Svg"), Angular2HtmlDialect {
  override fun getDisplayName(): @Nls String {
    return AngularJSBundle.message("angular.svg.template.17")
  }

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  override val svgDialect: Boolean
    get() = true

  companion object {
    @JvmField
    val INSTANCE = Angular17SvgLanguage()
  }
}