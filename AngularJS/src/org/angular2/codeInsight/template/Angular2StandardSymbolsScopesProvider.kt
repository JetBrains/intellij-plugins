// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitParameterStructure
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.types.Angular2EventType
import org.jetbrains.annotations.NonNls
import java.util.*
import java.util.function.Consumer

class Angular2StandardSymbolsScopesProvider : Angular2TemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<Angular2TemplateScope> {
    val result = SmartList<Angular2TemplateScope>(Angular2AnyScope(element.containingFile))
    if (hostElement != null) {
      var attribute = hostElement
      while (attribute != null
             && attribute !is XmlAttribute
             && attribute !is XmlTag) {
        attribute = attribute.parent
      }
      if (attribute is XmlAttribute) {
        val info = Angular2AttributeNameParser.parse(
          attribute.name, attribute.parent)
        if (info.type == Angular2AttributeType.EVENT) {
          result.add(Angular2EventScope(attribute))
        }
      }
    }
    else if (element is JSElement || element.parent is JSElement) {
      var attribute: PsiElement? = element
      while (attribute != null
             && attribute !is XmlAttribute
             && attribute !is XmlTag) {
        attribute = attribute.parent
      }
      if (attribute is Angular2HtmlEvent) {
        result.add(Angular2EventScope((attribute as Angular2HtmlEvent?)!!))
      }
    }
    return result
  }

  override fun isImplicitReferenceExpression(expression: JSReferenceExpression): Boolean {
    val value = expression.text
    return `$ANY` == value || `$EVENT` == value
  }

  private class Angular2AnyScope(context: PsiElement) : Angular2TemplateScope(null) {

    private val `$any`: JSImplicitElement

    init {
      `$any` = JSImplicitElementImpl.Builder(`$ANY`, context)
        .setJSType(JSAnyType.get(context, true))
        .setParameters(listOf(JSImplicitParameterStructure("arg", "*", false, false, true)))
        .setType(JSImplicitElement.Type.Function)
        .toImplicitElement()
    }

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      consumer.accept(JSResolveResult(`$any`))
    }
  }

  private class Angular2EventScope : Angular2TemplateScope {

    private val myEvent: XmlAttribute

    constructor(event: Angular2HtmlEvent) : super(null) {
      myEvent = event
    }

    constructor(event: XmlAttribute) : super(null) {
      myEvent = event
    }

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      consumer.accept(JSResolveResult(Angular2EventImplicitElement(myEvent)))
    }
  }

  private class Angular2EventImplicitElement(attribute: XmlAttribute)
    : JSLocalImplicitElementImpl(`$EVENT`, Angular2EventType(attribute), attribute, JSImplicitElement.Type.Variable) {

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || javaClass != other.javaClass) return false
      val element = other as Angular2EventImplicitElement?
      if (myName != element!!.myName) return false
      if (myProvider != element.myProvider) return false
      return myKind == element.myKind
    }

    override fun hashCode(): Int {
      return Objects.hash(javaClass, myName, myProvider, myKind)
    }
  }

  companion object {

    @NonNls
    const val `$ANY` = "\$any"

    @NonNls
    const val `$EVENT` = "\$event"
  }
}
