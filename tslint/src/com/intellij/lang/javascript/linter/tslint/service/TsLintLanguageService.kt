// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.service

import com.google.gson.*
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.AutodetectLinterPackage
import com.intellij.lang.javascript.linter.tslint.TsLintBundle
import com.intellij.lang.javascript.linter.tslint.TslintUtil
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration
import com.intellij.lang.javascript.linter.tslint.config.TsLintState
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputJsonParser
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError
import com.intellij.lang.javascript.service.*
import com.intellij.lang.javascript.service.protocol.*
import com.intellij.lang.javascript.service.protocol.LocalFilePath.Companion.create
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.text.SemVer
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction
import java.util.function.Consumer

class TsLintLanguageService(
  project: Project,
  val nodePackage: NodePackage,
  private val myWorkingDirectory: VirtualFile
) : JSLanguageServiceBase(project) {

  fun highlight(
    virtualFile: VirtualFile,
    config: VirtualFile?,
    content: String?,
    state: TsLintState
  ): CompletableFuture<MutableList<TsLinterError?>?>? {
    return createHighlightFuture(virtualFile, config, state,
                                 BiFunction { filePath: LocalFilePath?, configPath: LocalFilePath? ->
                                   GetErrorsCommand(filePath, configPath, content ?: "")
                                 })
  }

  fun highlightAndFix(virtualFile: VirtualFile, state: TsLintState): CompletableFuture<MutableList<TsLinterError?>?>? {
    val config = TslintUtil.getConfig(state, myProject, virtualFile)
    //doesn't pass content (file should be saved before)
    return createHighlightFuture(virtualFile, config, state, BiFunction { filePath: LocalFilePath?, configPath: LocalFilePath? ->
      FixErrorsCommand(filePath, configPath)
    })
  }

  private fun createHighlightFuture(
    virtualFile: VirtualFile,
    config: VirtualFile?,
    state: TsLintState,
    commandProvider: BiFunction<LocalFilePath?, LocalFilePath?, BaseCommand>
  ): CompletableFuture<MutableList<TsLinterError?>?>? {
    val configFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(config)
    if (configFilePath == null) {
      if (state.nodePackageRef === AutodetectLinterPackage.INSTANCE) {
        return CompletableFuture.completedFuture<MutableList<TsLinterError?>?>(ContainerUtil.emptyList<TsLinterError?>())
      }
      return CompletableFuture.completedFuture<MutableList<TsLinterError?>?>(mutableListOf(TsLinterError.createGlobalError(
        TsLintBundle.message("tslint.inspection.message.config.file.was.not.found"))))
    }
    val path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile)
    if (path == null) {
      return null
    }

    val process = process
    if (process == null) {
      return CompletableFuture.completedFuture<MutableList<TsLinterError?>?>(mutableListOf<TsLinterError?>(
        TsLinterError.createGlobalError(JSLanguageServiceUtil.getLanguageServiceCreationError(this))))
    }

    //doesn't pass content (file should be saved before)
    val command = commandProvider.apply(LocalFilePath.create(path),
                                        LocalFilePath.create(configFilePath))
    return cs.async {
      val answer = process.execute(command)?.answer ?: return@async null
      parseResults(answer, path, JSLanguageServiceUtil.getGson(this@TsLintLanguageService))
    }.asCompletableFuture()
  }

  override fun createLanguageServiceQueueBlocking(): JSLanguageServiceQueue {
    return JSLanguageServiceQueueImpl(
      myProject,
      Protocol(this.nodePackage, myWorkingDirectory, myProject),
      null,
      myDefaultReporter,
      JSLanguageServiceDefaultCacheData())
  }

  private abstract class BaseCommand protected constructor(
    var filePath: LocalFilePath?,
    var configPath: LocalFilePath?
  ) : JSLanguageServiceCommand, JSLanguageServiceSimpleCommand, JSLanguageServiceObject {
    override fun toSerializableObject(): JSLanguageServiceObject {
      return this
    }
  }

  private class GetErrorsCommand(filePath: LocalFilePath?, configPath: LocalFilePath?, var content: String?) : BaseCommand(filePath,
                                                                                                                           configPath) {
    override val command: String
      get() = "GetErrors"
  }

  private class FixErrorsCommand(filePath: LocalFilePath?, configPath: LocalFilePath?) : BaseCommand(filePath, configPath) {
    override val command: String
      get() = "FixErrors"
  }

  private class Protocol(
    private val myNodePackage: NodePackage,
    private val myWorkingDirectory: VirtualFile,
    project: Project
  ) : JSLanguageServiceNodeStdProtocolBase("tslint", project, Consumer { o: Any? -> }) {
    override val workingDirectory: String?
      get() = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(myWorkingDirectory)

    override fun createState(): JSLanguageServiceInitialState {
      val result = InitialState()
      val extendedState = TsLintConfiguration.getInstance(myProject).extendedState
      if (myNodePackage is YarnPnpNodePackage) {
        result.tslintPackagePath = LocalFilePath.create(myNodePackage.name)
        val packageJsonPath = myNodePackage.getPackageJsonPath(myProject)
        checkNotNull(packageJsonPath) { "Cannot find package.json path for " + myNodePackage }
        result.packageJsonPath = LocalFilePath.create(FileUtil.toSystemDependentName(packageJsonPath))
      }
      else {
        result.tslintPackagePath = LocalFilePath.create(myNodePackage.systemDependentPath)
      }
      result.additionalRootDirectory = create(extendedState.getState().rulesDirectory)
      result.pluginName = "tslint"
      result.pluginPath = LocalFilePath.create(
        JSLanguageServiceUtil.getPluginDirectory(javaClass, "js/languageService/tslint-plugin-provider.js")!!.absolutePath)
      return result
    }

    override fun addNodeProcessAdditionalArguments(targetRun: NodeTargetRun) {
      super.addNodeProcessAdditionalArguments(targetRun)
      targetRun.path(JSLanguageServiceUtil.getPluginDirectory(javaClass, "js")!!.absolutePath)
    }

    override fun dispose() {
    }
  }

  private class InitialState : JSLanguageServiceInitialState() {
    var tslintPackagePath: LocalFilePath? = null

    /**
     * Path to package.json declaring tslint dependency.
     * Allows requiring dependencies in proper context. Used by Yarn PnP.
     */
    var packageJsonPath: LocalFilePath? = null
    var additionalRootDirectory: LocalFilePath? = null
  }

  companion object {
    private val LOG = Logger.getInstance(TsLintLanguageService::class.java)

    private fun parseResults(answer: JSLanguageServiceAnswer, path: String, gson: Gson): MutableList<TsLinterError?>? {
      val element = answer.element
      val error = element.get("error")
      if (error != null) {
        return mutableListOf(TsLinterError.createGlobalError(error.asString)) //NON-NLS
      }
      val body: JsonElement? = parseBody(element)
      if (body == null) return null
      val version = element.get("version").asString
      val tsLintVersion = SemVer.parseFromText(version)
      val isZeroBased = TsLintOutputJsonParser.isVersionZeroBased(tsLintVersion)
      val parser = TsLintOutputJsonParser(path, body, isZeroBased, gson)
      return ArrayList(parser.errors)
    }

    private fun parseBody(element: JsonObject): JsonElement? {
      val body = element.get("body")
      if (body == null) {
        //we do not currently treat empty body as error in protocol
        return null
      }
      else {
        if (body.isJsonPrimitive && body.getAsJsonPrimitive().isString) {
          val bodyContent = StringUtil.unquoteString(body.getAsJsonPrimitive().getAsString())
          if (bodyContent.isNotBlank()) {
            try {
              return JsonParser.parseString(bodyContent)
            }
            catch (e: JsonParseException) {
              LOG.info(String.format("Problem parsing body: '%s'\n%s", body, e.message), e)
            }
          }
        }
        else {
          LOG.info(String.format("Error body type, should be a string with json inside. Body:'%s'", body.asString))
        }
      }
      return null
    }
  }
}
