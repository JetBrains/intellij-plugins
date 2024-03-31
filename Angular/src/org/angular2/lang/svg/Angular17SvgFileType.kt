// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.angular2.lang.Angular2Bundle

object Angular17SvgFileType
  : WebFrameworkHtmlFileType(Angular17SvgLanguage, "Angular17Svg", "svg") {
  override fun getDescription(): String {
    return Angular2Bundle.message("filetype.angular17svg.description")
  }
}