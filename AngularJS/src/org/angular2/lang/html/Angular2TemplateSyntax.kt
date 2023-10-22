// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect

enum class Angular2TemplateSyntax(val tokenizeExpansionForms: Boolean,
                                  val enableBlockSyntax: Boolean,
                                  val language: WebFrameworkHtmlDialect) {
  V_2(true, false, Angular2HtmlLanguage.INSTANCE),
  V_2_NO_EXPANSION_FORMS(false, false, Angular2HtmlLanguage.INSTANCE),
  V_17(true, true, Angular17HtmlLanguage.INSTANCE),
}