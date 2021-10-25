// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.codeInsight.WebSymbolReferenceProvider
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.vuejs.model.DEPRECATED_SLOT_ATTRIBUTE
import org.jetbrains.vuejs.model.getAvailableSlots

class VueDeprecatedSlotAttributeReferenceProvider : WebSymbolReferenceProvider<XmlAttributeValue>() {

  override fun getSymbolNameOffset(psiElement: XmlAttributeValue): Int =
    psiElement.valueTextRange.startOffset - psiElement.startOffset

  override fun getSymbol(psiElement: XmlAttributeValue): WebSymbol? {
    if ((psiElement.parent as? XmlAttribute)?.name == DEPRECATED_SLOT_ATTRIBUTE) {
      val value = psiElement.value
      if (value.isNotEmpty()) {
        return (psiElement.parent as? XmlAttribute)
          ?.let { getAvailableSlots(it, false) }
          ?.find { it.name == value }
      }
    }
    return null
  }
}