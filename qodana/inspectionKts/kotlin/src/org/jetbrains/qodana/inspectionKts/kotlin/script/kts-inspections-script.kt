package org.jetbrains.qodana.inspectionKts.kotlin.script

import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.JvmDependency

/**
 * Defines the script configuration (used by resolve/highlighting/etc in IDE)
 * See [org.jetbrains.kotlin.idea.base.analysis.RootKindMatcherImpl.matches]
 *
 * Need to return new instance of [ScriptCompilationConfiguration] class from [beforeCompiling] so that
 * [org.jetbrains.kotlin.scripting.resolve.refineScriptCompilationConfiguration] would return an instance of [ScriptCompilationConfiguration],
 * (not instance of our class loaded by qodana classloader!), otherwise we will fail to read cached value from file's attributes:
 * [org.jetbrains.kotlin.idea.core.script.configuration.cache.ScriptConfigurationSnapshotFile]
 *
 * The dependencies are provided in `refineConfiguration.beforeCompiling` to achieve a lazy collection of dependencies:
 * when the .inspection.kts file is actually met (for example, opened in editor), the [InspectionKtsClasspathService.collectClassPath] is called
 */
internal class QodanaKtsInspectionsScriptCompilationConfiguration(
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
        dependencies.append(JvmDependency(InspectionKtsClasspathService.getInstance().collectClassPath()))
      }.asSuccess()
    }
  }
})

internal class QodanaKtsInspectionsScriptEvaluationConfiguration(
  hostConfiguration: ScriptingHostConfiguration
) : ScriptEvaluationConfiguration({
  hostConfiguration(hostConfiguration)
})

