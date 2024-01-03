// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg.parser

import com.intellij.psi.tree.IFileElementType
import org.angular2.lang.html.parser.Angular17HtmlParserDefinition
import org.angular2.lang.svg.Angular17SvgFileElementType

class Angular17SvgParserDefinition : Angular17HtmlParserDefinition() {
  override fun getFileNodeType(): IFileElementType {
    return Angular17SvgFileElementType.INSTANCE
  }
}