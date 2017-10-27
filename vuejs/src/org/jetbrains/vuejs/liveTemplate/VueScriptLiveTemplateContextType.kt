package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.javascript.JSStatementContextType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.liveTemplate.VueBaseLiveTemplateContextType.Companion.evaluateContext
import org.jetbrains.vuejs.liveTemplate.VueBaseLiveTemplateContextType.Companion.isTagEnd

/**
 * @author Irina.Chernushina on 10/26/2017.
 */
private val CONTEXT_TYPE = "VUE_SCRIPT"

class VueScriptLiveTemplateContextType : TemplateContextType(CONTEXT_TYPE, "Vue script tag contents", VueBaseLiveTemplateContextType::class.java) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return evaluateContext(file, offset,
                           scriptContextEvaluator = { isTagEnd(it) || it.parent is JSEmbeddedContent && it is JSExpressionStatement },
                           notVueFileType = { JSStatementContextType.isInContext(it) })
  }
}