// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring.extractComponent

import com.intellij.execution.process.CapturingAnsiEscapesAwareProcessHandler
import com.intellij.ide.actions.CreateFileAction
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.NonEmptyInputValidator
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.util.PathUtil
import com.intellij.util.containers.ContainerUtil
import org.angular2.cli.AngularCliFilter
import org.angular2.cli.GenerateCommand
import org.angular2.cli.GenerateCommandKind
import org.angular2.cli.GenerateJsonParser
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angularjs.lang.AngularJSLanguage
import java.io.File
import java.nio.charset.StandardCharsets

interface Angular2CliComponentGenerator {
  fun showDialog(): Array<String>?

  /**
   * Executes Angular CLI schematic.
   *
   * Tries to use patched virtual approach and then falls back to official one that does not work with Undo.
   *
   * @return Function to be called inside a command. It might create PsiFiles for supported Angular CLI versions,
   * otherwise files are eagerly created by the CLI and synced by the VFS.
   */
  fun generateComponent(cliDir: VirtualFile, workingDir: VirtualFile, arguments: Array<String>): () -> List<String>

  companion object {
    fun getInstance(project: Project): Angular2CliComponentGenerator = project.service()
  }
}

class Angular2CliComponentGeneratorImpl(val project: Project) : Angular2CliComponentGenerator {
  override fun showDialog(): Array<String>? {
    val ref = Ref<Array<String>?>(null)
    ApplicationManager.getApplication().invokeAndWait {
      val str = Messages.showInputDialog(project, Angular2Bundle.message("angular.refactor.extractComponent.dialog.name"), Angular2Bundle.message("angular.refactor.extractComponent.dialog"), null, null, NonEmptyInputValidator())

      if (str != null) {
        // instead of validation we sanitize value because Angular CLI has built in name normalization
        // that could drift apart from our validation
        ref.set(arrayOf(str.replace(" --", "-").replace(" ", "")))
      }
    }
    return ref.get()
  }

  override fun generateComponent(cliDir: VirtualFile,
                                 workingDir: VirtualFile,
                                 arguments: Array<String>): () -> List<String> {
    try {
      return generateComponentVirtual(cliDir, workingDir, arguments)
    }
    catch (e: Exception) {
      return generateComponentFallback(cliDir, workingDir, arguments)
    }
  }

  private fun generateComponentVirtual(cliDir: VirtualFile, workingDir: VirtualFile, arguments: Array<String>): () -> List<String> {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: throw Exception("Node interpreter not found")
    findCliPackage(cliDir) ?: throw Exception("Angular CLI package not found")

    val utilityDirectory = JSLanguageServiceUtil.getPluginDirectory(AngularJSLanguage::class.java, "ngCli")
    val binPath = "${utilityDirectory}${File.separator}generate.js"

    val output = executeNode(interpreter, workingDir, arrayOf(binPath, cliDir.path, "component", *arguments))
    val commands = GenerateJsonParser.parse(output)

    return { executeGenerateCommands(cliDir, commands) }
  }

  private fun executeGenerateCommands(cliDir: VirtualFile, commands: MutableList<GenerateCommand>): List<String> {
    val affectedPaths = mutableListOf<String>()

    val psiManager = PsiManager.getInstance(project)
    val cliDirectory = psiManager.findDirectory(cliDir)!!

    CommandProcessor.getInstance().runUndoTransparentAction {
      for (command in commands) {
        when (command.kind) {
          GenerateCommandKind.WRITE -> {
            val mkDirs = CreateFileAction.MkDirs(command.path, cliDirectory)

            val file = mkDirs.directory.findFile(mkDirs.newName) ?: mkDirs.directory.createFile(mkDirs.newName)
            val document = PsiDocumentManager.getInstance(project).getDocument(file)!!
            document.setText(StringUtil.convertLineSeparators(command.content!!))
            PsiDocumentManager.getInstance(project).commitDocument(document)

            affectedPaths.add(command.path)
          }
          GenerateCommandKind.DELETE -> {
            val file = psiManager.findFile(cliDir.findFileByRelativePath(command.path)!!)!!
            file.delete()

            affectedPaths.add(command.path)
          }
          GenerateCommandKind.RENAME -> {
            val fromFile = cliDir.findFileByRelativePath(command.path)!!
            val mkDirs = CreateFileAction.MkDirs(command.to!!, cliDirectory)
            if (mkDirs.directory.virtualFile != fromFile.parent) {
              fromFile.rename(psiManager, ".angular_cli_ij_temp")
              fromFile.move(psiManager, mkDirs.directory.virtualFile)
            }
            fromFile.rename(psiManager, mkDirs.newName)

            affectedPaths.add(command.path)
            affectedPaths.add(command.to!!)
          }
        }
      }
    }

    return affectedPaths
  }

  private fun generateComponentFallback(cliDir: VirtualFile, workingDir: VirtualFile, arguments: Array<String>): () -> List<String> {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: throw Exception("Node interpreter not found")
    val module = findCliPackage(cliDir) ?: throw Exception("Angular CLI package not found")

    val binPath = NodePackage(module.virtualFile!!.path).findBinFile("ng", null)?.absolutePath
    if (binPath == null) {
      throw Exception("Executable not found")
    }

    val output = executeNode(interpreter, workingDir, arrayOf(binPath, "generate", "component", *arguments))

    cliDir.refresh(false, true)
    cliDir.children

    return { extractPaths(output) }
  }

  private fun findCliPackage(cliDir: VirtualFile): CompletionModuleInfo? {
    val modules = mutableListOf<CompletionModuleInfo>()
    NodeModuleSearchUtil.findModulesWithName(modules, Angular2LangUtil.ANGULAR_CLI_PACKAGE, cliDir, null)
    return modules.firstOrNull()
  }

  /**
   * Based on NpmPackageProjectGenerator#generate
   */
  private fun executeNode(node: NodeJsInterpreter, workingDir: VirtualFile, arguments: Array<String>): String {
    val configurator = NodeCommandLineConfigurator.find(node)

    val parameters: MutableList<String> = ArrayList()
    ContainerUtil.addAll(parameters, *arguments)

    val commandLine = NodeCommandLineUtil.createCommandLine(false)
    commandLine.addParameters(parameters)
    commandLine.setWorkDirectory(VfsUtilCore.virtualToIoFile(workingDir).path)
    commandLine.charset = StandardCharsets.UTF_8
    NodeCommandLineUtil.configureUsefulEnvironment(commandLine)
    if (node is NodeJsLocalInterpreter) {
      val bin = PathUtil.getParentPath(node.interpreterSystemDependentPath)
      var path = commandLine.parentEnvironment["PATH"]
      path = if (StringUtil.isEmpty(path)) bin else bin + File.pathSeparatorChar + path
      commandLine.environment["PATH"] = path
    }
    configurator.configure(commandLine)

    val processHandler = CapturingAnsiEscapesAwareProcessHandler(commandLine)
    val output = processHandler.runProcess()

    if (output.exitCode != 0 || output.stdout.isEmpty()) {
      throw Exception("Node error:s\n" + output.stderr)
    }

    return output.stdout
  }
}

fun extractPaths(output: String): List<String> {
  return output.lineSequence()
    .flatMap { AngularCliFilter.doParse(it) }
    .map { it.filePath }
    .toList()
}