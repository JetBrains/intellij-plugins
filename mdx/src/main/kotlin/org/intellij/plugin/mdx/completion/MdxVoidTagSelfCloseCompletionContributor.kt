package org.intellij.plugin.mdx.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementDecorator
import com.intellij.xml.util.HtmlUtil
import org.intellij.plugin.mdx.lang.template.MdxFileViewProvider

/**
 * When a void HTML element (`input`, `br`, `img`, `hr`, …) is chosen from JSX tag-name completion,
 * insert a self-closing `<input/>` instead of leaving `<input` open, since JSX requires void elements
 * to be self-closed. Runs first and delegates to the real JSX completions via
 * [CompletionResultSet.runRemainingContributors], decorating void-tag results with the insert handler.
 */
internal class MdxVoidTagSelfCloseCompletionContributor : CompletionContributor() {
  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    if (parameters.originalFile.viewProvider !is MdxFileViewProvider) return
    result.runRemainingContributors(parameters) { completionResult ->
      val element = completionResult.lookupElement
      if (HtmlUtil.isSingleHtmlTag(element.lookupString, true)) {
        result.passResult(completionResult.withLookupElement(withSelfClose(element)))
      }
      else {
        result.passResult(completionResult)
      }
    }
  }

  private fun withSelfClose(element: LookupElement): LookupElement =
    LookupElementDecorator.withInsertHandler(element) { context, item ->
      item.delegate.handleInsert(context)

      val document = context.document
      val nameStart = context.startOffset
      // Self-close only an opening JSX tag name (`<input`) — not a closing tag (`</input`), a member
      // expression, or a plain JS identifier that happens to match a void-element name.
      if (nameStart <= 0 || document.charsSequence[nameStart - 1] != '<') return@withInsertHandler

      val caret = context.editor.caretModel.offset
      val chars = document.charsSequence
      val next = if (caret < chars.length) chars[caret] else ' '
      if (next == '/' || next == '>') return@withInsertHandler

      document.insertString(caret, "/>")
      context.editor.caretModel.moveToOffset(caret + 2)
      context.commitDocument()
    }
}
