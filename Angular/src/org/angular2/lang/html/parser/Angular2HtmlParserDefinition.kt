// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.psi.tree.IFileElementType
import org.angular2.lang.html.Angular2HtmlFileElementType
import org.angular2.lang.html.Angular2TemplateSyntax

open class Angular2HtmlParserDefinition : Angular2HtmlParserDefinitionBase() {

  override val syntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2

  override fun getFileNodeType(): IFileElementType {
    return Angular2HtmlFileElementType.INSTANCE
  }
}