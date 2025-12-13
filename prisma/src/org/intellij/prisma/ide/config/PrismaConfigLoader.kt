package org.intellij.prisma.ide.config

import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.execution.process.ProcessOutput
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageDescriptor
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.json.Json
import org.intellij.prisma.PrismaBundle

private const val TIMEOUT = 15000

private val LOG = logger<PrismaConfigLoader>()

class PrismaConfigLoader {
  @Suppress("JSUnresolvedReference")
  private val marker = "___PRISMA_LOADER___"

  fun accepts(file: VirtualFile): Boolean {
    return file.extension in EXTENSIONS
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun load(project: Project, file: VirtualFile): PrismaConfig? {
    if (!TrustedProjects.isProjectTrusted(project)) {
      return null
    }

    val interpreter = getInterpreter(project)
    if (interpreter == null) {
      throw RuntimeException(PrismaBundle.message("prisma.config.node.interpreter.error"))
    }

    val (_, run) = run(project, interpreter, file)
    val stdout = run.stdout.trim()
    val stderr = run.stderr.trim()

    if (run.exitCode == 0) {
      LOG.debug { "Evaluated ${file.path} config: stdout=$stdout\nstderr=$stderr" }
      val lastNewLine = stdout.lastIndexOf(marker)
      val startIndex = lastNewLine + marker.length + 1
      val result = if (lastNewLine >= 0 && stdout.length >= startIndex) stdout.substring(startIndex) else stdout
      if (result.isBlank() && stderr.isNotEmpty()) {
        completeExceptionally(file, run)
      }

      try {
        return PrismaConfig(file, parseJsonResult(result))
      }
      catch (e: Exception) {
        LOG.warn("${e.message}\nstdout: ${stdout}\nstderr: $stderr", e)
        throw RuntimeException(PrismaBundle.message("prisma.config.evaluation.error"), e)
      }
    }
    else {
      completeExceptionally(file, run)
    }
  }

  private fun completeExceptionally(file: VirtualFile, run: ProcessOutput): Nothing {
    LOG.warn(
      """
        |Failed to evaluate ${file.path} config. Exit code: ${run.exitCode}${if (run.isTimeout) ", timed out" else ""}
        |stdout: ${run.stdout}
        |stderr: ${run.stderr}
      """.trimMargin()
    )

    val errorDetails = run.stderr.trim()
    if (errorDetails.isNotEmpty()) {
      throw RuntimeException(PrismaBundle.message("prisma.config.evaluation.error"), Throwable(run.stderr))
    }
    else {
      throw RuntimeException(PrismaBundle.message("prisma.config.evaluation.error"))
    }
  }

  private fun parseJsonResult(result: String): PrismaConfigData {
    val json = Json {
      isLenient = true
      ignoreUnknownKeys = true
    }
    return json.decodeFromString<PrismaConfigData>(result)
  }

  private fun run(
    project: Project,
    interpreter: NodeJsInterpreter,
    file: VirtualFile,
  ): Pair<NodeTargetRun, ProcessOutput> {
    val packageJson = findPackageJson(file)
    val workingDir = file.parent
    if (packageJson != null) PackageJsonData.getOrCreate(packageJson).isModuleType else false
    val targetRun = createTargetRun(project, interpreter, workingDir.path)
    LOG.info("Loading ${file.path} config")

    val tsxPackageDescriptor = NodePackageDescriptor("tsx")
    val tsxPackage = tsxPackageDescriptor.listAvailable(project, interpreter, workingDir).firstOrNull()
    if (tsxPackage == null) {
      LOG.info("'tsx' package not found")
      throw RuntimeException(PrismaBundle.message("prisma.config.tsx.package.not.found.error"))
    }

    configureCommandLine(tsxPackage, targetRun)
    val processHandler = targetRun.startProcessEx().processHandler
    val processOutput = CapturingProcessRunner(processHandler).runProcess(TIMEOUT, true)
    return Pair(targetRun, processOutput)
  }

  private fun createTargetRun(project: Project, interpreter: NodeJsInterpreter, workingDir: String): NodeTargetRun {
    val targetRun = NodeTargetRun(interpreter, project, null, NodeTargetRunOptions.of(false))
    targetRun.commandLineBuilder.setWorkingDirectory(targetRun.path(workingDir))
    targetRun.commandLineBuilder.addEnvironmentVariable("NODE_ENV", "development")
    return targetRun
  }

  private fun configureCommandLine(
    tsxPackage: NodePackage,
    targetRun: NodeTargetRun,
  ) {
    val commandLine = targetRun.commandLineBuilder

    tsxPackage.findBinFilePath("tsx", "./bin/tsx.js")?.let {
      commandLine.addParameter(targetRun.path(it.toString()))
    } ?: throw RuntimeException(PrismaBundle.message("prisma.config.tsx.package.not.found.error"))

    commandLine.addParameter("-e")
    //language=JavaScript
    commandLine.addParameter(
      """
        import { loadConfigFromFile } from "@prisma/config";
    
        function printConfig({config, error}) {
          if (error) {
            console.error(error);
            process.exit(1);
          }
        
          console.log("$marker");
          console.log(JSON.stringify(config, null, 2));
          process.exit(0);
        }
        
        loadConfigFromFile({}).then(config => printConfig(config)).catch(err => console.error(err));
      """.trimIndent().trim()
    )
  }

  private fun findPackageJson(from: VirtualFile?): VirtualFile? {
    if (from == null) {
      return null
    }
    var packageJson = PackageJsonUtil.findUpPackageJson(from)
    while (packageJson != null && JSLibraryUtil.hasDirectoryInPath(packageJson, JSLibraryUtil.NODE_MODULES, null)) {
      packageJson = PackageJsonUtil.findUpPackageJson(packageJson.parent.parent)
    }
    return packageJson
  }
}

internal fun getInterpreter(project: Project): NodeJsInterpreter? {
  val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
  return if (interpreter != null && interpreter.validate(project) == null) interpreter else null
}
