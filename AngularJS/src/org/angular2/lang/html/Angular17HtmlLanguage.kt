// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angularjs.AngularJSBundle
import org.jetbrains.annotations.Nls

class Angular17HtmlLanguage private constructor() : WebFrameworkHtmlDialect(
  Angular2HtmlLanguage.INSTANCE, "Angular17Html") {
  override fun getDisplayName(): @Nls String {
    return AngularJSBundle.message("angular.html.template.17")
  }

  companion object {
    @JvmField
    val INSTANCE = Angular17HtmlLanguage()
  }
}