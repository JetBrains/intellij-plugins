// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

object Angular181HtmlLanguage : WebFrameworkHtmlDialect(Angular17HtmlLanguage, "Angular181Html"), Angular2HtmlDialect {
  override fun getDisplayName(): @Nls String {
    return Angular2Bundle.message("angular.html.template.181")
  }

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  override val svgDialect: Boolean
    get() = false
}