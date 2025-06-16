// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr

import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.highlighting.JSHighlightDescriptor
import com.intellij.lang.javascript.psi.JSExpectedTypeKind
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import com.intellij.util.applyIf
import org.angular2.lang.Angular2HighlightDescriptor
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.types.Angular2PropertyBindingType
import org.angular2.lang.types.Angular2TemplateBindingType
import org.angular2.signals.Angular2SignalUtils

class Angular2JSFrameworkSpecificHandler : JSFrameworkSpecificHandler {
  override fun findExpectedType(element: PsiElement, parent: PsiElement?, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is Angular2Binding && parent.expression == element) {
      val attribute = parent.enclosingAttribute
      val descriptor = attribute?.descriptor as? HtmlAttributeSymbolDescriptor ?: return null
      val info = Angular2AttributeNameParser.parse(descriptor.name)
      if (info.type == Angular2AttributeType.PROPERTY_BINDING || info.type == Angular2AttributeType.BANANA_BOX_BINDING) {
        return Angular2PropertyBindingType(attribute, expectedTypeKind).substitute(element)
          .applyIf(info.type == Angular2AttributeType.BANANA_BOX_BINDING) {
            Angular2SignalUtils.addWritableSignal(element, this)
          }
      }
      return null
    }
    if (parent is Angular2TemplateBinding && parent.expression == element) {
      return (parent.keyJSType as? Angular2TemplateBindingType)?.copyWithExpectedKind(expectedTypeKind)?.substitute(element)
    }
    if (parent is Angular2Interpolation && parent.expression == element) {
      return if (expectedTypeKind != JSExpectedTypeKind.TYPE_CHECKING)
        JSNamedTypeFactory.createStringPrimitiveType(JSTypeSource.EMPTY)
      else null
    }
    return null
  }

  override fun buildHighlightForElement(resolve: PsiElement, place: PsiElement): JSHighlightDescriptor? =
    Angular2HighlightDescriptor.getFor(resolve, place)
}