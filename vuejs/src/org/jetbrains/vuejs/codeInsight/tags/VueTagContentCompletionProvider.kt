// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.tags

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.impl.source.html.HtmlTagImpl
import com.intellij.util.ProcessingContext
import icons.VuejsIcons
import org.jetbrains.vuejs.codeInsight.completion.vuetify.VuetifyIcons
import org.jetbrains.vuejs.index.isVueContext

class VueTagContentCompletionProvider : CompletionProvider<CompletionParameters>() {

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    if (!isVueContext(parameters.position)) return
    if (parameters.position.parent.parent is HtmlTagImpl && (parameters.position.parent.parent as HtmlTagImpl).name.contains("v-icon")) {
      VuetifyIcons.materialAndFontAwesome.forEach {
        result.addElement(LookupElementBuilder.create(it).withIcon(VuejsIcons.Vue))
      }
    }
  }

}
