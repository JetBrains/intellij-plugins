// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs

import com.intellij.javascript.web.css.CssClassInJSLiteralOrIdentifierReferenceProvider
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.attributes.isNgClassAttribute
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.PropertyBindingType

internal class Angular2CssReferencesContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    val cssClassInAttributePattern = psiElement(XmlAttribute::class.java)
      .and(FilterPattern(object : ElementFilter {
        override fun isClassAcceptable(hintClass: Class<*>): Boolean = true

        override fun isAcceptable(element: Any, context: PsiElement?): Boolean {
          val info: Angular2AttributeNameParser.AttributeInfo
          if (context is Angular2HtmlPropertyBinding || context is XmlAttribute && !context.language.`is`(Angular2Language)) {
            info = Angular2AttributeNameParser.parse((context as XmlAttribute).name, context.parent)
            return info is Angular2AttributeNameParser.PropertyBindingInfo && info.bindingType == PropertyBindingType.CLASS
          }
          return false
        }
      }))

    CssClassInJSLiteralOrIdentifierReferenceProvider.register(registrar, Angular2Language, Angular2Binding::class.java) {
      isNgClassAttribute(it)
    }
    registrar.registerReferenceProvider(cssClassInAttributePattern, Angular2CssClassInAttributeReferenceProvider())
  }
}
