// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs

import com.intellij.javascript.web.css.CssClassInJSLiteralOrIdentifierReferenceProvider
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.PropertyBindingType

class Angular2CssReferencesContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    CssClassInJSLiteralOrIdentifierReferenceProvider.register(registrar, Angular2Language.INSTANCE, Angular2Binding::class.java) {
      Angular2AttributeValueProvider.isNgClassAttribute(it)
    }
    registrar.registerReferenceProvider(CSS_CLASS_PATTERN_IN_ATTRIBUTE, Angular2CssClassInAttributeReferenceProvider())
  }

  companion object {

    private val CSS_CLASS_PATTERN_IN_ATTRIBUTE = PlatformPatterns.psiElement(XmlAttribute::class.java)
      .and(FilterPattern(object : ElementFilter {
        override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
          val info: Angular2AttributeNameParser.AttributeInfo
          if (context is Angular2HtmlPropertyBinding || context is XmlAttribute && !context.language.`is`(Angular2Language.INSTANCE)) {
            info = Angular2AttributeNameParser.parse((context as XmlAttribute).name, context.parent)
            return info is Angular2AttributeNameParser.PropertyBindingInfo && info.bindingType == PropertyBindingType.CLASS
          }
          return false
        }

        override fun isClassAcceptable(hintClass: Class<*>): Boolean {
          return true
        }
      }))
  }
}
