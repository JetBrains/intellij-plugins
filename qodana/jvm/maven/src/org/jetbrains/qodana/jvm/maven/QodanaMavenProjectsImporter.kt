package org.jetbrains.qodana.jvm.maven

import org.jetbrains.idea.maven.project.MavenUnlinkedProjectAware
import org.jetbrains.idea.maven.wizards.MavenProjectOpenProcessor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaExternalProjectsImporter

internal class QodanaMavenProjectsImporter :
  QodanaExternalProjectsImporter(MavenUnlinkedProjectAware(), MavenProjectOpenProcessor::class) {
}
