// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.javascript.validation.JSEmptyTypeChecker
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabled
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable

class VolarTypeScriptAnnotationProvider : TypeScriptAnnotatorCheckerProvider() {
  override fun getTypeChecker(reporter: JSProblemReporter<*>): JSTypeChecker {
    return JSEmptyTypeChecker.getInstance()
  }

  override fun skipErrors(context: PsiElement?) = true

  override fun isAvailable(context: PsiElement): Boolean {
    val virtualFile = PsiUtilCore.getVirtualFile(context) ?: return false
    return isVolarEnabled(context.project, virtualFile)
  }
}