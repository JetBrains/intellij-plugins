// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.angularjs.AngularJSBundle
import org.jetbrains.annotations.Nls

class Angular2HtmlLanguage private constructor() : WebFrameworkHtmlDialect("Angular2Html") {
  override fun getDisplayName(): @Nls String {
    return AngularJSBundle.message("angular.html.template")
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2HtmlLanguage()
  }
}