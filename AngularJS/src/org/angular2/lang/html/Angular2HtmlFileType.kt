// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.angularjs.AngularJSBundle

class Angular2HtmlFileType private constructor()
  : WebFrameworkHtmlFileType(Angular2HtmlLanguage.INSTANCE, "Angular2Html", "html") {
  override fun getDescription(): String {
    return AngularJSBundle.message("filetype.angular2html.description")
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2HtmlFileType()
  }
}