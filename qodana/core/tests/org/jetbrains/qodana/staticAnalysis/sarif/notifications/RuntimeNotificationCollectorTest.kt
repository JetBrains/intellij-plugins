package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Exception
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.Notification
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import org.junit.jupiter.api.Test
import java.nio.file.Path


class RuntimeNotificationCollectorTest {

  private val subject = collector(maxRuntimeNotifications = 100)

  @Test
  fun `same failure reported for every inspected file is retained once`() {
    val files = (1..6).map { "packages/pkg-$it/package.json" }

    files.forEach { subject.add(toolError("VulnerableLibrariesLocal", it)) }

    assertThat(subject.notifications).hasSize(1)
    val notification = subject.notifications.single()
    assertThat(notification.message.text).isEqualTo("Inspection VulnerableLibrariesLocal failed")
    assertThat(notification.occurrences).isEqualTo(6)
    assertThat(notification.locationUris).containsExactlyElementsOf(files)
  }

  @Test
  fun `failures of different inspections are retained separately`() {
    listOf("VulnerableLibrariesLocal", "MaliciousLibrariesLocal").forEach { toolId ->
      (1..3).forEach { subject.add(toolError(toolId, "packages/pkg-$it/package.json")) }
    }

    assertThat(subject.notifications).hasSize(2)
    assertThat(subject.notifications.map { it.occurrences }).containsExactly(3, 3)
  }

  @Test
  fun `same inspection failing in different ways is retained separately`() {
    subject.add(toolError("Tool", "a.json", stackTrace = "java.lang.IllegalStateException: a\n\tat A.a(A.kt:1)"))
    subject.add(toolError("Tool", "b.json", stackTrace = "java.io.IOException: b\n\tat B.b(B.kt:1)"))

    assertThat(subject.notifications).hasSize(2)
    assertThat(subject.notifications.map { it.locationUris.single() }).containsExactly("a.json", "b.json")
  }

  /**
   * Files are inspected concurrently, so the same failure gets different frames below the throw site depending on
   * whether its file ran on the caller thread or was dispatched to a worker. That must not split it in two.
   */
  @Test
  fun `differing scheduling frames do not split one failure`() {
    val throwSite = "java.lang.IllegalStateException: boom\n\tat Tool.run(Tool.kt:1)"
    subject.add(toolError("Tool", "a.json", stackTrace = "$throwSite\n" +
                                                        "\tat com.intellij.concurrency.JobLauncherImpl\$1MyProcessQueueTask.call(JobLauncherImpl.java:395)\n" +
                                                        "\tat java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)"))
    subject.add(toolError("Tool", "b.json", stackTrace = "$throwSite\n" +
                                                        "\tat com.intellij.concurrency.JobLauncherImpl.processQueue(JobLauncherImpl.java:483)\n" +
                                                        "\tat com.intellij.codeInspection.ex.GlobalInspectionContextImpl.runTools(GlobalInspectionContextImpl.java:423)"))

    val notification = subject.notifications.single()
    assertThat(notification.occurrences).isEqualTo(2)
    assertThat(notification.locationUris).containsExactly("a.json", "b.json")
  }

  @Test
  fun `a failure reported once carries no occurrence count`() {
    subject.add(toolError("Tool", "a.json"))

    assertThat(subject.notifications.single().properties).doesNotContainKey(RuntimeNotificationCollector.OCCURRENCES_PROPERTY)
  }

  @Test
  fun `repeats of one failure do not exhaust the budget for other failures`() {
    val collector = collector(maxRuntimeNotifications = 2)

    repeat(5) { collector.add(toolError("Flooding", "file$it.json")) }
    collector.add(toolError("Other", "other.json"))

    assertThat(collector.notifications.map { it.message.text })
      .containsExactly("Inspection Flooding failed", "Inspection Other failed")
    assertThat(collector.notifications.first().occurrences).isEqualTo(5)
  }

  @Test
  fun `the budget still limits the number of distinct failures`() {
    val collector = collector(maxRuntimeNotifications = 1)

    collector.add(toolError("First", "a.json"))
    collector.add(toolError("Second", "b.json"))

    assertThat(collector.notifications.map { it.message.text }).containsExactly("Inspection First failed")
  }

  @Test
  fun `listed files are capped while the occurrence count stays exact`() {
    repeat(60) { subject.add(toolError("Tool", "file$it.json")) }

    val notification = subject.notifications.single()
    assertThat(notification.occurrences).isEqualTo(60)
    assertThat(notification.locationUris).hasSize(50)
  }

  @Test
  fun `sanity notifications are left untouched`() {
    repeat(2) {
      subject.add(Notification()
                    .withMessage(Message().withText("Analysis by sanity inspection was suspended."))
                    .withKind(SanityInspectionGroup.SANITY_FAILURE_NOTIFICATION))
    }

    assertThat(subject.notifications).hasSize(2)
  }


  private fun collector(maxRuntimeNotifications: Int) = RuntimeNotificationCollector().apply {
    // Relative location uris keep this independent of the host os, as they are never relativized against the project.
    initializeForRun(Path.of("project"), maxRuntimeNotifications)
  }

  private fun toolError(
    toolId: String,
    file: String,
    stackTrace: String = "java.lang.IllegalStateException: $toolId\n\tat Tool.run(Tool.kt:1)",
  ): Notification =
    Notification()
      .withMessage(Message().withText("Inspection $toolId failed"))
      .withLevel(Notification.Level.ERROR)
      .withLocations(setOf(Location().withPhysicalLocation(
        PhysicalLocation().withArtifactLocation(ArtifactLocation().withUri(file)))))
      .withException(Exception().withMessage(stackTrace))
      .withProperties(PropertyBag().apply { put("toolId", toolId) })
      .withKind(ToolErrorInspectListener.TOOL_ERROR_NOTIFICATION)

  private val Notification.occurrences: Int?
    get() = (properties?.get(RuntimeNotificationCollector.OCCURRENCES_PROPERTY) as? Number)?.toInt()

  private val Notification.locationUris: List<String>
    get() = locations.orEmpty().map { it.physicalLocation.artifactLocation.uri }
}
