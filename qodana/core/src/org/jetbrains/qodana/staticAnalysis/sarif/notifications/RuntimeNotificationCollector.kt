package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toNioPathOrNull
import com.jetbrains.qodana.sarif.model.Notification
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup.Companion.SANITY_FAILURE_NOTIFICATION
import org.jetbrains.qodana.staticAnalysis.sarif.SRCROOT_URI_BASE
import org.jetbrains.qodana.staticAnalysis.sarif.SarifReportContributor
import org.jetbrains.qodana.staticAnalysis.sarif.qodanaKind
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
internal class RuntimeNotificationCollector {
  private val _notifications = ConcurrentLinkedDeque<Notification>()
  private val capacity = AtomicInteger(0)
  private lateinit var projectPath: Path

  val notifications get() = _notifications.toList()

  fun configure(config: QodanaConfig) {
    projectPath = config.projectPath
    _notifications.clear()
    capacity.set(config.maxRuntimeNotifications)
  }

  fun add(notification: Notification) {
    if (notification.qodanaKind == SANITY_FAILURE_NOTIFICATION || capacity.getAndUpdate { i -> maxOf(i - 1, 0) } > 0) {
      tryRelativizeArtifactLocation(notification)
      _notifications += notification
    }
  }

  private fun tryRelativizeArtifactLocation(notification: Notification) {
    notification.locations?.forEach { original ->
      val path = original?.physicalLocation?.artifactLocation?.uri?.toNioPathOrNull() ?: return@forEach
      val hasSrcRoot = original.physicalLocation.artifactLocation.uriBaseId == SRCROOT_URI_BASE

      if (hasSrcRoot || !path.isAbsolute) return@forEach
      original.physicalLocation.artifactLocation
        .withUriBaseId(SRCROOT_URI_BASE)
        .withUri(projectPath.relativize(path).toString())
    }
  }

  class NotificationWorkflowExtension : QodanaWorkflowExtension {
    override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
      project.serviceAsync<RuntimeNotificationCollector>()
        .configure(config)
    }
  }

  class NotificationReportContributor : SarifReportContributor {
    private companion object {
      val logger = logger<NotificationReportContributor>()
    }

    override fun contribute(run: Run, project: Project, config: QodanaConfig) =
      contribute(run, project.service<RuntimeNotificationCollector>())

    @VisibleForTesting
    fun contribute(run: Run, collector: RuntimeNotificationCollector) {
      val invocation = run.invocations?.firstOrNull() ?: run {
        logger.warn("Cannot report tool errors, because invocation node is not present")
        return
      }

      val collected = collector.notifications.ifEmpty {
        logger.info("No tool errors to report to sarif file")
        return
      }

      val notifications = invocation.toolExecutionNotifications?.toMutableList() ?: mutableListOf()
      notifications.addAll(collected)
      invocation.toolExecutionNotifications = notifications
    }
  }

}
