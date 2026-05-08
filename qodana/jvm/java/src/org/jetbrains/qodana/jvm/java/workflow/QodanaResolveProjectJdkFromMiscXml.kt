package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import org.jetbrains.qodana.jvm.java.buildJavaSdkLookup
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ConsoleLog
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import org.jetbrains.qodana.staticAnalysis.workflow.appendBeforeOpen
import kotlin.time.Duration.Companion.minutes

class QodanaResolveProjectJdkFromMiscXml : QodanaWorkflowExtension {
  override suspend fun configureProjectOpening(config: QodanaConfig, openProjectTaskBuilder: OpenProjectTaskBuilder) {
    openProjectTaskBuilder.appendBeforeOpen { project ->
      resolveProjectJdkFromMiscXml(project)
      true
    }
  }
}

internal suspend fun resolveProjectJdkFromMiscXml(project: Project) {
  val rootManager = ProjectRootManager.getInstance(project)
  val requiredName = rootManager.projectSdkName ?: return
  val requiredType = rootManager.projectSdkTypeName
  if (requiredType != null && requiredType != JavaSdk.getInstance().name) {
    return
  }

  if (ProjectJdkTable.getInstance().findJdk(requiredName, JavaSdk.getInstance().name) != null) {
    return
  }

  ConsoleLog.info("Project JDK '$requiredName' specified in misc.xml not found in table, attempting to resolve...")

  val deferred = CompletableDeferred<Unit>()

  buildJavaSdkLookup(requiredName)
    .onSdkResolved { sdk ->
      try {
        if (sdk == null) {
          ConsoleLog.warn("Could not resolve JDK '$requiredName'; Project sync may fail")
          return@onSdkResolved
        }

        if (ProjectJdkTable.getInstance().findJdk(requiredName, JavaSdk.getInstance().name) != null) {
          ConsoleLog.info("JDK '$requiredName' resolution complete")
          return@onSdkResolved
        }

        val homePath = sdk.homePath ?: run {
          ConsoleLog.warn("Resolved JDK '${sdk.name}' has no home path; skipping resolution for '$requiredName'")
          return@onSdkResolved
        }

        val alias = JavaSdk.getInstance().createJdk(requiredName, homePath, false)
        ApplicationManager.getApplication().runWriteAction {
          if (ProjectJdkTable.getInstance().findJdk(requiredName) == null) {
            ProjectJdkTable.getInstance().addJdk(alias)
            ConsoleLog.info("JDK '$requiredName' resolution complete")
          }
        }
      }
      finally {
        deferred.complete(Unit)
      }
    }
    .executeLookup()

  withTimeout(5.minutes) { deferred.await() }
}
