// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.vuex.codeInsight

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.codeInsight.TargetElementUtilBase
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.ecma6.TypeScriptIndexSignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.impl.JSPropertyNameReference
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.jetbrains.vuejs.libraries.vuex.VuexUtils

class VuexFrameworkSpecificHandler : JSFrameworkSpecificHandler {

  override fun adjustTargetElement(editor: Editor?, offset: Int, flags: Int, targetElement: PsiElement): PsiElement {
    if (true == (targetElement as? TypeScriptIndexSignature)
        ?.context
        ?.asSafely<TypeScriptObjectType>()
        ?.context
        ?.asSafely<TypeScriptInterface>()
        ?.name
        ?.let { it == "ActionTree" || it == "GetterTree" || it == "MutationTree" || it == "ModuleTree" }
        && VuexUtils.isVuexContext(targetElement)) {
      val project = editor?.project ?: return targetElement

      val document = editor.document
      val file = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return targetElement
      val adjusted = TargetElementUtilBase.adjustOffset(file, document, offset)
      return TargetElementUtil.findReference(editor, adjusted)
               ?.asSafely<JSPropertyNameReference>()
               ?.element ?: targetElement
    }
    return targetElement
  }
}