// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

object Angular17HtmlLanguage : WebFrameworkHtmlDialect(Angular2HtmlLanguage, "Angular17Html"), Angular2HtmlDialect {
  override fun getDisplayName(): @Nls String {
    return Angular2Bundle.message("angular.html.template.17")
  }

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  override val svgDialect: Boolean
    get() = false
}