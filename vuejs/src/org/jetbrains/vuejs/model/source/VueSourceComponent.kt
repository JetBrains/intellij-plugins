// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.codeInsight.findExpressionInAttributeValue
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.VueIndexData
import org.jetbrains.vuejs.model.*

class VueSourceComponent(sourceElement: JSImplicitElement,
                         descriptor: VueSourceEntityDescriptor,
                         private val indexData: VueIndexData?)
  : VueSourceContainer(sourceElement, descriptor), VueRegularComponent {

  override val nameElement: PsiElement?
    get() = descriptor.initializer
      ?.let { it as? JSObjectLiteralExpression }
      ?.findProperty(NAME_PROP)
      ?.let { it.literalExpressionInitializer ?: it.value }

  override val defaultName: String?
    get() = indexData?.originalName
            ?: getTextIfLiteral(nameElement)

  override val slots: List<VueSlot>
    get() {
      val template = template ?: return emptyList()
      return CachedValuesManager.getCachedValue(template.source) {
        create(buildSlotsList(template), template.source)
      }
    }

  override fun createPointer(): Pointer<VueSourceComponent> {
    val descriptor = this.descriptor.createPointer()
    val source = (this.source as JSImplicitElement).createSmartPointer()
    val indexData = this.indexData
    return Pointer {
      val newDescriptor = descriptor.dereference() ?: return@Pointer null
      val newSource = source.dereference() ?: return@Pointer null
      VueSourceComponent(newSource, newDescriptor, indexData)
    }
  }

  override fun toString(): String {
    return "VueSourceComponent(${defaultName ?: descriptor.source.containingFile.name})"
  }

  companion object {

    private fun buildSlotsList(template: VueTemplate<*>): List<VueSlot> {
      val result = mutableListOf<VueSlot>()
      template.safeVisitTags { tag ->
        if (tag.name == SLOT_TAG_NAME) {
          PsiTreeUtil.getStubChildrenOfTypeAsList(tag, XmlAttribute::class.java)
            .asSequence()
            .filter { !it.value.isNullOrBlank() }
            .mapNotNull { attr ->
              VueAttributeNameParser.parse(attr.name, SLOT_TAG_NAME).let { info ->
                if (info.kind == VueAttributeKind.SLOT_NAME) {
                  attr.value?.let { name -> VueSourceSlot(name, tag) }
                }
                else if ((info as? VueDirectiveInfo)?.directiveKind == VueDirectiveKind.BIND
                         && info.arguments == SLOT_NAME_ATTRIBUTE) {
                  findExpressionInAttributeValue(attr, JSExpression::class.java)
                    ?.let { getSlotNameRegex(it) }
                    ?.let { VueSourceRegexSlot(it, tag) }
                }
                else null
              }
            }
            .firstOrNull()
            .let {
              result.add(it ?: VueSourceSlot(DEFAULT_SLOT_NAME, tag))
            }
        }
      }
      return result
    }

    private fun getSlotNameRegex(expr: JSExpression): String {
      if (expr is JSStringTemplateExpression) {
        var lastIndex = 1
        val exprText = expr.text
        val result = StringBuilder()
        for (range in expr.stringRanges) {
          if (lastIndex < range.startOffset) {
            result.append(".*")
          }
          result.append(Regex.escape(range.substring(exprText)))
          lastIndex = range.endOffset
        }
        if (lastIndex < exprText.length - 1) {
          result.append(".*")
        }
        return result.toString()
      }
      return ".*"
    }
  }

  private class VueSourceRegexSlot(override val pattern: String?,
                                   override val source: PsiElement) : VueSlot {
    override val name: String
      get() = "Dynamic slot"

  }

  private class VueSourceSlot(override val name: String,
                              override val source: PsiElement) : VueSlot
}
