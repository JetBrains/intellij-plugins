// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.registry.Registry.Companion.get
import com.intellij.prettierjs.formatting.extendRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor

class PrettierPostFormatProcessor : PostFormatProcessor {
  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(psiFile: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (get("prettier.use.async.formatting.service").asBoolean()) {
      return rangeToReformat
    }

    if (isApplicable(psiFile)) {
      val extendedRange = extendRange(psiFile, rangeToReformat)
      return ReformatWithPrettierAction.processFileAsPostFormatProcessor(psiFile, extendedRange)
    }
    return rangeToReformat
  }

}

fun isApplicable(psiFile: PsiFile): Boolean {
  val file = psiFile.getVirtualFile()
  if (file == null) return false

  val project = psiFile.getProject()
  val configuration = PrettierConfiguration.getInstance(project)
  if (!configuration.isRunOnReformat) return false

  val fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file)
  if (fileEditor is TextEditor) {
    val template = TemplateManager.getInstance(psiFile.getProject()).getActiveTemplate(fileEditor.getEditor())
    if (template != null) return false
  }

  return isPrettierFormattingAllowedFor(project, file)
}