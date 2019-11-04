/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.editor.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareAction
import java.util.*

class BlockCaretAction(private val editor: Editor) : DumbAwareAction(LearnActions.LEARN_BLOCK_EDITOR_CARET_ACTION), LearnActions {

  override val actionId: String
    get() = LearnActions.LEARN_BLOCK_EDITOR_CARET_ACTION

  private val actionHandlers = mutableListOf<Runnable>()

  init {
    //collect all shortcuts for caret actions
    val superShortcut = mutableListOf<Shortcut>()
    val caretActionIds = mutableSetOf<String>()
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN_WITH_SELECTION)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT_WITH_SELECTION)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_UP_WITH_SELECTION)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT)
    caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT_WITH_SELECTION)

    //block clone caret
    caretActionIds.add(IdeActions.ACTION_EDITOR_CLONE_CARET_ABOVE)
    caretActionIds.add(IdeActions.ACTION_EDITOR_CLONE_CARET_BELOW)

    //tab
    caretActionIds.add(IdeActions.ACTION_EDITOR_TAB)
    caretActionIds.add(IdeActions.ACTION_EDITOR_EMACS_TAB)

    caretActionIds
      .map { ActionManager.getInstance().getAction(it).shortcutSet.shortcuts }
      .forEach { Collections.addAll(superShortcut, *it) }

    val shortcutSet = CustomShortcutSet(*superShortcut.toTypedArray())
    this.registerCustomShortcutSet(shortcutSet, editor.component)
  }

  override fun unregisterAction() {
    this.unregisterCustomShortcutSet(editor.component)
  }

  override fun actionPerformed(e: AnActionEvent) {
    actionHandlers.forEach { it.run() }
  }

  fun addActionHandler(runnable: Runnable) {
    actionHandlers.add(runnable)
  }

}
