package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.JavaScriptCodeContextType
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Irina.Chernushina on 10/26/2017.
 */
private val CONTEXT_TYPE = "VUE_COMPONENT_DESCRIPTOR"
class VueComponentDescriptorLiveTemplateContextType : TemplateContextType(CONTEXT_TYPE, "Vue component", VueBaseLiveTemplateContextType::class.java) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return VueBaseLiveTemplateContextType.evaluateContext(file, offset,
      scriptContextEvaluator = { it is JSExportAssignment || PsiTreeUtil.getParentOfType(it, JSExportAssignment::class.java) != null },
      notVueFileType = { JavaScriptCodeContextType.areJavaScriptTemplatesApplicable(it) &&
        PsiTreeUtil.getParentOfType(it, JSObjectLiteralExpression::class.java) != null })
  }
}