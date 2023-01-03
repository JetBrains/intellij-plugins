// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.angularjs.AngularJSBundle

class Angular2SvgFileType private constructor()
  : WebFrameworkHtmlFileType(Angular2SvgLanguage.INSTANCE, "Angular2Svg", "svg") {
  override fun getDescription(): String {
    return AngularJSBundle.message("filetype.angular2svg.description")
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2SvgFileType()
  }
}