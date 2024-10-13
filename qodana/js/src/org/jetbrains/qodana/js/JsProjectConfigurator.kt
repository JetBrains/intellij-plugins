package org.jetbrains.qodana.js

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.npm.NpmManager
import com.intellij.javascript.nodejs.npm.NpmUtil
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunProfileState
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.QodanaBundle

private val LOG = logger<JsProjectConfigurator>()
private const val SHRINKWRAP_JSON_FILENAME = "npm-shrinkwrap"
private const val JS_CONFIGURATOR_STATUS = "static.analysis.use.js.configurator"

class JsProjectConfigurator : CommandLineInspectionProjectConfigurator {
  override fun getName() = "qodanaJsProjectConfigurator"

  override fun getDescription() =
    QodanaBundle.message("progress.message.qodana.js.npm.configuring")

  override fun preConfigureProject(project: Project, context: CommandLineInspectionProjectConfigurator.ConfiguratorContext) {
    if (Registry.`is`(JS_CONFIGURATOR_STATUS)) {
      val lockJsonFile = detectPackageManagerRefInDirectory(project.guessProjectDir())
      if (lockJsonFile != null) {
        if (lockJsonFile.name == NpmUtil.YARN_LOCK_FILENAME)
          installDependency(project, lockJsonFile, NpmCommand.INSTALL)
        else
          installDependency(project, lockJsonFile, NpmCommand.CI)
      }
      else {
        val packageJson = PackageJsonFileManager.getInstance(project).validPackageJsonFiles.minByOrNull { it.path }
        installDependency(project, packageJson, NpmCommand.INSTALL)
      }
    }
  }

  private fun installDependency(project: Project, packageJson: VirtualFile?, command: NpmCommand) {
    if (packageJson == null) return
    LOG.info("Running install js dependency...")
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreterRef.resolveNotNull(project)
    val npmPkgRef = NpmManager.getInstance(project).packageRef
    val targetRun = configureCommandLine(interpreter, npmPkgRef, packageJson.path, command, project, packageJson)
    val runner = CapturingProcessRunner(targetRun.startProcess())
    runner.runProcess()
    packageJson.parent.refresh(false, true)
  }

  private fun configureCommandLine(
    nodeInterpreter: NodeJsInterpreter,
    npmPackageRef: NodePackageRef,
    packageJsonPath: String,
    command: NpmCommand,
    project: Project,
    packageJson: VirtualFile
  ): NodeTargetRun {
    val targetRun = NodeTargetRun(nodeInterpreter, project, null, NodeTargetRunOptions.of(true))
    NpmRunProfileState.configureCommandLine(targetRun, "", npmPackageRef, packageJsonPath, command,
                                            listOf(), "", EnvironmentVariablesData.DEFAULT, null)
    prepareCommandLineYarnLock(packageJson, targetRun)
    return targetRun
  }

  private fun prepareCommandLineYarnLock(packageJson: VirtualFile, targetRun: NodeTargetRun) {
    if (packageJson.name == NpmUtil.YARN_LOCK_FILENAME) {
      targetRun.commandLineBuilder.addParameters("--immutable", "--immutable-cache", "--check-cache")
    }
  }

  private fun detectPackageManagerRefInDirectory(dir: VirtualFile?): VirtualFile? {
    if (dir == null) return null
    if (dir.exists(NpmUtil.YARN_LOCK_FILENAME))
      return dir.findChild(NpmUtil.YARN_LOCK_FILENAME)

    if (dir.exists(NpmUtil.PACKAGE_LOCK_JSON_FILENAME))
      return dir.findChild(NpmUtil.PACKAGE_LOCK_JSON_FILENAME)

    if (dir.exists(SHRINKWRAP_JSON_FILENAME))
      return dir.findChild(SHRINKWRAP_JSON_FILENAME)

    return null
  }

  private fun VirtualFile.exists(filename: String): Boolean {
    val child = this.findChild(filename)
    return child != null && !child.isDirectory
  }
}
