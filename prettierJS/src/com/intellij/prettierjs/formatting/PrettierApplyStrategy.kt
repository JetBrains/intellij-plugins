// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.codeStyle.AbstractConvertLineSeparatorsAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil
import com.intellij.util.LineSeparator

fun interface PrettierFormattingApplier {
  fun apply(project: Project, psiFile: PsiFile): Boolean

  companion object {
    fun from(document: Document, psiFile: PsiFile, formattedContent: String): PrettierFormattingApplier {
      val context = createFormattingContext(document, formattedContent)
      val snapshot = CaretSnapshot.from(document, psiFile)
      val diff = computeFormattingDiff(context, snapshot)

      return PrettierFormattingApplier { project, psiFile ->
        if (diff.isEmpty()) return@PrettierFormattingApplier false

        DocumentUtil.executeInBulk(document) {
          applyTextDifferencesToDocument(context, diff)
        }
        val lineSeparatorUpdate = updateLineSeparatorIfNeeded(project, psiFile.virtualFile, context.detectedLineSeparator)

        snapshot?.restore(document, psiFile)

        lineSeparatorUpdate
      }
    }
  }
}

internal fun applyTextDifferencesToDocument(
  formattingContext: PrettierFormattingContext,
  diffFragments: List<PrettierDiffFragment>,
) {
  val document = formattingContext.document
  val formattedText = formattingContext.formattedContent

  // Apply from the end to keep offsets stable
  for (fragment in diffFragments.asReversed()) {
    val start1 = fragment.startOffset1
    val end1 = fragment.endOffset1
    val start2 = fragment.startOffset2
    val end2 = fragment.endOffset2

    val len1 = end1 - start1
    val len2 = end2 - start2

    when {
      len1 == 0 -> {
        val newText = formattedText.substring(start2, end2)
        val insertion = if (fragment.isCharFragment) newText else "$newText\n"
        document.insertString(start1, insertion)
      }
      len2 == 0 -> {
        document.deleteString(start1, end1)
      }
      else -> {
        val newText = formattedText.substring(start2, end2)
        document.replaceString(start1, end1, newText)
      }
    }
  }
}

internal fun updateLineSeparatorIfNeeded(
  project: Project,
  virtualFile: VirtualFile,
  newSeparator: LineSeparator?,
): Boolean {
  if (newSeparator != null && virtualFile.detectedLineSeparator != newSeparator.separatorString) {
    AbstractConvertLineSeparatorsAction.changeLineSeparators(project, virtualFile, newSeparator.separatorString)
    return true
  }
  return false
}
