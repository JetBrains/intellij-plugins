// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.google.gson.GsonBuilder
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.gist.GistManager
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.IOUtil
import org.angularjs.lang.AngularJSLanguage
import java.io.DataInput
import java.io.DataOutput
import java.io.File
import java.io.IOException

object SchematicsLoader {
  fun load(project: Project,
           cli: VirtualFile): Collection<Schematic> = load(project, cli, false, true)

  fun load(project: Project,
           cli: VirtualFile,
           includeHidden: Boolean,
           logErrors: Boolean): Collection<Schematic> = ApplicationManager.getApplication().runReadAction(
    Computable {
      try {
        myLogErrors.set(logErrors)
        (if (includeHidden) ourGistHiddenList else ourGist).getFileData(project, cli)
      }
      finally {
        myLogErrors.set(true)
      }
    }
  )
}

private var myLogErrors: ThreadLocal<Boolean> = ThreadLocal.withInitial { true }

private var ourGist = GistManager.getInstance().newVirtualFileGist(
  "AngularBlueprints", 3, SchematicsExternalizer()
) { project, file -> doLoad(project, file, false) }
private var ourGistHiddenList = GistManager.getInstance().newVirtualFileGist(
  "AngularSchematicsHidden", 3, SchematicsExternalizer()
) { project, file -> doLoad(project, file, true) }

private val LOG: Logger = Logger.getInstance("#org.angular2.cli.SchematicsLoader")

private class SchematicsExternalizer : DataExternalizer<List<Schematic>> {
  override fun save(out: DataOutput, value: List<Schematic>?) {
    value!!
    IOUtil.writeUTF(out, GsonBuilder().create().toJson(value))
  }

  override fun read(`in`: DataInput): List<Schematic> {
    val json = IOUtil.readUTF(`in`)
    val result: List<Schematic>
    try {
      result = SchematicsJsonParser.parse(json)
    }
    catch (e: Throwable) {
      throw IOException("Failed to load gist: " + e.message, e)
    }
    if (result == null) {
      throw IOException()
    }
    return result
  }
}

private fun doLoad(project: Project, cli: VirtualFile, includeHidden: Boolean): List<Schematic> {
  val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
  val node = NodeJsLocalInterpreter.tryCast(interpreter) ?: return emptyList()
  if (!node.isValid) return emptyList()

  var parse: Collection<Schematic> = emptyList()

  val schematicsInfoJson = loadSchematicsInfoJson(node, cli, includeHidden)
  if (schematicsInfoJson.isNotEmpty() && !schematicsInfoJson.startsWith("No schematics")) {
    try {
      parse = SchematicsJsonParser.parse(schematicsInfoJson)
    }
    catch (e: Exception) {
      LOG.error("Failed to parse schematics: " + e.message, e, Attachment("output", schematicsInfoJson))
    }
  }

  if (parse.isEmpty()) {
    val blueprintHelpOutput = loadBlueprintHelpOutput(node, cli)
    if (blueprintHelpOutput.isNotEmpty()) {
      try {
        parse = BlueprintParser().parse(blueprintHelpOutput)
      }
      catch (e: Exception) {
        LOG.error("Failed to parse blueprints: " + e.message, e, Attachment("output", blueprintHelpOutput))
      }
    }
  }

  if (parse.isEmpty()) {
    parse = BlueprintParser().parse(DEFAULT_OUTPUT)
  }

  return parse.sortedBy { it.name }
}

fun loadSchematicsInfoJson(node: NodeJsLocalInterpreter,
                           cli: VirtualFile,
                           includeHidden: Boolean): String {
  val directory = JSLanguageServiceUtil.getPluginDirectory(AngularJSLanguage::class.java, "ngCli")
  val utilityExe = "${directory}${File.separator}runner.js"
  val commandLine = GeneralCommandLine(node.interpreterSystemDependentPath, utilityExe, cli.path, "./schematicsInfoProvider.js")
  if (includeHidden)
    commandLine.addParameter("--includeHidden")
  return grabCommandOutput(commandLine, cli.path)
}

fun loadBlueprintHelpOutput(node: NodeJsLocalInterpreter, cli: VirtualFile): String {
  val modules: MutableList<CompletionModuleInfo> = mutableListOf()
  NodeModuleSearchUtil.findModulesWithName(modules, AngularCLIProjectGenerator.PACKAGE_NAME, cli, false, node)

  val module = modules.firstOrNull() ?: return ""
  val moduleExe = "${module.virtualFile!!.path}${File.separator}bin${File.separator}ng"
  return grabCommandOutput(GeneralCommandLine(node.interpreterSystemDependentPath, moduleExe, "help", "generate"),
                           cli.path)
}

fun grabCommandOutput(commandLine: GeneralCommandLine, workingDir: String?): String {
  if (workingDir != null) {
    commandLine.withWorkDirectory(workingDir)
  }
  val handler = CapturingProcessHandler(commandLine)
  val output = handler.runProcess()

  if (output.exitCode == 0) {
    if (output.stderr.trim().isNotEmpty()) {
      if (myLogErrors.get()) {
        LOG.error("Error while loading schematics info.\n"
                  + shortenOutput(output.stderr),
                  Attachment("err-output", output.stderr))
      }
      else {
        LOG.info("Error while loading schematics info.\n"
                 + shortenOutput(output.stderr))
      }
    }
    return output.stdout
  }
  else if (myLogErrors.get()) {
    LOG.error("Failed to load schematics info.\n"
              + shortenOutput(output.stderr),
              Attachment("err-output", output.stderr),
              Attachment("std-output", output.stdout))
  }
  else {
    LOG.info("Error while loading schematics info.\n"
             + shortenOutput(output.stderr))
  }
  return ""
}

fun shortenOutput(output: String): String {
  return StringUtil.shortenTextWithEllipsis(
    output.replace('\\', '/')
      .replace("(/[^()/:]+)+(/[^()/:]+)(/[^()/:]+)".toRegex(), "/...$1$2$3"),
    750, 0)
}

const val DEFAULT_OUTPUT: String = """

  Available blueprints:
    class <name> <options...>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    component <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --inline-template (Boolean) Specifies if the template will be in the ts file.
        aliases: -it, --inlineTemplate
      --inline-style (Boolean) Specifies if the style will be in the ts file.
        aliases: -is, --inlineStyle
      --prefix (String) (Default: null) Specifies whether to use the prefix.
        aliases: --prefix <value>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --view-encapsulation (String) Specifies the view encapsulation strategy.
        aliases: -ve <value>, --viewEncapsulation <value>
      --change-detection (String) Specifies the change detection strategy.
        aliases: -cd <value>, --changeDetection <value>
      --skip-import (Boolean) (Default: false) Allows for skipping the module import.
        aliases: --skipImport
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --export (Boolean) (Default: false) Specifies if declaring module exports the component.
        aliases: --export
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    directive <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --prefix (String) (Default: null) Specifies whether to use the prefix.
        aliases: --prefix <value>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --skip-import (Boolean) (Default: false) Allows for skipping the module import.
        aliases: --skipImport
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --export (Boolean) (Default: false) Specifies if declaring module exports the component.
        aliases: --export
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    enum <name> <options...>
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    guard <name> <options...>
      --flat (Boolean) Indicate if a dir is created.
        aliases: -flat
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
    interface <interface-type> <options...>
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    module <name> <options...>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --routing (Boolean) (Default: false) Specifies if a routing module file should be generated.
        aliases: --routing
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    pipe <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --skip-import (Boolean) (Default: false) Allows for skipping the module import.
        aliases: --skipImport
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --export (Boolean) (Default: false) Specifies if declaring module exports the pipe.
        aliases: --export
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    service <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>

ng generate <blueprint> <options...>
  Generates new code from blueprints.
  aliases: g
  --dry-run (Boolean) (Default: false) Run through without making any changes.
    aliases: -d, --dryRun
  --verbose (Boolean) (Default: false) Adds more details to output logging.
    aliases: -v, --verbose

    """

