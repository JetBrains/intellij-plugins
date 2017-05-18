package org.angularjs.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.gist.GistManager
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.IOUtil
import java.io.DataInput
import java.io.DataOutput
import java.io.File
import java.util.*

/**
 * @author Dennis.Ushakov
 */
object BlueprintsLoader {
  fun load(project: Project, cli: VirtualFile): Collection<Blueprint> = ApplicationManager.getApplication().runReadAction(
    Computable {
      ourGist.getFileData(project, cli)
    }
  )
}

private var ourGist = GistManager.getInstance().newVirtualFileGist("AngularBlueprints", 1, BlueprintsExternalizer(), { project, file -> doLoad(project, file) })

private class BlueprintsExternalizer : DataExternalizer<List<Blueprint>> {
  override fun save(out: DataOutput, value: List<Blueprint>?) {
    value!!
    out.writeInt(value.size)
    value.forEach {
      IOUtil.writeUTF(out, it.name)
      out.writeBoolean(it.description != null)
      if (it.description != null) IOUtil.writeUTF(out, it.description)
      out.writeInt(it.args.size)
      it.args.forEach { IOUtil.writeUTF(out, it) }
    }
  }

  override fun read(`in`: DataInput): List<Blueprint> {
    val size = `in`.readInt()
    val result = ArrayList<Blueprint>(size)
    for (i in 0..size - 1) {
      val name = IOUtil.readUTF(`in`)
      val description = if (`in`.readBoolean()) IOUtil.readUTF(`in`) else null
      val argsSize = `in`.readInt()
      val args = ArrayList<String>(argsSize)
      for (j in 0..argsSize - 1) {
        args.add(IOUtil.readUTF(`in`))
      }
      result.add(Blueprint(name, description, args))
    }
    return result
  }
}

private fun doLoad(project: Project, cli: VirtualFile): List<Blueprint> {
  val interpreter = NodeJsInterpreterManager.getInstance(project).default
  val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return emptyList()

  val modules:MutableList<CompletionModuleInfo> = mutableListOf()
  NodeModuleSearchUtil.findModulesWithName(modules, AngularCLIProjectGenerator.PACKAGE_NAME, cli, false, node)

  val module = modules.firstOrNull() ?: return emptyList()
  val moduleExe = "${module.virtualFile!!.path}${File.separator}bin${File.separator}ng"
  val commandLine = GeneralCommandLine(node.interpreterSystemDependentPath, moduleExe, "help", "generate")
  commandLine.withWorkDirectory(cli.path)
  val handler = CapturingProcessHandler(commandLine)
  val output = handler.runProcess()

  if (output.exitCode == 0) {
    return BlueprintParser().parse(output.stdout).sortedBy { it.name }
  }

  return emptyList()
}

fun findAngularCliFolder(project: Project, file: VirtualFile?): VirtualFile? {
  var current = file
  while (current != null) {
    if (current.isDirectory && current.findChild(AngularJSProjectConfigurator.ANGULAR_CLI_JSON) != null) return current
    current = current.parent
  }
  if (project.baseDir?.findChild(AngularJSProjectConfigurator.ANGULAR_CLI_JSON) != null) return project.baseDir
  return null
}

