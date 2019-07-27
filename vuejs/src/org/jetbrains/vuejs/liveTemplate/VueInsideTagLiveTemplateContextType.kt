// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

private const val CONTEXT_TYPE = "VUE_INSIDE_TAG"

class VueInsideTagLiveTemplateContextType : TemplateContextType(CONTEXT_TYPE, "Vue template tag element",
                                                                VueBaseLiveTemplateContextType::class.java) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return VueBaseLiveTemplateContextType.evaluateContext(file, offset, forAttributeInsert = true)
  }
}
