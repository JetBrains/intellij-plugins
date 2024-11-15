package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.kotlin.idea.core.script.k2.BundledScriptConfigurationsSource
import org.jetbrains.kotlin.idea.core.script.scriptDefinitionsSourceOfType
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

internal class QodanaScriptConfigurationSource(
  override val project: Project,
  cs: CoroutineScope
) : BundledScriptConfigurationsSource(project, cs) {
  override fun getScriptDefinitionsSource(): ScriptDefinitionsSource? {
    return project.scriptDefinitionsSourceOfType<QodanaScriptDefinitionSource>()
  }
}

internal class QodanaScriptDefinitionSource : ScriptDefinitionsSource {
  override val definitions: Sequence<ScriptDefinition>
    get() = sequenceOf(
      ScriptDefinition.FromConfigurations(
        defaultJvmScriptingHostConfiguration,
        QodanaKtsInspectionsScriptCompilationConfiguration(defaultJvmScriptingHostConfiguration),
        QodanaKtsInspectionsScriptEvaluationConfiguration(defaultJvmScriptingHostConfiguration)
      ).apply {
        order = Int.MIN_VALUE
      }
    )
}