/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.editor.actions

interface LearnActions {

  val actionId: String

  fun unregisterAction()

  companion object {
    val LEARN_BLOCK_EDITOR_CARET_ACTION = "LearnBlockEditorCaretAction"
  }
}
