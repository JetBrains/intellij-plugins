package org.jetbrains.qodana.cpp

import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.projectImport.ProjectOpenProcessor
import com.jetbrains.cidr.cpp.cmake.CMakeProjectOpenProcessor
import com.jetbrains.cidr.cpp.compdb.wizard.CompDBProjectOpenProcessor
import com.jetbrains.cidr.cpp.makefile.wizard.MakefileProjectOpenProcessor
import com.jetbrains.cidr.meson.wizard.MesonProjectOpenProcessor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaProjectLoaderExtension

private val LOG = logger<QodanaCppProjectLoaderExtension>()

/**
 * A mapping of values allowed by cpp.buildSystem in qodana.yaml to the corresponding ProjectOpenProcessor class.
 *
 * Items are ordered by selection priority when a build system is not specified.
 *
 * Note: this map should ideally contain all the processors that CLion supports so that Qodana does not artificially restrict the
 * set of build systems that could theoretically be used. Build systems explicitly supported by Qodana should be specified in the JSON
 * schema for `cpp.buildSystem`.
 */
private val supportedProcessors = mapOf(
  "CMake" to CMakeProjectOpenProcessor::class,
  "CompDB" to CompDBProjectOpenProcessor::class,
  "Meson" to MesonProjectOpenProcessor::class,
  "Make" to MakefileProjectOpenProcessor::class,
).map { (key, value) -> key.lowercase() to value }.toMap() // keys are always lowercase

fun selectProcessor(processors: List<ProjectOpenProcessor>): ProjectOpenProcessor {
  LOG.debugValues(
    "Qodana C++ is selecting a ProjectOpenProcessor for ${qodanaConfig.projectPath}. Available processors are:",
    processors.map { it.name }
  )

  val projectPath = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(qodanaConfig.projectPath)
  if (projectPath == null) throw QodanaException("Project path '${qodanaConfig.projectPath}' was not found")

  val requestedBuildSystem = qodanaConfig.cpp?.buildSystem
  if (requestedBuildSystem != null) {
    LOG.debug("Build system specified in qodana.yaml: '$requestedBuildSystem'")
    val requestedProcessor = supportedProcessors.getOrElse(requestedBuildSystem.lowercase()) {
      throw QodanaException("Specified build system '$requestedBuildSystem' is not supported by Qodana")
    }

    try {
      return processors.first { it::class == requestedProcessor }.also {
        LOG.debug("Selected matching processor for build system '$requestedBuildSystem': '${it.name}'")
      }
    }
    catch (_: NoSuchElementException) {
      throw QodanaException("Specified build system '$requestedBuildSystem' is not supported by CLion")
    }
  }
  LOG.debugValues(
    "Build system was not specified in qodana.yaml. Selecting the best match from preference list:",
    supportedProcessors.keys
  )

  // Sort processors by our preference, placing unrecognized processors at the end.
  val processors = processors.sortedBy { processor ->
    supportedProcessors.values.indexOf(processor::class).let { if (it == -1) Int.MAX_VALUE else it }
  }

  try {
    return processors.first { it.canOpenProject(projectPath) }.also {
      LOG.debug("Selected the best supported processor: '${it.name}'")
    }
  }
  catch (_: NoSuchElementException) {
    throw QodanaException("No build systems were detected in '${qodanaConfig.projectPath}'")
  }
}

class QodanaCppProjectLoaderExtension : QodanaProjectLoaderExtension {
  override val buildProjectOpenTask: OpenProjectTaskBuilder.() -> Unit = {
    @Suppress("UNCHECKED_CAST")  // Why is it a list of `Any` anyway? This makes no sense to me.
    processorChooser = { selectProcessor(it as List<ProjectOpenProcessor>) }
  }
}
