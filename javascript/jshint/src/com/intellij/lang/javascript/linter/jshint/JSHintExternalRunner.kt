package com.intellij.lang.javascript.linter.jshint

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult
import com.intellij.lang.javascript.linter.JSLinterError
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation
import com.intellij.lang.javascript.linter.JSLinterInput
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Executes JSHint as an external Node.js process and parses the results.
 */
internal class JSHintExternalRunner {

  fun execute(collectedInfo: JSLinterInput<JSHintState>): JSLinterAnnotationResult? {
    val project = collectedInfo.project
    if (project.isDisposed) {
      return null
    }

    val state = collectedInfo.state
    val file = collectedInfo.psiFile.virtualFile ?: return null

    // Check if file is ignored
    if (JSHintConfigFileUtil.isIgnored(project, file)) {
      return null
    }

    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: return JSLinterAnnotationResult.create(
      collectedInfo,
      JSLinterFileLevelAnnotation(JSHintBundle.message("jshint.inspection.message.node.interpreter.not.configured")),
      null
    )

    // Resolve JSHint package
    val packageRef = state.nodePackageRef
    val jshintPackage = packageRef.constantPackage
    if (jshintPackage == null || jshintPackage.isEmptyPath) {
      return JSLinterAnnotationResult.create(
        collectedInfo,
        JSLinterFileLevelAnnotation(JSHintBundle.message("jshint.inspection.message.package.not.configured")),
        null
      )
    }

    val resolver = JSHintConfigResolver(state, project, file)
    val resolution = when (val result = resolver.resolve()) {
      is JSHintConfigResolver.Result.Success -> result.resolution
      is JSHintConfigResolver.Result.Error -> {
        return JSLinterAnnotationResult.create(collectedInfo, result.annotation, result.configFile)
      }
    }

    return try {
      val errors = execute(
        project,
        interpreter,
        jshintPackage,
        file,
        collectedInfo.fileContent,
        resolution.configFile
      )
      if (errors.isNotEmpty()) {
        JSHintAnnotationResult(collectedInfo.colorsScheme, errors, resolution.configVirtualFile, resolution.optionsState)
      }
      else {
        null
      }
    }
    catch (e: ExecutionException) {
      LOG.warn("Failed to execute JSHint", e)
      JSLinterAnnotationResult.create(
        collectedInfo,
        JSLinterFileLevelAnnotation(JSHintBundle.message("jshint.inspection.message.execution.failed", e.message ?: "")),
        resolution.configVirtualFile
      )
    }
  }

  private fun execute(
    project: Project,
    interpreter: NodeJsInterpreter,
    jshintPackage: NodePackage,
    file: VirtualFile,
    fileContent: String,
    configFile: Path,
  ): List<JSLinterError> {
    val jshintMainFile = jshintPackage.findBinFilePath("jshint", "bin/jshint", interpreter)
    if (jshintMainFile == null || !jshintMainFile.exists()) {
      throw ExecutionException(JSHintBundle.message("jshint.inspection.message.jshint.binary.not.found", jshintMainFile))
    }

    val targetRun = NodeTargetRun(interpreter, project, null, NodeTargetRunOptions.of(false))
    val commandLineBuilder = targetRun.commandLineBuilder
    val nioFile = file.toNioPath()
    nioFile.parent?.let { workingDirectory ->
      commandLineBuilder.setWorkingDirectory(targetRun.path(workingDirectory))
    }
    commandLineBuilder.addParameter(targetRun.path(jshintMainFile))
    commandLineBuilder.addParameter("--reporter=checkstyle")
    commandLineBuilder.addParameter("--config")
    commandLineBuilder.addParameter(targetRun.path(configFile))
    commandLineBuilder.addParameter("--filename")
    commandLineBuilder.addParameter(targetRun.path(nioFile))
    // Pass file content via stdin using the '-' argument
    commandLineBuilder.addParameter("-")

    val processHandler = targetRun.startProcess()


    // Write file content to stdin
    try {
      processHandler.processInput!!.write(fileContent.toByteArray(StandardCharsets.UTF_8))
      processHandler.processInput!!.close()
    }
    catch (e: IOException) {
      throw ExecutionException(JSHintBundle.message("jshint.inspection.message.file.content.not.written"), e)
    }

    val output = CapturingProcessRunner(processHandler).runProcess(TIMEOUT.inWholeMilliseconds.toInt(), true)

    // Check for execution errors
    when {
      output.isTimeout -> throw ExecutionException(JSHintBundle.message("jshint.inspection.message.execution.timed.out"))
      output.exitCode != 0 && output.exitCode != 2 -> {
        // Exit code 2 means JSHint found errors (which is normal)
        // Any other non-zero exit code is an error
        val stderr = output.stderr
        if (stderr.isNotEmpty()) {
          LOG.warn("JSHint execution failed with exit code ${output.exitCode}: $stderr")
          throw ExecutionException(JSHintBundle.message("jshint.inspection.message.execution.failed", stderr))
        }
      }
    }

    return JSHintResultParser.parse(output.stdout)
  }

  class JSHintAnnotationResult(
    colorsScheme: EditorColorsScheme?,
    errors: List<JSLinterError>,
    configFile: VirtualFile?,
    val optionsState: JSHintOptionsState,
  ) : JSLinterAnnotationResult(colorsScheme, errors, null, configFile)

  companion object {
    private val LOG: Logger = logger<JSHintExternalRunner>()
    private val TIMEOUT: Duration = 10.seconds
  }
}
