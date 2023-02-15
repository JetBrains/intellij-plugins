// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import org.jetbrains.astro.editor.AstroComponentSourceEdit
import org.jetbrains.astro.lang.AstroFileImpl

class AstroAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {
  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    return AstroComponentSourceEdit.getOrCreateFrontmatterScript(place.containingFile)
  }

}