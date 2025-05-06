package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.Notification
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup.Companion.SANITY_FAILURE_NOTIFICATION
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import java.time.Instant

fun addSanityNotification(project: Project, message: String) {
  project.service<RuntimeNotificationCollector>().add(createSanityNotification(message))
}

fun createSanityNotification(message: String): Notification =
  Notification()
    .withLevel(Notification.Level.ERROR)
    .withTimeUtc(Instant.now())
    .withMessage(Message().withText(message))
    .withKind(SANITY_FAILURE_NOTIFICATION)