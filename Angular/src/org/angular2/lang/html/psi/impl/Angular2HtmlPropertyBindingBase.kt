// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl

internal abstract class Angular2HtmlPropertyBindingBase : Angular2HtmlBoundAttributeImpl {

  constructor(stub: Angular2HtmlBoundAttributeStubImpl, nodeType: IElementType)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  val propertyName: String
    get() = attributeInfo.name
  val bindingType: PropertyBindingType
    get() = (attributeInfo as PropertyBindingInfo).bindingType
  val binding: Angular2Binding?
    get() = PsiTreeUtil.findChildrenOfType(this, Angular2Binding::class.java).firstOrNull()
  val interpolations: Array<Angular2Interpolation>
    get() = PsiTreeUtil.findChildrenOfType(this, Angular2Interpolation::class.java)
      .toTypedArray<Angular2Interpolation>()
}