// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.openapi.util.Ref
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor
import org.angular2.lang.html.psi.Angular2HtmlReference
import org.angularjs.codeInsight.refs.AngularJSReferenceBase
import java.util.function.BiPredicate

class Angular2ViewChildReferencesProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    return arrayOf(Angular2ViewChildReference(element as JSLiteralExpression))
  }


  class Angular2ViewChildReference(element: JSLiteralExpression)
    : AngularJSReferenceBase<JSLiteralExpression>(element, ElementManipulators.getValueTextRange(element)) {

    private val template: PsiFile?
      get() {
        val cls = PsiTreeUtil.getContextOfType(element, TypeScriptClass::class.java)
        if (cls != null) {
          val component = Angular2EntitiesProvider.getComponent(cls)
          if (component != null) {
            return component.templateFile
          }
        }
        return null
      }

    override fun isSoft(): Boolean {
      return false
    }

    override fun resolveInner(): PsiElement? {
      val result = Ref<PsiElement>()
      val refName = myElement.stringValue
      if (refName != null) {
        processVariables { name, psi ->
          if (refName == name) {
            result.set(psi)
            return@processVariables false
          }
          true
        }
      }
      return result.get()
    }

    override fun getVariants(): Array<Any> {
      val result = ArrayList<PsiElement>()
      val names = HashSet<String>()
      processVariables { name, psi ->
        if (names.add(name)) result.add(psi)
        true
      }
      return result.toTypedArray()
    }

    private fun processVariables(processor: BiPredicate<in String, in PsiElement>) {
      val template = template
      if (template != null) {
        if (template.language.isKindOf(Angular2HtmlLanguage.INSTANCE)) {
          template.accept(object : Angular2HtmlRecursiveElementWalkingVisitor() {
            override fun visitReference(reference: Angular2HtmlReference) {
              val refVar = reference.variable
              if (!processor.test(refVar?.name ?: return, refVar)) {
                stopWalking()
              }
            }
          })
        }
        else {
          template.accept(object : XmlRecursiveElementWalkingVisitor() {
            override fun visitXmlAttribute(attribute: XmlAttribute) {
              val info = Angular2AttributeNameParser.parse(attribute.name, attribute.parent)
              if (info.type == Angular2AttributeType.REFERENCE) {
                val refVar = JSLocalImplicitElementImpl(info.name, "*", attribute)
                if (!processor.test(info.name, refVar)) {
                  stopWalking()
                }
              }
            }
          })
        }
      }
    }
  }
}
