package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.Gson
import com.intellij.build.events.BuildEventsNls
import com.intellij.build.events.MessageEvent.Kind
import com.intellij.build.events.impl.MessageEventImpl
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.vfs.*
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.external.system.model.impl.ExternalFileConfigurationImpl
import com.jetbrains.cidr.external.system.model.impl.ExternalLanguageConfigurationImpl
import com.jetbrains.cidr.external.system.model.impl.ExternalResolveConfigurationBuilder
import com.jetbrains.cidr.lang.CLanguageKind
import com.jetbrains.cidr.lang.OCFileTypeHelpers
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompilerKind
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind
import com.jetbrains.cidr.lang.workspace.compiler.UnknownCompilerKind
import org.jetbrains.annotations.Nls
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.regex.Pattern

private val BUILD_SRC_FILTER_RE: Pattern = Regex("([+-])<([^>]*)>").toPattern()

private const val DEFAULT_SRC_FILTER = "+<*> -<.git/> -<.svn/>"

private typealias PlatformioSrcFilters = List<Pair<PathMatcher, Boolean>>

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

  internal fun parseResolveConfig(
    confBuilder: ExternalResolveConfigurationBuilder,
    jsonConfig: Map<String, Any>,
    srcFolder: VirtualFile,
    buildSrcFilter: PlatformioSrcFilters) {

    val scanFilesEventId = publishMessage(ClionEmbeddedPlatformioBundle.message("build.event.message.scanning.source.files"))

    val compilerKind: OCCompilerKind = if (jsonConfig["compiler_type"] == "gcc") GCCCompilerKind else UnknownCompilerKind

    fun extractCompilerSwitches(key: String, includeSwitches: List<String>, defineSwitches: List<String>): MutableList<String> {
      val switches = jsonConfig[key].asSafely<String>()?.split(" ") ?: emptyList()
      return switches.toMutableList().apply { addAll(includeSwitches); addAll(defineSwitches) }
    }

    val includeSwitches: List<String> = jsonConfig["includes"]
                                          .asSafely<Map<String, List<String>>>()
                                          ?.flatMap { it.value }
                                          ?.toSet()
                                          ?.map { "-I$it" } ?: emptyList()
    val defineSwitches: List<String> = jsonConfig["defines"]
                                         .asSafely<List<String>>()
                                         ?.map { "-D$it" } ?: emptyList()
    val cLanguageConfiguration = ExternalLanguageConfigurationImpl(
      languageKind = CLanguageKind.C, compilerKind = compilerKind,
      compilerExecutable = jsonConfig["cc_path"].asSafely<String>()?.let(::File),
      compilerSwitches = extractCompilerSwitches("cc_flags", includeSwitches, defineSwitches)
    )
    confBuilder.withLanguageConfiguration(cLanguageConfiguration)
    val cxxLanguageConfiguration = ExternalLanguageConfigurationImpl(
      languageKind = CLanguageKind.CPP, compilerKind = compilerKind,
      compilerExecutable = jsonConfig["cxx_path"].asSafely<String>()?.let(::File),
      compilerSwitches = extractCompilerSwitches("cxx_flags", includeSwitches, defineSwitches)
    )
    confBuilder.withLanguageConfiguration(cxxLanguageConfiguration)
    checkCancelled.run()
    val fileList = mutableListOf<String>()
    fun addSources(srcFolder: VirtualFile, buildSrcFilter: PlatformioSrcFilters) =
      scanSources(srcFolder, buildSrcFilter).forEach {
        if (OCFileTypeHelpers.isSourceFile(it.name)) {
          fileList.add(it.absolutePath)
          when (val kind = OCFileTypeHelpers.getLanguageKind(it.name)) {
            CLanguageKind.CPP -> confBuilder.withFileConfiguration(
              ExternalFileConfigurationImpl(it, kind, cxxLanguageConfiguration.compilerSwitches))
            CLanguageKind.C -> confBuilder.withFileConfiguration(
              ExternalFileConfigurationImpl(it, kind, cLanguageConfiguration.compilerSwitches))
          }
        }
      }

    addSources(srcFolder, buildSrcFilter)

    publishMessage(ClionEmbeddedPlatformioBundle.message("build.event.message.parsed.sources", fileList.size),
                   parentEventId = scanFilesEventId,
                   details = fileList.joinToString("\n"))
    val scanLibId = publishMessage(ClionEmbeddedPlatformioBundle.message("build.event.message.scanning.libraries"))
    val parsedLibraries = mutableListOf<String>()
    jsonConfig["libsource_dirs"].asSafely<List<String>>()?.forEach { libSource ->

      val librariesDir = VfsUtil.findFile(projectDir.toNioPath().resolve(Path.of(libSource)), true)
      librariesDir?.children?.filter(VirtualFile::isDirectory)?.forEach { libDir ->
        var libName = libDir.name
        try {
          val manifest = libDir.findFile("library.json")?.readText()?.let { Gson().fromJson<Map<String, Any>>(it, Map::class.java) }
          libName = manifest?.get("name").asSafely<String>() ?: libName
          parsedLibraries.add(libName)
          val manifestBuildPart = manifest?.get("build").asSafely<Map<String, String>>() ?: emptyMap()

          val libSrcFolder = manifestBuildPart["srcDir"]?.let { VfsUtil.findRelativeFile(libDir, it) } ?: libDir
          val libSrcFilter = createSrcFilter(manifestBuildPart["srcFilter"])
          addSources(libSrcFolder, libSrcFilter)
        }
        catch (e: Throwable) {
          LOG.warn(e)
          publishMessage(
            message = ClionEmbeddedPlatformioBundle.message("dialog.message.unable.to.parse.library", libName, e.localizedMessage),
            kind = Kind.WARNING)
        }
      }
    }
    publishMessage(message = ClionEmbeddedPlatformioBundle.message("build.event.message.parsed.libraries", parsedLibraries.size),
                   details = parsedLibraries.joinToString("\n"),
                   parentEventId = scanLibId)
  }

  private fun scanSources(srcFolder: VirtualFile, buildSrcFilter: PlatformioSrcFilters): List<File> {
    val srcFolderPath = srcFolder.toNioPath()
    val sourceFiles = mutableListOf<File>()
    val subfoldersFilter = mutableMapOf<Path, Boolean>()

    VfsUtil.processFilesRecursively(srcFolder)
    { virtualFile ->
      checkCancelled.run()
      val path = srcFolderPath.relativize(virtualFile.toNioPath())
      val inclusion: Boolean? = buildSrcFilter.findLast { it.first.matches(path) }?.second
      if (virtualFile.isFile) {
        when (inclusion) {
          true -> sourceFiles.add(VfsUtil.virtualToIoFile(virtualFile))
          false -> {}
          null -> if (subfoldersFilter[path.parent] == true) {
            sourceFiles.add(VfsUtil.virtualToIoFile(virtualFile))
          }
        }
      }
      else
        if (inclusion != null) {
          subfoldersFilter[path] = inclusion
        }
      true
    }
    return sourceFiles
  }

  @Suppress("UNCHECKED_CAST")
  /**
   * See https://docs.platformio.org/en/latest/projectconf/sections/env/options/build/build_src_filter.html#build-src-filter
   */
  internal fun gatherBuildSrcFilter(configMap: Map<String, List<Any>>,
                                    defaultEnvName: String?): List<Pair<PathMatcher, Boolean>> {
    val envConfigSection = configMap["env:${defaultEnvName}"]
    val buildSrcFilterClause = envConfigSection?.firstOrNull { (it as? List<Any>)?.getOrNull(0) == "build_src_filter" }
    var buildSrcFilterString: String? = null
    if (buildSrcFilterClause != null) {
      try {
        buildSrcFilterString = ((buildSrcFilterClause as List<Any>)[1] as List<String>)[0]
      }
      catch (e: RuntimeException) {
        throw ExternalSystemException(ClionEmbeddedPlatformioBundle.message("wrong.build.src.filter"))
      }
    }
    return createSrcFilter(buildSrcFilterString)
  }

  private fun createSrcFilter(buildSrcFilterString: String?): List<Pair<PathMatcher, Boolean>> {
    val matcher = BUILD_SRC_FILTER_RE.matcher(buildSrcFilterString ?: DEFAULT_SRC_FILTER)
    val result = mutableListOf<Pair<PathMatcher, Boolean>>()
    while (matcher.find()) {
      val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:${matcher.group(2)}")
      val includeExclude = "+" == matcher.group(1)
      result.add(pathMatcher to includeExclude)
    }
    return result
  }
}