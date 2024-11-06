package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.IdeaTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.settings.QodanaConfigChangeService

class QodanaConfigChangeServiceTest: QodanaPluginHeavyTestBase() {
  private val configChangeService get() = QodanaConfigChangeService.getInstance(project)

  fun `test default generated qodana yaml with jdk`() = runDispatchingOnUi {
    withJavaJdk("17") {
      @Language("YAML")
      val expected = """
        #-------------------------------------------------------------------------------#
        #               Qodana analysis is configured by qodana.yaml file               #
        #             https://www.jetbrains.com/help/qodana/qodana-yaml.html            #
        #-------------------------------------------------------------------------------#
        version: "1.0"
        
        #Specify inspection profile for code analysis
        profile:
          name: qodana.starter
        
        #Enable inspections
        #include:
        #  - name: <SomeEnabledInspectionId>
        
        #Disable inspections
        #exclude:
        #  - name: <SomeDisabledInspectionId>
        #    paths:
        #      - <path/where/not/run/inspection>
        
        projectJDK: "17" #(Applied in CI/CD pipeline)
        
        #Execute shell command before Qodana execution (Applied in CI/CD pipeline)
        #bootstrap: sh ./prepare-qodana.sh
        
        #Install IDE plugins before Qodana execution (Applied in CI/CD pipeline)
        #plugins:
        #  - id: <plugin.id> #(plugin id can be found at https://plugins.jetbrains.com)
        
        #Specify Qodana linter for analysis (Applied in CI/CD pipeline)
        linter: jetbrains/qodana-<linter>:LINTER_PLACEHOLDER
        
      """.trimIndent().updateVersion()
      val generated = configChangeService.createDefaultConfigContent()
      assertThat(generated).isEqualTo(expected)
    }
  }

  private suspend fun withJavaJdk(version: String?, action: suspend () -> Unit) {
    val projectJDKTable = ProjectJdkTable.getInstance()
    val projectRootManager = ProjectRootManager.getInstance(project)
    val newJdk = JavaSdk.getInstance().createJdk("mock-jdk", IdeaTestUtil.requireRealJdkHome(), false).apply {
      val sdkModificator = sdkModificator
      sdkModificator.versionString = version
      ApplicationManager.getApplication().runWriteAction {
        sdkModificator.commitChanges()
      }
    }
    try {
      writeAction {
        projectJDKTable.addJdk(newJdk, testRootDisposable)
        projectRootManager.projectSdk = newJdk
      }
      action.invoke()
    }
    finally {
      writeAction {
        projectJDKTable.removeJdk(newJdk)
      }
    }
  }

  private fun String.updateVersion(): String {
    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
    val ideMinorVersion = ApplicationInfo.getInstance().minorVersionMainPart
    return this.replace("LINTER_PLACEHOLDER","${ideMajorVersion}.${ideMinorVersion}")
  }
}