// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.refactoring

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.typescript.refactoring.TypeScriptRefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.RefactoringActionHandler

class AstroRefactoringSupportProvider : TypeScriptRefactoringSupportProvider() {

  override fun isAvailable(context: PsiElement): Boolean {
    val container = PsiTreeUtil.getNonStrictParentOfType(context, PsiFile::class.java, XmlTag::class.java, JSEmbeddedContent::class.java)
    return container != null
           && container !is XmlTag
           && DialectDetector.isTypeScript(container)
  }

  override fun getExtractMethodHandler(): RefactoringActionHandler =
    AstroExtractFunctionHandler()

}