package org.jetbrains.qodana.jvm.gradle

import org.jetbrains.plugins.gradle.autolink.GradleUnlinkedProjectAware
import org.jetbrains.plugins.gradle.service.project.open.GradleProjectOpenProcessor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaExternalProjectsImporter

internal class QodanaGradleProjectsImporter :
  QodanaExternalProjectsImporter(GradleUnlinkedProjectAware(), GradleProjectOpenProcessor::class) {
}