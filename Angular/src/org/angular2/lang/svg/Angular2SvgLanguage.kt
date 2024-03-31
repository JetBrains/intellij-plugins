// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.Angular2TemplateSyntax
import org.jetbrains.annotations.Nls

object Angular2SvgLanguage : WebFrameworkHtmlDialect(Angular2HtmlLanguage, "Angular2Svg"), Angular2HtmlDialect {
  override fun getDisplayName(): @Nls String {
    return Angular2Bundle.message("angular.svg.template")
  }

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2

  override val svgDialect: Boolean
    get() = true
}