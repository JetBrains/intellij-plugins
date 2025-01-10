package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.kotlin.idea.core.script.KotlinScriptEntitySource
import org.jetbrains.kotlin.idea.core.script.getUpdatedStorage
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

  override suspend fun updateModules(storage: MutableEntityStorage?) {
    val updatedStorage = getUpdatedStorage(
      project, data.get()
    ) { QodanaKotlinScriptEntitySource(it) }

    project.workspaceModel.update("updating .inspection.kts modules") {
      it.replaceBySource(
        { source -> source is QodanaKotlinScriptEntitySource }, updatedStorage
      )
    }
  }
}

open class QodanaKotlinScriptEntitySource(override val virtualFileUrl: VirtualFileUrl?) :
  KotlinScriptEntitySource(virtualFileUrl)

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