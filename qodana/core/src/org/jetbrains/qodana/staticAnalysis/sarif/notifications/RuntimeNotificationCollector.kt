package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toNioPathOrNull
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Notification
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup.Companion.SANITY_FAILURE_NOTIFICATION
import org.jetbrains.qodana.staticAnalysis.sarif.OriginalUriBaseId
import org.jetbrains.qodana.staticAnalysis.sarif.SarifReportContributor
import org.jetbrains.qodana.staticAnalysis.sarif.qodanaKind
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
class RuntimeNotificationCollector {
  companion object {
    /**
     * How many times the same failure was reported, set on the single notification retained for it.
     * Absent when the failure was reported once.
     */
    const val OCCURRENCES_PROPERTY: String = "occurrences"
  }

  private val _notifications = ConcurrentLinkedDeque<Notification>()

  /** The notification retained per distinct failure, into which further reports of that same failure are merged. */
  private val retainedByFailure = ConcurrentHashMap<FailureKey, Notification>()
  private val capacity = AtomicInteger(0)

  @Volatile
  private var maxRuntimeNotifications = 0
  private lateinit var projectPath: Path

  val notifications: List<Notification> get() = _notifications.toList()

  fun initializeForRun(config: QodanaConfig) = initializeForRun(config.projectPath, config.maxRuntimeNotifications)

  @VisibleForTesting
  internal fun initializeForRun(projectPath: Path, maxRuntimeNotifications: Int) {
    this.projectPath = projectPath
    this.maxRuntimeNotifications = maxRuntimeNotifications
    _notifications.clear()
    retainedByFailure.clear()
    capacity.set(maxRuntimeNotifications)
  }

  fun add(notification: Notification) {
    if (notification.qodanaKind == SANITY_FAILURE_NOTIFICATION) {
      tryRelativizeArtifactLocation(notification)
      _notifications += notification
      return
    }

    var isNewFailure = false
    retainedByFailure.compute(notification.failureKey) { _, retained ->
      when {
        retained != null -> retained.also {
          tryRelativizeArtifactLocation(notification)
          it.merge(notification)
        }
        consumeCapacity() -> {
          isNewFailure = true
          notification.also(::tryRelativizeArtifactLocation)
        }
        else -> null
      }
    }
    if (isNewFailure) _notifications += notification
  }

  private fun consumeCapacity(): Boolean = capacity.getAndUpdate { i -> maxOf(i - 1, 0) } > 0

  /**
   * Identity of a failure: which inspection failed and how, regardless of which file was being inspected at the time.
   * The stack trace is compared in full.
   */
  private data class FailureKey(val toolId: String?, val stackTrace: String?)

  private val Notification.failureKey: FailureKey
    get() = FailureKey(
      toolId = properties?.get(ToolErrorInspectListener.TOOL_ID) as? String,
      stackTrace = exception?.message,
    )

  private val Notification.occurrences: Int
    get() = (properties?.get(OCCURRENCES_PROPERTY) as? Number)?.toInt() ?: 1

  /** Folds a repeated report of an already retained failure into it: bumps the count, collects the affected file. */
  private fun Notification.merge(duplicate: Notification) {
    val bag = properties ?: PropertyBag().also { withProperties(it) }
    bag[OCCURRENCES_PROPERTY] = occurrences + 1

    val known = locations.orEmpty()
    val additional = duplicate.locations.orEmpty().filterNotNull()
    if (additional.isEmpty() || known.size >= maxRuntimeNotifications) return

    val merged = LinkedHashSet<Location>(known)
    for (location in additional) {
      if (merged.size >= maxRuntimeNotifications) break
      merged += location
    }
    withLocations(merged)
  }

  private fun tryRelativizeArtifactLocation(notification: Notification) {
    notification.locations?.forEach { original ->
      val path = original?.physicalLocation?.artifactLocation?.uri?.toNioPathOrNull() ?: return@forEach
      val hasSrcRoot = original.physicalLocation.artifactLocation.uriBaseId == OriginalUriBaseId.SRCROOT.uriBaseId

      if (hasSrcRoot || !path.isAbsolute) return@forEach
      original.physicalLocation.artifactLocation
        .withUriBaseId(OriginalUriBaseId.SRCROOT.uriBaseId)
        .withUri(projectPath.relativize(path).toString())
    }
  }

  class NotificationReportContributor : SarifReportContributor {
    private companion object {
      val logger = logger<NotificationReportContributor>()
    }

    override fun contribute(run: Run, project: Project, config: QodanaConfig): Unit =
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
