package org.jetbrains.qodana.inspectionKts.kotlin.script

import java.io.File
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.intellij.ScriptDefinitionsProvider

internal class QodanaKtsInspectionsScriptDefinitionsProvider : ScriptDefinitionsProvider {
  override val id: String
    get() = "QodanaKtsInspectionsScriptDefinitionSource"

  override fun getDefinitionClasses(): Iterable<String> = emptyList()

  override fun getDefinitionsClassPath(): Iterable<File> = emptyList()

  override fun useDiscovery(): Boolean = false

  override fun provideDefinitions(
    baseHostConfiguration: ScriptingHostConfiguration,
    loadedScriptDefinitions: List<kotlin.script.experimental.host.ScriptDefinition>,
  ): Iterable<kotlin.script.experimental.host.ScriptDefinition> {
    return listOf(
      kotlin.script.experimental.host.ScriptDefinition(
        QodanaKtsInspectionsScriptCompilationConfiguration(baseHostConfiguration),
        QodanaKtsInspectionsScriptEvaluationConfiguration(baseHostConfiguration)
      )
    )
  }
}