package org.intellij.plugin.mdx.editor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

/**
 * Tab inside an MDX code fence, replayed in a sandbox of the fence language (see [MdxCodeFenceSandbox]);
 * falls back to the platform Tab handler when the caret is not inside a fence body.
 */
internal class MdxCodeFenceTabHandler(private val baseHandler: EditorActionHandler?) : EditorWriteActionHandler() {
  override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
    baseHandler?.isEnabled(editor, caret, dataContext) == true

  override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext?) {
    if (!MdxCodeFenceSandbox.replay(editor, "EditorTab")) {
      baseHandler?.execute(editor, caret, dataContext)
    }
  }
}
