package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

/**
 * @author Irina.Chernushina on 10/26/2017.
 */
private val CONTEXT_TYPE = "VUE_INSIDE_TAG"
class VueInsideTagLiveTemplateContextType : TemplateContextType(CONTEXT_TYPE, "Vue template tag element", VueBaseLiveTemplateContextType::class.java) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return VueBaseLiveTemplateContextType.evaluateContext(file, offset, forAttributeInsert = true)
  }
}