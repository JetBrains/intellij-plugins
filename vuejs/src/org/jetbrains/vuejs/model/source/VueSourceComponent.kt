// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.castSafelyTo
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

  override val defaultName: String?
    get() = indexData?.originalName
            ?: getTextIfLiteral(descriptor.initializer?.castSafelyTo<JSObjectLiteralExpression>()
                                  ?.findProperty(NAME_PROP)?.value)

  override val slots: List<VueSlot>
    get() {
      val template = template ?: return emptyList()
      return CachedValuesManager.getCachedValue(template.source) {
        create(buildSlotsList(template), template.source)
      }
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

    private fun getSlotNameRegex(expr: JSExpression): Regex {
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
        return Regex(result.toString())
      }
      return Regex(".*")
    }
  }

  private class VueSourceRegexSlot(override val pattern: Regex,
                                   override val source: PsiElement) : VueSlot {
    override val name: String
      get() = "Dynamic slot"

  }

  private class VueSourceSlot(override val name: String,
                              override val source: PsiElement) : VueSlot
}
