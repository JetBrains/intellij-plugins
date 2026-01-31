package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.displayName
import kotlin.script.experimental.api.fileExtension
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.JvmDependency

internal fun qodanaInspectionsKtsScriptCompilationConfiguration(
  project: Project,
  hostConfiguration: ScriptingHostConfiguration
): ScriptCompilationConfiguration {
  val provider = inspectionKtsClasspathProvider(project, doInitialize = true)
  return QodanaKtsInspectionsScriptCompilationConfiguration(provider, hostConfiguration)
}

/**
 * Defines the script configuration (used by resolve/highlighting/etc in IDE)
 * See [org.jetbrains.kotlin.idea.base.analysis.RootKindMatcherImpl.matches]
 *
 * Need to return new instance of [ScriptCompilationConfiguration] class from [beforeCompiling] so that
 * [org.jetbrains.kotlin.scripting.resolve.refineScriptCompilationConfiguration] would return an instance of [ScriptCompilationConfiguration],
 * (not instance of our class loaded by qodana classloader!), otherwise we will fail to read cached value from file's attributes:
 * [org.jetbrains.kotlin.idea.core.script.k1.configuration.cache.ScriptConfigurationSnapshotFile]
 *
 * The dependencies are provided in `refineConfiguration.beforeCompiling` to achieve a lazy collection of dependencies:
 * when the .inspection.kts file is actually met (for example, opened in editor), the [InspectionKtsClasspathProvider.collectClassPath] is called
 */
internal class QodanaKtsInspectionsScriptCompilationConfiguration(
  classpathProvider: InspectionKtsClasspathProvider,
  hostConfiguration: ScriptingHostConfiguration
) : ScriptCompilationConfiguration({
  defaultImports(InspectionKtsDefaultImportProvider.imports())
  hostConfiguration(hostConfiguration)
  baseClass(KotlinType("kotlin.script.templates.standard.ScriptTemplateWithBindings"))
  displayName("Qodana .inspection.kts")
  fileExtension("inspection.kts")
  refineConfiguration {
    beforeCompiling { context ->
      ScriptCompilationConfiguration(context.compilationConfiguration) {
        dependencies.append(JvmDependency(classpathProvider.collectClassPath()))
      }.asSuccess()
    }
  }
})

internal class QodanaKtsInspectionsScriptEvaluationConfiguration(
  hostConfiguration: ScriptingHostConfiguration
) : ScriptEvaluationConfiguration({
  hostConfiguration(hostConfiguration)
})

