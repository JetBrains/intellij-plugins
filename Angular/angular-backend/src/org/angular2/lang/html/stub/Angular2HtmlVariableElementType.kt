// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.PsiElement
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl

class Angular2HtmlVariableElementType(val kind: Angular2HtmlAttrVariable.Kind)
  : JSVariableElementType(kind.name + "_VARIABLE") {

  override fun toString(): String {
    return Angular2HtmlElementTypes.EXTERNAL_ID_PREFIX + debugName
  }

  override fun construct(node: ASTNode): PsiElement {
    return Angular2HtmlAttrVariableImpl(node)
  }
}