// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.util.asSafely
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript

class AstroComponentSourceEdit(private val file: AstroFileImpl) {

  private val formatFixers = mutableListOf<FormatFixer>()

  companion object {
    fun getOrCreateFrontmatterScript(file: PsiFile): AstroFrontmatterScript? =
      file.asSafely<AstroFileImpl>()
        ?.let { AstroComponentSourceEdit(it) }
        ?.getOrCreateFrontmatterScript()
  }

  fun getOrCreateFrontmatterScript(): AstroFrontmatterScript {
    file.frontmatterScript()?.let { return it }

    val dummyContentRoot = PsiFileFactory.getInstance(file.project)
      .createFileFromText("dummy.astro", AstroFileType.INSTANCE, "---\n---\n")
      .astroContentRoot()!!

    val oldContentRoot = file.astroContentRoot()!!
    oldContentRoot.addRangeBefore(dummyContentRoot.firstChild, dummyContentRoot.lastChild.prevSibling, oldContentRoot.firstChild)
    return file.frontmatterScript()!!
  }

  fun insertAstroComponentImport(importName: String, componentFile: AstroFileImpl) {

    val info = ES6ImportPsiUtil.CreateImportExportInfo(importName, importName, ES6ImportPsiUtil.ImportExportType.DEFAULT,
                                                       ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT)

    val frontmatterScript = getOrCreateFrontmatterScript()

    ES6ImportPsiUtil.insertJSImport(frontmatterScript, info, componentFile)
  }

  private fun reformat(element: PsiElement) {
    formatFixers.add(FormatFixer.create(element, FormatFixer.Mode.Reformat))
  }

  fun reformatChanges() {
    JSRefactoringUtil.format(formatFixers)
  }

}