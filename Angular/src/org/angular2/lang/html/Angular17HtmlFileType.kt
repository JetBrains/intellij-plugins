// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.angular2.lang.Angular2Bundle

object Angular17HtmlFileType : WebFrameworkHtmlFileType(Angular17HtmlLanguage, "Angular17Html", "html") {
  override fun getDescription(): String {
    return Angular2Bundle.message("filetype.angular17html.description")
  }
}