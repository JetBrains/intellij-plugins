package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.jetbrains.qodana.sarif.model.Invocation
import com.jetbrains.qodana.sarif.model.Notification
import com.jetbrains.qodana.sarif.model.Notification.Level
import com.jetbrains.qodana.sarif.model.Run
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import org.junit.jupiter.api.Test


class NotificationReportContributorTest {

  private val subject = RuntimeNotificationCollector.NotificationReportContributor()

  private val Run.notifications
    get() = invocations.orEmpty().flatMap { it.toolExecutionNotifications.orEmpty() }


  @Test
  fun `should not throw when run has no invocation`() {
    subject.contribute(Run(), collectorWithNotifs(Level.ERROR))
    subject.contribute(Run().withInvocations(emptyList()), collectorWithNotifs(Level.ERROR))
  }

  @Test
  fun `should not set notifications when no notifications collected`() {
    val actual = withRun { subject.contribute(it, collectorWithNotifs()) }

    assertThat(actual.notifications).isEmpty()
  }

  @Test
  fun `should include notifications`() {
    val actual = withRun { subject.contribute(it, collectorWithNotifs(Level.ERROR, Level.WARNING)) }

    assertThat(actual.notifications).hasSize(2)
    assertThat(actual.notifications.first().level).isEqualTo(Level.ERROR)
    assertThat(actual.notifications.last().level).isEqualTo(Level.WARNING)
  }


  private fun collectorWithNotifs(vararg notifLevels: Level) = RuntimeNotificationCollector().apply {
    notifLevels.map {
      // use sanity here to skip setting up runContext and configuration
      Notification().withLevel(it).withKind(SanityInspectionGroup.SANITY_FAILURE_NOTIFICATION)
    }.forEach(::add)
  }

  private fun withRun(f: (Run) -> Unit): Run {
    val run = Run().withInvocations(mutableListOf(Invocation()))
    f(run)
    return run
  }
}
