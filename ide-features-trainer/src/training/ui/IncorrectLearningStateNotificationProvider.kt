// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import training.commands.kotlin.TaskContext
import training.learn.LearnBundle

class IncorrectLearningStateNotificationProvider : EditorNotifications.Provider<EditorNotificationPanel>() {
  data class RestoreNotification(val type: TaskContext.RestoreProposal, val callback: () -> Unit)

  private val KEY: Key<EditorNotificationPanel> = Key.create("incorrect.learning.state.notification.provider")

  override fun getKey(): Key<EditorNotificationPanel> = KEY

  override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor, project: Project): EditorNotificationPanel? {
    val restoreNotification = file.getUserData(INCORRECT_LEARNING_STATE_NOTIFICATION) ?: return null
    return EditorNotificationPanel().apply {
      setText(when (restoreNotification.type) {
                TaskContext.RestoreProposal.Modification -> LearnBundle.message("learn.restore.notification.modification.message")
                TaskContext.RestoreProposal.Caret -> LearnBundle.message("learn.restore.notification.caret.message")
                else -> return null
              })
      createActionLabel(LearnBundle.message("learn.restore.notification.restore.link"), restoreNotification.callback)
      toolTipText = when (restoreNotification.type) {
        TaskContext.RestoreProposal.Modification -> LearnBundle.message("learn.restore.notification.modification.tooltip")
        TaskContext.RestoreProposal.Caret -> LearnBundle.message("learn.restore.notification.caret.tooltip")
        else -> null
      }
    }
  }

  companion object {
    val INCORRECT_LEARNING_STATE_NOTIFICATION = Key<RestoreNotification>("INCORRECT_LEARNING_STATE_NOTIFICATION")

    fun clearMessage(file: VirtualFile, project: Project) {
      file.putUserData(INCORRECT_LEARNING_STATE_NOTIFICATION, null)
      EditorNotifications.getInstance(project).updateNotifications(file)
    }
  }
}
