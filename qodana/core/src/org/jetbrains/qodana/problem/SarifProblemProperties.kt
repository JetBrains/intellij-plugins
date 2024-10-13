package org.jetbrains.qodana.problem

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.actions.OpenProblemWithRevisionAction
import org.jetbrains.qodana.notifications.QodanaNotifications
import kotlin.math.max

/**
 * Actual properties associated with [SarifProblem] in the current project
 * (i.e. problem could be not present, or shifted by line/column)
 */
data class SarifProblemProperties(
  /** not present in project (for example: not valid file), should not be displayed in UI */
  val isPresent: Boolean,

  /** missing in current file due to some changes, should be displayed in UI */
  val isMissing: Boolean,

  /** is fixed (local inspection run is enabled, so it is either has duplicate, or not found by local run -> also fixed)*/
  val isFixed: Boolean,

  val line: Int,
  val column: Int
)

data class SarifProblemWithProperties(
  val problem: SarifProblem,
  val properties: SarifProblemProperties
)

class SarifProblemWithPropertiesAndFile(
  val problem: SarifProblem,
  val properties: SarifProblemProperties,
  val project: Project,
  val file: VirtualFile? = null
)

fun SarifProblemWithProperties.openFileDescriptor(project: Project): OpenFileDescriptor? {
  val line = properties.line
  val column = properties.column
  val virtualFile = problem.getVirtualFile(project) ?: return null

  if (line < 0 || !properties.isPresent) {
    return OpenFileDescriptor(project, virtualFile, -1)
  }

  val lineStartDescriptor = OpenFileDescriptor(project, virtualFile, line, 0)
  val nextLineStartDescriptor = OpenFileDescriptor(project, virtualFile, line + 1, 0)

  val offset = lineStartDescriptor.offset + max(0, column)
  val problemLocationDescriptor = OpenFileDescriptor(project, virtualFile, offset)

  return if (problemLocationDescriptor >= nextLineStartDescriptor) lineStartDescriptor else problemLocationDescriptor
}

fun SarifProblemWithProperties.navigatable(project: Project): Navigatable {
  val openFileDescriptor = if (!properties.isMissing) openFileDescriptor(project) else null

  val missingProblemNavigatable = object : Navigatable {
    override fun navigate(requestFocus: Boolean) {
      val description = problem.buildDescription(useQodanaPrefix = false, showSeverity = false)
      val notificationContent = QodanaBundle.message("notification.content.problem.not.present.in.file", description, problem.relativeNioFile.fileName)

      val notification = QodanaNotifications.ProblemsTab.notification(
        QodanaBundle.message("notification.title.cannot.navigate.to.problem"),
        notificationContent,
        NotificationType.WARNING
      )
      val openProblemWithRevisionAction = OpenProblemWithRevisionAction(problem)
      if (openProblemWithRevisionAction.canBePerformed(project)) {
        notification.addAction(openProblemWithRevisionAction)
      }

      notification.notify(project)
    }

    override fun canNavigate(): Boolean = true

    override fun canNavigateToSource(): Boolean = true
  }

  return openFileDescriptor ?: missingProblemNavigatable
}