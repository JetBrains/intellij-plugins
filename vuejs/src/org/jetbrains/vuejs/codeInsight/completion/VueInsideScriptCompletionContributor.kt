// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.completion.JSLookupPriority
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.VueComponentDetailsProvider
import org.jetbrains.vuejs.codeInsight.VueFrameworkInsideScriptSpecificHandlersFactory

class VueInsideScriptCompletionContributor : CompletionContributor() {
  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    if (parameters.position.containingFile.fileType != VueFileType.INSTANCE) return
    if (!VueFrameworkInsideScriptSpecificHandlersFactory.isInsideScript(parameters.position)) return
    val vueVariants = VueComponentDetailsProvider.INSTANCE.getAttributesAndCreateLookupElements(parameters.position,
                                                                                                JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC)
    if (vueVariants != null) {
      result.addAllElements(vueVariants)
    }
  }

}