package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.Gson
import com.intellij.build.events.BuildEventsNls
import com.intellij.build.events.MessageEvent.Kind
import com.intellij.build.events.impl.MessageEventImpl
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.vfs.*
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.project.command.*
import com.jetbrains.cidr.cpp.toolchains.CPPCompilerSwitchesUtil
import com.jetbrains.cidr.external.system.model.ExternalLanguageConfiguration
import com.jetbrains.cidr.external.system.model.impl.ExternalResolveConfigurationBuilder
import com.jetbrains.cidr.lang.OCFileTypeHelpers
import com.jetbrains.cidr.lang.toolchains.CidrSwitchBuilder
import org.jetbrains.annotations.Nls
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

internal class PlatformioFileScanner(private val projectDir: VirtualFile,
                                     private val listener: ExternalSystemTaskNotificationListener,
                                     private val id: ExternalSystemTaskId,
                                     private val checkCancelled: Runnable) {


  private fun publishMessage(@BuildEventsNls.Message message: String,
                             kind: Kind = Kind.SIMPLE,
                             @BuildEventsNls.Message details: String? = null,
                             parentEventId: Any = id): Any {
    val messageEvent = MessageEventImpl(parentEventId, kind, null, message, details)
    listener.onStatusChange(ExternalSystemBuildEvent(id, messageEvent))
    return messageEvent.id
  }

  internal fun findConfigs(platformioSection: Map<String, Any>): Set<String> {
    val result = mutableSetOf<String>()
    projectDir.findFile(PlatformioFileType.FILE_NAME)?.apply { result.add(this.path) }
    val fileSystem = FileSystems.getDefault()
    val extraConfigMatchers: MutableList<PathMatcher> = mutableListOf()
    platformioSection["extra_configs"]
      ?.asSafely<List<String>>()
      ?.forEach {
        try {
          checkCancelled.run()
          extraConfigMatchers.add(fileSystem.getPathMatcher("glob:$it"))
        }
        catch (e: RuntimeException) {
          @Nls val message = ClionEmbeddedPlatformioBundle.message("invalid.extra.configs.element", it)
          LOG.warn(message, e)
          publishMessage(message, Kind.ERROR)
        }
      }
    val projectNioPath = projectDir.toNioPath()
    if (!extraConfigMatchers.isEmpty()) {
      VfsUtil.processFileRecursivelyWithoutIgnored(projectDir) { file ->
        checkCancelled.run()
        val relativePath = projectNioPath.relativize(file.toNioPath())
        if (extraConfigMatchers.any { matcher -> matcher.matches(relativePath) }) {
          result.add(file.path)
        }
        true
      }
    }
    return result
  }

  internal data class CompDbEntry(val file: String, val command: String, val directory: String)

  internal fun scanSources(
    compDbJson: List<CompDbEntry>,
    workspace: PlatformioWorkspace,
    languageConfigurations: List<ExternalLanguageConfiguration>,
    confBuilder: ExternalResolveConfigurationBuilder
  ) {

    val scanFilesEventId = publishMessage(ClionEmbeddedPlatformioBundle.message("build.event.message.scanning.source.files"))
    val fileList = mutableListOf<String>()
    val environment = workspace.environment
    val commandParser = CPPCompilationCommandParser(environment)
    val commandConverter = CPPCompilationCommandConverter(environment, workspace.project)

    compDbJson.mapNotNull {
      val command = it.command
      val file = it.file
      val directory = it.directory

      fileList.add(file)
      when (val parseResult = commandParser.parse(CPPCompilationCommand(directory, file, command, emptyList()))) {
        is CPPCommandParserResult.SuccessCommandObject -> parseResult.commandObject
        is CantFindCompilerExecutable -> {
          val fallback = languageConfigurations.find { conf ->
            conf.languageKind == OCFileTypeHelpers.getLanguageKind(file)
          }
          if (fallback == null) { return@mapNotNull null }
          val dir = File(directory)
          CPPCommandObject(
            dir,
            dir.resolve(File(file)),
            file,
            fallback.compilerExecutable!!,
            CidrSwitchBuilder().parseAndAdd(command, CPPCompilerSwitchesUtil.getFlagsFormat(environment)).args.drop(1).filter { f -> f != file },
          )
        }
        else -> {
          // If the parse failed, try to fall back to project-wide configuration
          val fallback = languageConfigurations.find { conf ->
            conf.languageKind == OCFileTypeHelpers.getLanguageKind(file)
          }
          val dir = File(directory)
          if (fallback == null) { return@mapNotNull null }
          CPPCommandObject(
            dir,
            dir.resolve(File(file)),
            file,
            fallback.compilerExecutable!!,
            fallback.compilerSwitches ?: emptyList(),
          )
        }
      }
    }.flatMap {
      commandConverter.convert(it).fileConfigurations
    }.forEach {
      confBuilder.withFileConfiguration(it)
    }

    publishMessage(ClionEmbeddedPlatformioBundle.message("build.event.message.parsed.sources", fileList.size),
                   parentEventId = scanFilesEventId,
                   details = fileList.joinToString("\n"))
  }

  internal fun scanLibraries(
    jsonConfig: Map<String, Any>,
  ): MutableMap<String, String> {

    val scanLibId = publishMessage(ClionEmbeddedPlatformioBundle.message("build.event.message.scanning.libraries"))
    val parsedLibPaths = mutableMapOf<String, String>()

    // Scans libraries, making note of their names
    jsonConfig["libsource_dirs"].asSafely<List<String>>()?.forEach { libSource ->
      val librariesDir = VfsUtil.findFile(projectDir.toNioPath().resolve(Path.of(libSource)), true)
      librariesDir?.children?.filter(VirtualFile::isDirectory)?.forEach { libDir ->
        val libName = libDir.name
        try {
          val manifest = libDir.findFile("library.json")?.readText()?.let { Gson().fromJson<Map<String, Any>>(it, Map::class.java) }
          parsedLibPaths[libDir.path] = manifest?.get("name").asSafely<String>() ?: libName
        }
        catch (e: Throwable) {
          LOG.warn(e)
          publishMessage(
            message = ClionEmbeddedPlatformioBundle.message("dialog.message.unable.to.parse.library", libName, e.localizedMessage),
            kind = Kind.WARNING)
        }
      }
    }
    publishMessage(message = ClionEmbeddedPlatformioBundle.message("build.event.message.parsed.libraries", parsedLibPaths.size),
                   details = parsedLibPaths.values.joinToString("\n"),
                   parentEventId = scanLibId)
    return parsedLibPaths
  }
}