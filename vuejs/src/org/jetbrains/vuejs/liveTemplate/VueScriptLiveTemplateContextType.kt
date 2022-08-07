// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.javascript.JSStatementContextType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.liveTemplate.VueBaseLiveTemplateContextType.Companion.evaluateContext
import org.jetbrains.vuejs.liveTemplate.VueBaseLiveTemplateContextType.Companion.isTagEnd

class VueScriptLiveTemplateContextType : TemplateContextType(VueBundle.message("vue.live.template.context.script.tag")) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return evaluateContext(file, offset,
                           scriptContextEvaluator = { isTagEnd(it) || it.parent is JSEmbeddedContent && it is JSExpressionStatement },
                           notVueFileType = { JSStatementContextType.isInContext(it) })
  }
}
