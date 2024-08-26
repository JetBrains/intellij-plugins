// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.parser

import com.intellij.psi.tree.IFileElementType
import org.angular2.lang.html.Angular181HtmlFileElementType
import org.angular2.lang.html.Angular2TemplateSyntax

open class Angular181HtmlParserDefinition : Angular2HtmlParserDefinitionBase() {

  override val syntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  override fun getFileNodeType(): IFileElementType {
    return Angular181HtmlFileElementType.INSTANCE
  }

}