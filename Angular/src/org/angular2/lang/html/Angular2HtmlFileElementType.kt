// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.psi.tree.IFileElementType

class Angular2HtmlFileElementType private constructor()
  : IFileElementType("html.angular2", Angular2HtmlLanguage) {

  companion object {
    @JvmField
    val INSTANCE: IFileElementType = Angular2HtmlFileElementType()
  }
}