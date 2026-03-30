package org.jetbrains.qodana.cpp

import com.intellij.clion.makefile.core.wizard.MakefileProjectOpenProcessor
import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.projectImport.ProjectOpenProcessor
import com.jetbrains.cidr.cpp.cmake.CMakeProjectOpenProcessor
import com.jetbrains.cidr.cpp.compdb.wizard.CompDBProjectOpenProcessor
import com.jetbrains.cidr.meson.wizard.MesonProjectOpenProcessor
import org.jdom.Element
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaProjectLoaderExtension
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists

private val log = logger<QodanaCppProjectLoaderExtension>()

/**
 * A mapping of values allowed by cpp.buildSystem in qodana.yaml to the corresponding ProjectOpenProcessor class.
 *
 * Items are ordered by selection priority when a build system is not specified.
 *
 * Note: this map should ideally contain all the processors that CLion supports so that Qodana does not artificially restrict the
 * set of build systems that could theoretically be used. Build systems explicitly supported by Qodana should be specified in the JSON
 * schema for `cpp.buildSystem`.
 */
/** Display names of supported build systems, in selection priority order. */
val supportedBuildSystems = listOf("CMake", "CompDB", "Meson", "Make")

private val supportedProcessors = mapOf(
    "CMake" to CMakeProjectOpenProcessor::class,
    "CompDB" to CompDBProjectOpenProcessor::class,
    "Meson" to MesonProjectOpenProcessor::class,
    "Make" to MakefileProjectOpenProcessor::class,
).map { (key, value) -> key.lowercase() to value }.toMap() // keys are always lowercase

/** Maps processor class → workspace component name (the `@State(name)` of the corresponding [CidrWorkspace]). */
private val processorToWorkspaceComponent = mapOf(
    CMakeProjectOpenProcessor::class to "CMakeWorkspace",
    CompDBProjectOpenProcessor::class to "CompDBWorkspace",
    MesonProjectOpenProcessor::class to "MesonWorkspace",
    MakefileProjectOpenProcessor::class to "MakefileWorkspace",
)

/**
 * The workspace component name (e.g. "CMakeWorkspace", "MakefileWorkspace") for the build system
 * selected by [selectProcessor]. Set during project open, read by [QodanaCppStartupManager] to
 * wait only for the relevant workspace instead of all workspaces.
 *
 * Uses [AtomicReference] for thread-safe get-and-set. The value is effectively single-assignment:
 * set once during project open (EDT), read during startup (also EDT via `withContext(Dispatchers.EDT)`).
 */
private val selectedWorkspaceComponentNameRef = AtomicReference<String?>(null)
val selectedWorkspaceComponentName: String? get() = selectedWorkspaceComponentNameRef.get()

fun selectProcessor(processors: List<ProjectOpenProcessor>): ProjectOpenProcessor {
    selectedWorkspaceComponentNameRef.set(null)  // reset for this analysis run
    log.debugValues(
        "Qodana C++ is selecting a ProjectOpenProcessor for ${qodanaConfig.projectPath}. Available processors are:",
        processors.map { it.name }
    )

    val projectPath = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(qodanaConfig.projectPath)
    if (projectPath == null) throw QodanaException("Project path '${qodanaConfig.projectPath}' was not found")

    val requestedBuildSystem = qodanaConfig.cpp?.buildSystem
    if (requestedBuildSystem != null) {
        log.debug("Build system specified in qodana.yaml: '$requestedBuildSystem'")
        val requestedProcessor = supportedProcessors.getOrElse(requestedBuildSystem.lowercase()) {
            throw QodanaException("Specified build system '$requestedBuildSystem' is not supported by Qodana")
        }

        try {
            return processors.first { it::class == requestedProcessor }.also {
                selectedWorkspaceComponentNameRef.set(processorToWorkspaceComponent[it::class])
                println("Build system: ${it.name} (specified in qodana.yaml)")
            }
        } catch (_: NoSuchElementException) {
            throw QodanaException("Specified build system '$requestedBuildSystem' was not detected in the project")
        }
    }
    log.debugValues(
        "Build system was not specified in qodana.yaml. Selecting the best match from preference list:",
        supportedProcessors.keys
    )

    // Sort processors by our preference, placing unrecognized processors at the end.
    val sortedProcessors = processors.sortedBy { processor ->
        supportedProcessors.values.indexOf(processor::class).let { if (it == -1) Int.MAX_VALUE else it }
    }

    try {
        return sortedProcessors.first { it.canOpenProject(projectPath) }.also {
            selectedWorkspaceComponentNameRef.set(processorToWorkspaceComponent[it::class])
            println("Build system: ${it.name} (auto-detected)")
        }
    } catch (_: NoSuchElementException) {
        throw QodanaException("No build systems were detected in '${qodanaConfig.projectPath}'")
    }
}

/**
 * Maps `cpp.buildSystem` values to the `@State(name)` of the corresponding [CidrWorkspace] component in `.idea/misc.xml`.
 * Keys are pre-lowercased — lookups must use `.lowercase()`.
 */
private val buildSystemToWorkspaceComponent = mapOf(
    "cmake" to "CMakeWorkspace",
    "compdb" to "CompDBWorkspace",
    "make" to "MakefileWorkspace",
    "meson" to "MesonWorkspace",
)

@VisibleForTesting
internal val allWorkspaceComponentNames = buildSystemToWorkspaceComponent.values.toSet()

class QodanaCppProjectLoaderExtension : QodanaProjectLoaderExtension {
    /**
     * QD-13184: Prepare `.idea` so that `qodana.yaml` build system and preset settings take effect.
     *
     * When `.idea` exists, the platform opens the project directly, bypassing processor selection.
     * We fix this by surgically editing `.idea` state so that the correct CIDR workspace is linked
     * and CMake profiles are reset, while preserving user-authored config (inspection profiles, etc.).
     */
    override fun prepareProjectDirectory(projectPath: Path) {
        val cppConfig = try {
            qodanaConfig.cpp
        } catch (e: IllegalStateException) {
            log.debug("Qodana config not available, skipping .idea preparation", e)
            null
        } ?: return
        val ideaDir = projectPath / ".idea"
        if (!ideaDir.exists()) return

        val buildSystem = cppConfig.buildSystem
        val cmakePreset = cppConfig.cmakePreset

        if (buildSystem == null && cmakePreset == null) return

        // Delete workspace.xml — it's entirely derived/session state (CMakeSettings, CMakePresetLoader, etc.)
        // and prevents CMakeEnabledProfileInitializer from applying the preset from qodana.yaml.
        if ((ideaDir / "workspace.xml").deleteIfExists()) {
            println("Resetting stale build system session state in .idea")
            log.info("Deleted .idea/workspace.xml to reset build system session state")
        }

        // Ensure the correct workspace component is linked in misc.xml.
        if (buildSystem != null) {
            val targetComponent = buildSystemToWorkspaceComponent[buildSystem.lowercase()]
                ?: return  // Unknown build system — selectProcessor will fail later with a clear error
            ensureWorkspaceComponentInMiscXml(ideaDir / "misc.xml", targetComponent)
        }
    }

    override val buildProjectOpenTask: OpenProjectTaskBuilder.() -> Unit = {
        // The List<Any> type comes from CLion's ProjectOpenProcessor API.
        // This cast is exercised by BuildSystemSelectionTest integration tests (every analyze() call triggers it).
        @Suppress("UNCHECKED_CAST")
        processorChooser = { selectProcessor(it as List<ProjectOpenProcessor>) }
    }
}

/**
 * Ensure `.idea/misc.xml` has a workspace component with the given [targetName] and a valid `PROJECT_DIR`.
 * Removes workspace components for other build systems to avoid conflicts.
 */
@VisibleForTesting
internal fun ensureWorkspaceComponentInMiscXml(miscXml: Path, targetName: String) {
    val root = if (miscXml.exists()) {
        JDOMUtil.load(miscXml)
    } else {
        miscXml.parent.createDirectories()
        Element("project").setAttribute("version", "4")
    }

    // Remove all CIDR workspace components (stale or conflicting).
    root.getChildren("component")
        .filter { it.getAttributeValue("name") in allWorkspaceComponentNames }
        .forEach { root.removeContent(it) }

    // Add the target workspace component with a valid project link.
    root.addContent(
        Element("component")
            .setAttribute("name", targetName)
            .setAttribute("PROJECT_DIR", "\$PROJECT_DIR$")
    )

    JDOMUtil.write(root, miscXml)
    log.info("Ensured $targetName with valid PROJECT_DIR in .idea/misc.xml")
}
