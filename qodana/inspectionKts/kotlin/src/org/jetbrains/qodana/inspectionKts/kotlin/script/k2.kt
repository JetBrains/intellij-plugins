package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import kotlin.script.experimental.host.ScriptDefinition
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.intellij.ScriptDefinitionsProvider
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

internal class QodanaScriptDefinitionsProvider(val project: Project) : ScriptDefinitionsProvider {
  override val id: String = "QodanaInspections"

  override fun provideDefinitions(
    baseHostConfiguration: ScriptingHostConfiguration,
    loadedScriptDefinitions: List<ScriptDefinition>,
  ): List<ScriptDefinition> = listOf(
    ScriptDefinition(
      qodanaInspectionsKtsScriptCompilationConfiguration(project, defaultJvmScriptingHostConfiguration),
      QodanaKtsInspectionsScriptEvaluationConfiguration(defaultJvmScriptingHostConfiguration)
    )
  )
}
