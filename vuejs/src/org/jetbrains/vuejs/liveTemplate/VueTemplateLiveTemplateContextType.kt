package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

/**
 * @author Irina.Chernushina on 10/26/2017.
 */
private val CONTEXT_TYPE = "VUE_TEMPLATE"

class VueTemplateLiveTemplateContextType : TemplateContextType(CONTEXT_TYPE, "Vue template", VueBaseLiveTemplateContextType::class.java) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return VueBaseLiveTemplateContextType.evaluateContext(file, offset, forTagInsert = true)
  }
}