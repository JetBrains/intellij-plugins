package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiFile
import com.jetbrains.qodana.sarif.model.*
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import java.nio.file.Path
import java.time.Instant

internal class ToolErrorInspectListener : InspectListener {
  companion object {
    const val TOOL_ERROR_NOTIFICATION = "toolError"

    private const val EXCEPTION_CLASS = "exceptionClass"
    private const val TOOL_ID = "toolId"
    private val ignored = listOf(CancellationException::class, ProcessCanceledException::class, IndexNotReadyException::class)

    private fun Throwable.isRelevant() = ignored.none { it.isInstance(this) }
  }

  override fun inspectionFailed(toolId: String,
                                throwable: Throwable,
                                file: PsiFile?,
                                project: Project) {
    if (!throwable.isRelevant()) return

    project.service<RuntimeNotificationCollector>()
      .add(buildNotification(toolId, throwable, file?.virtualFile?.toNioPathOrNull()))
  }

  private fun buildNotification(toolId: String, ex: Throwable, path: Path?): Notification {
    val location = path?.toString()
      ?.let(ArtifactLocation()::withUri)
      ?.let(PhysicalLocation()::withArtifactLocation)
      ?.let(Location()::withPhysicalLocation)
    return Notification()
      .withMessage(Message().withText("Inspection ${toolId} failed"))
      .withLocations(setOfNotNull(location))
      .withTimeUtc(Instant.now())
      .withLevel(Notification.Level.ERROR)
      .withProperties(PropertyBag().apply {
        put(EXCEPTION_CLASS, ex.javaClass.canonicalName)
        put(TOOL_ID, toolId)
      })
      .withKind(TOOL_ERROR_NOTIFICATION)
      .withException(Exception().withMessage(ex.stackTraceToString()))
  }

}
