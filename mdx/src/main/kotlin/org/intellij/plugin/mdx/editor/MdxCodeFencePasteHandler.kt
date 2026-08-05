package org.intellij.plugin.mdx.editor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

/**
 * Paste inside an MDX code fence, replayed in a sandbox of the fence language (see [MdxCodeFenceSandbox]);
 * falls back to the platform paste handler when the caret is not inside a fence body.
 *
 * The platform indents each pasted line through `adjustLineIndent`, which does nothing inside a fence because
 * [org.intellij.plugin.mdx.format.MdxFormattingModelBuilder] reports the fence as a leaf. The clipboard text
 * would therefore land verbatim, every line after the first at its own absolute column — in an indented fence
 * that loses the fence indent completely. Replaying the paste against a standalone file of the fence language
 * lets that language's own indenter run from column zero, and the sandbox re-indents the result to the fence base.
 *
 * Unlike [MdxEnterHandler] and [MdxCodeFenceTabHandler] this is not an
 * [com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler]: the platform paste handler opens its own
 * write actions (under a cancellable progress), so it must not be wrapped in one.
 */
internal class MdxCodeFencePasteHandler(private val baseHandler: EditorActionHandler?) : EditorActionHandler() {
  override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
    baseHandler?.isEnabled(editor, caret, dataContext) == true

  override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
    if (!MdxCodeFenceSandbox.replay(editor, "EditorPaste")) {
      baseHandler?.execute(editor, caret, dataContext)
    }
  }
}
