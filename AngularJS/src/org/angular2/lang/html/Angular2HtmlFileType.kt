// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.angular2.lang.expr.Angular2Language
import org.angularjs.AngularJSBundle

class Angular2HtmlFileType private constructor()
  : WebFrameworkHtmlFileType(Angular2HtmlLanguage.INSTANCE, "Angular2Html", "html") {

  init {
    // Initialize Angular 2 language as well
    Angular2Language.INSTANCE
  }

  override fun getDescription(): String {
    return AngularJSBundle.message("filetype.angular2html.description")
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2HtmlFileType()
  }
}