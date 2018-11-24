package org.angularjs.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.NodeSettings
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import java.io.File

/**
 * @author Dennis.Ushakov
 */
object BlueprintsLoader {
  object CacheModificationTracker: ModificationTracker {
    var count:Long = 0
    override fun getModificationCount(): Long {
      return count
    }
  }

  fun load(project: Project): Collection<Blueprint> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      Result.create(doLoad(project), CacheModificationTracker)
    }
  }

  private fun doLoad(project: Project): Collection<Blueprint> {
    val interpreter = NodeJsInterpreterManager.getInstance(project).default
    val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return emptyList()

    val modules:MutableList<CompletionModuleInfo> = mutableListOf()
    val baseDir = project.baseDir
    NodeModuleSearchUtil.findModulesWithName(modules, "angular-cli", baseDir, NodeSettings.create(node), false)

    val module = modules.firstOrNull() ?: return emptyList()
    val moduleExe = "${module.virtualFile!!.path}${File.separator}bin${File.separator}ng"
    val commandLine = GeneralCommandLine(node.interpreterSystemDependentPath, moduleExe, "help", "generate")
    commandLine.withWorkDirectory(baseDir.path)
    val handler = CapturingProcessHandler(commandLine)
    val output = handler.runProcess()

    if (output.exitCode == 0) {
      return BlueprintParser().parse(output.stdout).sortedBy { it.name }
    }

    return emptyList()
  }
}
