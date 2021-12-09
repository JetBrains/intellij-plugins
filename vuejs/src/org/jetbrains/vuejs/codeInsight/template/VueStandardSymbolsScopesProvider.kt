// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import java.util.*
import java.util.function.Consumer

class VueStandardSymbolsScopesProvider : VueTemplateScopesProvider() {

  companion object {
    @NonNls
    val EVENT = "\$event"

    private fun resolveEventType(@Suppress("UNUSED_PARAMETER") attribute: XmlAttribute): JSType? {
      // TODO resolve event type
      return null
    }
  }

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    val result = SmartList<VueTemplateScope>()

    val attribute: PsiElement? = PsiTreeUtil.getParentOfType(
      hostElement ?: element, XmlAttribute::class.java, XmlTag::class.java)

    if (attribute is XmlAttribute) {
      val info = VueAttributeNameParser.parse(attribute.name, attribute.parent)
      if ((info as? VueAttributeNameParser.VueDirectiveInfo)?.directiveKind === VueAttributeNameParser.VueDirectiveKind.ON) {
        result.add(VueEventScope(attribute))
      }
    }
    return result
  }

  private class VueEventScope(event: XmlAttribute) : VueTemplateScope(null) {

    private val myEvent: XmlAttribute = event

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      consumer.accept(JSResolveResult(VueEventImplicitElement(myEvent)))
    }
  }

  private class VueEventImplicitElement(attribute: XmlAttribute) :
    JSLocalImplicitElementImpl(EVENT, resolveEventType(attribute),
                               attribute, JSImplicitElement.Type.Variable) {

    private val myDeclarations: Collection<PsiElement> = attribute.descriptor?.declarations ?: emptyList()

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || javaClass != other.javaClass) return false
      val element = other as VueEventImplicitElement
      return myName == element.myName
             && myDeclarations == element.myDeclarations
             && myProvider == element.myProvider
             && myKind == element.myKind
    }

    override fun hashCode(): Int {
      return Objects.hash(javaClass, myDeclarations, myName, myProvider, myKind)
    }
  }
}
