// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refs

import com.intellij.htmltools.xml.util.HtmlReferenceProvider
import com.intellij.openapi.paths.PathReferenceManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class Angular2NgSrcReferencesProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
    PathReferenceManager.getInstance().createReferences(
      element, false, false, true, HtmlReferenceProvider.IMAGE_FILE_TYPES)
}