// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.index.processScriptSetupTopLevelDeclarations
import org.jetbrains.vuejs.lang.html.psi.VueRefAttribute

class VueRefValueCompletionProvider : CompletionProvider<CompletionParameters>() {

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    if (parameters.position.parentOfType<XmlAttribute>() !is VueRefAttribute) return

    processScriptSetupTopLevelDeclarations(parameters.position) {
      if (it.name != null && it is JSVariable) {
        result.addElement(JSLookupUtilImpl.createLookupElement(it))
      }
      true
    }
  }
}