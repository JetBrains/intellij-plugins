package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.testFramework.HeavyPlatformTestCase
import kotlinx.coroutines.CoroutineScope
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.PreconfiguredRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.QodanaScript
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
abstract class QodanaConfigurationIntegrationBaseTest : HeavyPlatformTestCase() {
  protected data class ScriptEnv(
    val script: QodanaScript,
    val runContext: QodanaRunContext,
    val config: QodanaConfig,
    val app: QodanaInspectionApplication,
  )

  protected suspend fun buildScript(
    cliArgs: List<String>,
    testProject: Project,
    projectFiles: List<Pair<String, String>>,
    scope: CoroutineScope
  ): ScriptEnv {
    for ((path, content) in projectFiles) createTempFile("${testProject.basePath}/$path", content)
    val app = QodanaInspectionApplicationFactory().buildApplication(cliArgs)!!
    val loadedProfile = LoadedProfile.load(app.config, testProject, QodanaMessageReporter.DEFAULT)
    val contextFactory = PreconfiguredRunContextFactory(app.config, QodanaMessageReporter.DEFAULT, testProject, loadedProfile, scope)
    val runner = app.constructQodanaRunner(contextFactory)

    val script = runner.script

    return ScriptEnv(script, contextFactory.openRunContext(), app.config, app)
  }

  @Language("XML")
  protected val emptyXML = """
    <component name="InspectionProjectProfileManager">
        <profile version="1.0" is_locked="true">
            <option name="myName" value="empty" />
        </profile>
    </component>
  """.trimIndent()

  @Language("XML")
  protected val conventionsXML = """
    <component name="InspectionProjectProfileManager">
        <profile version="1.0">
            <option name="myName" value="default.name.conventions" />
            <inspection_tool class="ExceptionNameDoesntEndWithException" enabled="true" level="WARNING" enabled_by_default="true">
                <scope name="Project Files" level="WARNING" enabled="true" />
            </inspection_tool>
        </profile>
    </component>
  """.trimIndent()
}
