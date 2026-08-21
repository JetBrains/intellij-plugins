package com.intellij.mdx.backend.js

import com.intellij.lang.ImportOptimizer
import com.intellij.lang.LanguageImportStatements
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.parse.MdxEsmScanner

internal class MdxJSImportOptimizer : ImportOptimizer {
  override fun supports(file: PsiFile): Boolean {
    return file.language == MdxJSLanguage.INSTANCE
  }

  override fun processFile(file: PsiFile): Runnable {
    val delegate = findJavaScriptOptimizer(file)?.processFile(file)
    return Runnable {
      delegate?.run()
      repairAdjacentEsmStatements(file)
    }
  }

  private fun findJavaScriptOptimizer(file: PsiFile): ImportOptimizer? {
    return LanguageImportStatements.INSTANCE
      .allForLanguageOrAny(JavascriptLanguage)
      .firstOrNull { it !is MdxJSImportOptimizer && it.supports(file) }
  }

  private fun repairAdjacentEsmStatements(file: PsiFile) {
    val manager = PsiDocumentManager.getInstance(file.project)
    val document = manager.getDocument(file) ?: return
    val offsets = collectMissingSeparatorOffsets(document.charsSequence.toString())
    for (offset in offsets.asReversed()) {
      document.insertString(offset, "\n")
    }
    if (offsets.isNotEmpty()) {
      manager.commitDocument(document)
    }
  }

  private fun collectMissingSeparatorOffsets(text: CharSequence): List<Int> {
    val offsets = mutableListOf<Int>()
    var offset = 0
    while (offset < text.length) {
      if (MdxEsmScanner.isLineStart(text, offset)) {
        val blockEnd = MdxEsmScanner.scanBlock(text, offset)?.range?.last ?: text.length
        offsets.addAll(MdxEsmScanner.findMissingStatementSeparators(text, offset, blockEnd))
        offset = blockEnd.coerceAtLeast(offset + 1)
      }
      else {
        offset++
      }
    }
    return offsets
  }
}
