// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.NodePackageVersionUtil
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.lang.Angular2LangUtil.ANGULAR_CLI_PACKAGE
import org.angularjs.lang.AngularJSLanguage
import java.io.File

private var myLogErrors: ThreadLocal<Boolean> = ThreadLocal.withInitial { true }
private val LOG: Logger = Logger.getInstance("#org.angular2.cli.SchematicsLoader")

fun doLoad(project: Project, cli: VirtualFile, includeHidden: Boolean, logErrors: Boolean): List<Schematic> {
  myLogErrors.set(logErrors)
  val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: return emptyList()
  val configurator: NodeCommandLineConfigurator
  try {
    configurator = NodeCommandLineConfigurator.find(interpreter)
  }
  catch (e: Exception) {
    LOG.error("Cannot load schematics", e)
    return emptyList()
  }

  var parse: Collection<Schematic> = emptyList()

  val schematicsInfoJson = loadSchematicsInfoJson(configurator, cli, includeHidden)
  if (schematicsInfoJson.isNotEmpty() && !schematicsInfoJson.startsWith("No schematics")) {
    try {
      parse = SchematicsJsonParser.parse(schematicsInfoJson)
    }
    catch (e: Exception) {
      LOG.error("Failed to parse schematics: " + e.message, e, Attachment("output", schematicsInfoJson))
    }
  }

  if (parse.isEmpty()) {
    val blueprintHelpOutput = loadBlueprintHelpOutput(configurator, cli)
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

private fun loadSchematicsInfoJson(configurator: NodeCommandLineConfigurator,
                                   cli: VirtualFile,
                                   includeHidden: Boolean): String {
  val directory = JSLanguageServiceUtil.getPluginDirectory(AngularJSLanguage::class.java, "ngCli")
  val utilityExe = "${directory}${File.separator}runner.js"
  val commandLine = GeneralCommandLine("", utilityExe, cli.path, "./schematicsInfoProvider.js")
  if (includeHidden)
    commandLine.addParameter("--includeHidden")
  configurator.configure(commandLine)
  return grabCommandOutput(commandLine, cli.path)
}

private fun loadBlueprintHelpOutput(configurator: NodeCommandLineConfigurator, cli: VirtualFile): String {
  val modules: MutableList<CompletionModuleInfo> = mutableListOf()
  NodeModuleSearchUtil.findModulesWithName(modules, ANGULAR_CLI_PACKAGE, cli, null)

  val module = modules.firstOrNull() ?: return ""
  val modulePath = module.virtualFile!!.path
  val moduleExe = "$modulePath${File.separator}bin${File.separator}ng"
  val isGt14 =
    NodePackageVersionUtil.getPackageVersion(modulePath)?.semVer?.isGreaterOrEqualThan(14, 0, 0) ?: false
  val commandLine =
    if (isGt14) GeneralCommandLine("", moduleExe, "generate", "--help") else GeneralCommandLine("", moduleExe, "help", "generate")
  configurator.configure(commandLine)
  return grabCommandOutput(commandLine, cli.path)
}

private fun grabCommandOutput(commandLine: GeneralCommandLine, workingDir: String?): String {
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

private fun shortenOutput(output: String): String {
  return StringUtil.shortenTextWithEllipsis(
    output.replace('\\', '/')
      .replace("(/[^()/:]+)+(/[^()/:]+)(/[^()/:]+)".toRegex(), "/...$1$2$3"),
    750, 0)
}

internal const val DEFAULT_OUTPUT: String = """

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

