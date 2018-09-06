// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.html.HtmlTagImpl
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import icons.VuejsIcons
import org.jetbrains.vuejs.codeInsight.completion.vuetify.VuetifyIcons
import org.jetbrains.vuejs.index.hasVue

/**
 * @author Artem.Gainanov on 5/9/2018.
 */
class VueAttributeDataCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS),
           VueEventAttrDataCompletionProvider())
  }
}

private class VueEventAttrDataCompletionProvider : CompletionProvider<CompletionParameters>() {

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    if (!hasVue(parameters.position.project)) return
    if ((parameters.position.parent.parent as HtmlTagImpl).name.contains("v-icon")) {
      VuetifyIcons.materialAndFontAwesome.forEach {
        result.addElement(LookupElementBuilder.create(it).withIcon(VuejsIcons.Vue))
      }
    }
  }

}

