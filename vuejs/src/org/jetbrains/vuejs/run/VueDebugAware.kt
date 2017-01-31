package org.jetbrains.vuejs.run

import com.intellij.javascript.JSDebuggerSupportUtils
import com.intellij.javascript.debugger.ExpressionInfoFactory
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.PsiElement
import com.intellij.xdebugger.evaluation.ExpressionInfo
import com.jetbrains.javascript.debugger.JavaScriptDebugAware
import org.jetbrains.vuejs.VueFileType

class VueDebugAware : JavaScriptDebugAware() {
  override fun getFileType(): LanguageFileType? {
    return VueFileType.INSTANCE
  }

  public override fun getEvaluationInfo(elementAtOffset: PsiElement,
                                        document: Document,
                                        expressionInfoFactory: ExpressionInfoFactory): ExpressionInfo? {
    return JSDebuggerSupportUtils.getEvaluationInfo(elementAtOffset, document, expressionInfoFactory)
  }

}