package com.intellij.mdx.frontend.split

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.jetbrains.codeWithMe.model.SyntaxHighlighterModel
import com.jetbrains.rd.ide.model.DefaultHighlighterModel
import com.jetbrains.rd.ide.model.HighlighterModel
import com.jetbrains.rdclient.daemon.FrontendHighlighterSuppressionHandler
import org.intellij.plugin.mdx.lang.MdxFileType

internal class MdxFrontendHighlightingSuppressor : FrontendHighlighterSuppressionHandler {
  override fun shouldSuppress(highlighterModel: HighlighterModel, document: Document): Boolean {
    return when {
      FileDocumentManager.getInstance().getFile(document)?.fileType != MdxFileType -> false
      highlighterModel is SyntaxHighlighterModel -> true
      highlighterModel.properties.attributeId.startsWith(MDX_BACKEND_HIGHLIGHTER_ID) -> true
      highlighterModel.isBraceHighlighting() -> true
      highlighterModel.isParseError() -> true
      else -> false
    }
  }

  private fun HighlighterModel.isParseError(): Boolean {
    return (this as? DefaultHighlighterModel)?.info?.toolId?.endsWith("DefaultHighlightVisitor") == true
  }

  private fun HighlighterModel.isBraceHighlighting(): Boolean {
    return textAttributesKey?.externalName in BRACE_ATTRIBUTES_TO_SUPPRESS
  }
}

private val BRACE_ATTRIBUTES_TO_SUPPRESS = setOf(
  CodeInsightColors.MATCHED_BRACE_ATTRIBUTES.externalName,
  CodeInsightColors.UNMATCHED_BRACE_ATTRIBUTES.externalName,
)

private const val MDX_BACKEND_HIGHLIGHTER_ID = "IJ.MDX"
