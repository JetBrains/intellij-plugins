package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

internal class QodanaScriptDefinitionSource(val project: Project) : ScriptDefinitionsSource {
  override val definitions: Sequence<ScriptDefinition>
    get() = sequenceOf(
      ScriptDefinition.FromConfigurations(
        defaultJvmScriptingHostConfiguration,
        qodanaInspectionsKtsScriptCompilationConfiguration(project, defaultJvmScriptingHostConfiguration),
        QodanaKtsInspectionsScriptEvaluationConfiguration(defaultJvmScriptingHostConfiguration)
      ).apply {
        order = Int.MIN_VALUE
      }
    )
}