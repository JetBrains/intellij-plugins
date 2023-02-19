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
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.*
import org.jetbrains.vuejs.model.getAvailableSlotsCompletions
import java.util.*

// TODO move to web-types
class VueAttributeValueCompletionProvider : CompletionProvider<CompletionParameters>() {
  private val VUE_SCRIPT_LANGUAGE = setOf("js", "ts")
  private val VUE_STYLE_LANGUAGE = vueStyleLanguages()
  private val VUE_TEMPLATE_LANGUAGE = setOf("html", "pug")

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val xmlTag = PsiTreeUtil.getParentOfType(parameters.position, XmlTag::class.java, false)
    val xmlAttribute = PsiTreeUtil.getParentOfType(parameters.position, XmlAttribute::class.java,
                                                   false)
    if (xmlTag == null || xmlAttribute == null) return

    for (completion in listOfCompletions(xmlTag, xmlAttribute)) {
      result.addElement(LookupElementBuilder.create(completion))
    }
  }

  private fun listOfCompletions(xmlTag: XmlTag, xmlAttribute: XmlAttribute): Set<String> =
    when (VueAttributeNameParser.parse(xmlAttribute.name, xmlTag).kind) {
      SCRIPT_LANG -> VUE_SCRIPT_LANGUAGE
      STYLE_LANG -> VUE_STYLE_LANGUAGE
      TEMPLATE_LANG -> VUE_TEMPLATE_LANGUAGE
      SLOT -> getAvailableSlotsCompletions(xmlAttribute.parent, "", 0, false).map { it.name }.toSet()
      else -> emptySet()
    }

  private fun vueStyleLanguages(): Set<String> {
    val result = mutableListOf<String>()
    result.add("css")
    CSSLanguage.INSTANCE.dialects.forEach {
      if (it.displayName != "JQuery-CSS") {
        result.add(it.displayName.lowercase(Locale.US))
      }
    }
    return result.toSet()
  }
}

