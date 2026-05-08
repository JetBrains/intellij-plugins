package org.jetbrains.qodana.staticAnalysis.workflow

import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension.Companion.EP_NAME
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KSuspendFunction2
import kotlin.reflect.KSuspendFunction3
import kotlin.reflect.jvm.javaMethod

private val LOG = logger<QodanaWorkflowExtension>()

private fun qodanaWorkflowExtensions(): List<QodanaWorkflowExtension> {
  val isHeadless = getApplication().isHeadlessEnvironment
  return EP_NAME.extensionList.filter { !it.requireHeadless || isHeadless }
}

/**
 * Invokes the given one-argument workflow callback on all registered extensions that override it.
 *
 * Extensions are scheduled in dependency-aware layers derived from
 * [QodanaWorkflowExtension.provides] and [QodanaWorkflowExtension.dependsOn].
 * The scheduling log includes only extensions that implement the referenced callback.
 */
suspend fun <A> callQodanaWorkflowExtensions(
  phase: KSuspendFunction2<QodanaWorkflowExtension, A, Unit>,
  arg: A,
) {
  callQodanaWorkflowExtensions(phase.requireJavaMethod()) {
    phase(this, arg)
  }
}

/**
 * Invokes the given two-argument workflow callback on all registered extensions that override it.
 *
 * Extensions are scheduled in dependency-aware layers derived from
 * [QodanaWorkflowExtension.provides] and [QodanaWorkflowExtension.dependsOn].
 * The scheduling log includes only extensions that implement the referenced callback.
 */
suspend fun <A, B> callQodanaWorkflowExtensions(
  phase: KSuspendFunction3<QodanaWorkflowExtension, A, B, Unit>,
  first: A,
  second: B,
) {
  callQodanaWorkflowExtensions(phase.requireJavaMethod()) {
    phase(this, first, second)
  }
}

/**
 * Executes items in dependency-aware layers.
 *
 * An item becomes ready when every capability it depends on that is present in the current run
 * has been fully provided by all matching providers. All ready items from the same layer run in parallel.
 *
 * [phaseMethod] identifies the callback being executed and is used both for scheduling logs and for filtering
 * extensions that do not override the callback.
 */
private suspend fun callQodanaWorkflowExtensions(
  phaseMethod: Method,
  action: suspend QodanaWorkflowExtension.() -> Unit,
) {
  val items = qodanaWorkflowExtensions().filter { it.implementsMethod(phaseMethod) }
  val remaining = items.toMutableList()
  val providerCounts = items
    .flatMap { item -> item.provides }
    .groupingBy { it }
    .eachCount()
  val knownCapabilities = providerCounts.keys
  val completedProviderCounts = HashMap<QodanaWorkflowCapability, Int>()
  while (remaining.isNotEmpty()) {
    val ready = remaining.filter { item ->
      val effectiveDependencies = item.dependsOn.filterTo(HashSet()) { it in knownCapabilities }
      effectiveDependencies.all { capability -> completedProviderCounts[capability] == providerCounts[capability] }
    }

    if (ready.isEmpty()) {
      val unresolved = remaining.joinToString(separator = "; ") { item ->
        val pendingDependencies = item.dependsOn
          .filterTo(HashSet()) { capability ->
            capability in knownCapabilities && completedProviderCounts[capability] != providerCounts[capability]
          }
          .joinToString()
        "${item.describe()} waiting for [$pendingDependencies]"
      }
      error("Qodana workflow capability dependencies cannot be resolved: $unresolved")
    }

    LOG.info("Running ${phaseMethod.name} Qodana workflow layer: ${ready.joinToString { it.describe() }}")

    coroutineScope {
      ready.forEach { item ->
        launch {
          item.action()
        }
      }
    }

    ready.asSequence()
      .flatMap { item -> item.provides.asSequence() }
      .forEach { capability ->
        completedProviderCounts.merge(capability, 1, Int::plus)
      }
    remaining.removeAll(ready.toSet())
  }
}

/**
 * Invokes the given two-argument workflow callback sequentially on all registered extensions that override it.
 *
 * Unlike [callQodanaWorkflowExtensions], this variant preserves extension iteration order and does not use
 * dependency-graph scheduling.
 */
suspend fun <A, B> callQodanaWorkflowExtensionsSequentially(
  phase: KSuspendFunction3<QodanaWorkflowExtension, A, B, Unit>,
  first: A,
  second: B,
) {
  val phaseMethod = phase.requireJavaMethod()
  qodanaWorkflowExtensions().forEach { extension ->
    if (extension.implementsMethod(phaseMethod)) {
      phase(extension, first, second)
    }
  }
}

private fun KFunction<*>.requireJavaMethod(): Method {
  return javaMethod ?: error("Unable to resolve Java method for Qodana workflow callback: $name")
}

private fun QodanaWorkflowExtension.describe(): String {
  return javaClass.simpleName
}

/**
 * Returns whether this extension provides an effective override for the given callback method.
 */
@ApiStatus.Internal
fun QodanaWorkflowExtension.implementsMethod(method: Method): Boolean {
  return javaClass.methods.any { candidate ->
    candidate.name == method.name &&
    candidate.parameterTypes.contentEquals(method.parameterTypes) &&
    !candidate.isSynthetic &&
    candidate.declaringClass != QodanaWorkflowExtension::class.java
  }
}

/**
 * Appends a [OpenProjectTaskBuilder.beforeOpen] callback while preserving any callback already set on the builder.
 */
@ApiStatus.Internal
fun OpenProjectTaskBuilder.appendBeforeOpen(action: suspend (Project) -> Boolean) {
  val existingBeforeOpen = beforeOpen
  beforeOpen = callback@{ project ->
    if (existingBeforeOpen?.invoke(project) == false) {
      return@callback false
    }
    action(project)
  }
}

@ApiStatus.Internal
interface QodanaWorkflowExtension {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaWorkflowExtension> = ExtensionPointName("org.intellij.qodana.workflowExtension")
  }

  // Workaround: Some extensions are only applicable when not running from within the IDE,
  // but creating a clear lifecycle for qodana is a large task on its own.
  val requireHeadless: Boolean get() = false

  /**
   * Capabilities made available to dependent extensions invoked via [callQodanaWorkflowExtensions].
   */
  val provides: Set<QodanaWorkflowCapability> get() = emptySet()

  /**
   * Capabilities awaited before this extension enters callbacks invoked via [callQodanaWorkflowExtensions].
   * A dependency is satisfied only after all extensions that provide the capability have completed.
   * Capabilities without providers in the current run are ignored.
   */
  val dependsOn: Set<QodanaWorkflowCapability> get() = emptySet()

  /**
   * Called before project opening starts, before the platform chooses the project open processor.
   * Implementations may prepare the project directory or external environment for the selected analysis run.
   */
  suspend fun beforeProjectOpened(config: QodanaConfig) {}

  /**
   * Called while [OpenProjectTaskBuilder] is being configured for a Qodana analysis run.
   * Implementations may customize the open task and attach additional [OpenProjectTaskBuilder.beforeOpen] callbacks.
   * Use [appendBeforeOpen] instead of assigning [OpenProjectTaskBuilder.beforeOpen] directly, so callbacks from
   * multiple workflow extensions compose correctly.
   */
  suspend fun configureProjectOpening(config: QodanaConfig, openProjectTaskBuilder: OpenProjectTaskBuilder) {}

  /**
   * Called after the project has been opened and initially configured, when the project
   * was opened in *manual* mode ([QodanaConfig.rootJavaProjects] is non-empty).
   * Use this hook to trigger the build-system's import or removal of modules selected for analysis.
   */
  suspend fun manualProjectsImport(project: Project) {}

  /**
   * Called after the project has been opened and initially configured, when the project
   * was opened in *automatic* mode and the platform's default processor handled the import.
   * Use this hook to trigger the build-system's import or removal of modules selected for analysis.
   */
  suspend fun automaticProjectsImport(project: Project) {}

  /**
   * Called during the Qodana configuration phase, after the standard platform configurators
   * have run. Implementations may **modify the project model** here (for example, configure SDKs,
   * set up module JDKs, or adjust project structure). After this method returns, the platform
   * configuration cycle is run once more to apply the changes.
   *
   * @see afterConfiguration for the subsequent read-only phase
   */
  suspend fun configureForQodana(config: QodanaConfig, project: Project) {}

  /**
   * Called after [configureForQodana] and after the platform has applied all pending
   * configuration changes. Implementations must be **read-only**: do not modify the
   * project model here. Typical uses include validation, logging, or collecting
   * project-structure metrics.
   *
   * @see configureForQodana for the preceding write phase
   */
  suspend fun afterConfiguration(config: QodanaConfig, project: Project) {}

  /**
   * @deprecated Use [afterConfiguration] instead.
   */
  @Deprecated(
    message = "Use afterConfiguration(config: QodanaConfig, project: Project) instead",
    replaceWith = ReplaceWith("afterConfiguration(config, project)")
  )
  suspend fun beforeLaunch(context: QodanaRunContext) {
  }

  /**
   * Called before the project is closed and disposed of. Use this hook to flush or
   * persist any state that must be saved while the project is still accessible.
   */
  suspend fun beforeProjectClose(project: Project) {}

  /**
   * Called after the project has been closed and disposed of. Use this hook for
   * post-analysis cleanup that does not require access to the project (for example,
   * releasing external resources or reporting final results).
   */
  suspend fun afterProjectClosed(config: QodanaConfig) {}
}
