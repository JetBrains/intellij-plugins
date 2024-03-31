// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

object Angular2HtmlLanguage : WebFrameworkHtmlDialect("Angular2Html"), Angular2HtmlDialect {
  override fun getDisplayName(): @Nls String {
    return Angular2Bundle.message("angular.html.template")
  }

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2

  override val svgDialect: Boolean
    get() = false
}