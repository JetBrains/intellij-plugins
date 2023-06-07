// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.imports

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.astro.editor.AstroComponentSourceEdit

class AstroAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {
  override fun prepareScopeToAdd(place: PsiElement, fromExternalModule: Boolean): PsiElement? {
    ApplicationManager.getApplication().assertReadAccessAllowed()

    return PsiTreeUtil.findFirstContext(place, false) {
      it is XmlTag && HtmlUtil.isScriptTag(it)
    }?.let {
      PsiTreeUtil.getChildOfType(it, JSEmbeddedContent::class.java)
    } ?: AstroComponentSourceEdit.getOrCreateFrontmatterScript(place.containingFile)
  }
}