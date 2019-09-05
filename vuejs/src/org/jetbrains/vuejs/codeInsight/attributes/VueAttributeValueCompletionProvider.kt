// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.css.CSSLanguage
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.PLAIN
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.SLOT
import org.jetbrains.vuejs.model.getAvailableSlots
import java.util.*

class VueAttributeValueCompletionProvider : CompletionProvider<CompletionParameters>() {
  private val VUE_SCRIPT_LANGUAGE = ContainerUtil.immutableSet("js", "ts")
  private val VUE_STYLE_LANGUAGE = vueStyleLanguages()
  private val VUE_TEMPLATE_LANGUAGE = ContainerUtil.immutableSet("html", "pug")

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val xmlTag = PsiTreeUtil.getParentOfType(parameters.position, XmlTag::class.java, false)
    val xmlAttribute = PsiTreeUtil.getParentOfType(parameters.position, XmlAttribute::class.java,
                                                   false)
    if (xmlTag == null || xmlAttribute == null) return

    for (completion in listOfCompletions(xmlTag, xmlAttribute)) {
      result.addElement(LookupElementBuilder.create(completion))
    }
  }

  private fun listOfCompletions(xmlTag: XmlTag, xmlAttribute: XmlAttribute): Set<String> {
    val attrInfo = VueAttributeNameParser.parse(xmlAttribute.name, xmlTag)
    when (attrInfo.kind) {
      PLAIN ->
        if (xmlAttribute.name == "lang") {
          when (xmlTag.name) {
            "script" -> return VUE_SCRIPT_LANGUAGE
            "style" -> return VUE_STYLE_LANGUAGE
            "template" -> return VUE_TEMPLATE_LANGUAGE
          }
        }
      SLOT -> return getAvailableSlots(xmlAttribute, false).map { it.name }.toSet()
      else -> {
      }
    }
    return emptySet()
  }

  private fun vueStyleLanguages(): Set<String> {
    val result = mutableListOf<String>()
    result.add("css")
    CSSLanguage.INSTANCE.dialects.forEach {
      if (it.displayName != "JQuery-CSS") {
        result.add(it.displayName.toLowerCase(Locale.US))
      }
    }
    return result.toSet()
  }
}

