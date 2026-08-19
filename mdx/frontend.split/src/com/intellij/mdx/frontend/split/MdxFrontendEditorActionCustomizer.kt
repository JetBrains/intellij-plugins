package com.intellij.mdx.frontend.split

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ENTER
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MATCH_BRACE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_SELECT_WORD_AT_CARET
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_UNSELECT_WORD_AT_CARET
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.jetbrains.rd.ide.model.ActionCallStrategyKind
import com.jetbrains.rdclient.editorActions.cwm.ActionCallStrategyInfoSet
import com.jetbrains.rdclient.editorActions.cwm.BaseFrontendEditorActionHandlerStrategyCustomizer
import org.intellij.plugin.mdx.lang.MdxFileType

internal class MdxFrontendEditorActionCustomizer : BaseFrontendEditorActionHandlerStrategyCustomizer() {
  override fun getCallStrategyInfo(actionId: String,
                                   editor: Editor,
                                   caret: Caret?,
                                   dataContext: DataContext,
                                   fileType: FileType?): ActionCallStrategyInfoSet =
    ActionCallStrategyInfoSet(
      MdxFileType,
      ActionCallStrategyKind.FrontendFirst,
      ACTION_EDITOR_ENTER,
      ACTION_EDITOR_MATCH_BRACE,
      ACTION_EDITOR_SELECT_WORD_AT_CARET,
      ACTION_EDITOR_UNSELECT_WORD_AT_CARET,
    )
}
