// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr

import com.intellij.javascript.web.codeInsight.html.attributes.WebSymbolAttributeDescriptor
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.JSExpectedTypeKind
import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.impl.Angular2BindingImpl
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.types.Angular2PropertyBindingType

class Angular2JSFrameworkSpecificHandler : JSFrameworkSpecificHandler {
  override fun findExpectedType(element: PsiElement, parent: PsiElement?, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is Angular2Binding && parent.expression == element) {
      val attribute = Angular2BindingImpl.getEnclosingAttribute(parent)
      val descriptor = attribute?.descriptor as? WebSymbolAttributeDescriptor ?: return null
      val info = Angular2AttributeNameParser.parse(descriptor.name)
      if (info.type == Angular2AttributeType.PROPERTY_BINDING || info.type == Angular2AttributeType.BANANA_BOX_BINDING) {
        return Angular2PropertyBindingType(attribute)
      }
      return null
    }
    if (parent is Angular2TemplateBinding && parent.expression == element) {
      return parent.keyJSType
    }
    return null
  }
}