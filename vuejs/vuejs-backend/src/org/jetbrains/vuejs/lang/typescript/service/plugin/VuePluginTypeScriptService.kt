// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.JsonAdapter
import com.intellij.javascript.types.TSType
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.TypeScriptServiceEvaluationSupport
import com.intellij.lang.typescript.compiler.TypeScriptServiceQueueCommand
import com.intellij.lang.typescript.compiler.TypeScriptServiceResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequestArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptOpenEditorCommand
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil.getPreferableConfig
import com.intellij.openapi.components.service
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptPluginServiceWrapper
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptServiceProtocol
import org.jetbrains.vuejs.options.VueConfigurable
import org.jetbrains.vuejs.options.VueSettings
import java.lang.reflect.Type

private fun createServicePlugin(runtime: VueServiceRuntime): DownloadableTypeScriptServicePlugin {
  return DownloadableTypeScriptServicePlugin(
    shortLabel = "Vue",
    activationRule = VueTSPluginActivationRule(runtime),
  )
}

open class VuePluginTypeScriptService(
  project: Project,
  val runtime: VueServiceRuntime,
) : PluggableTypeScriptService(
  project = project,
  servicePlugin = createServicePlugin(runtime),
) {
  companion object {
    fun find(project: Project, runtime: VueServiceRuntime): VuePluginTypeScriptService? =
      project.service<VueTypeScriptPluginServiceWrapper>().findService(runtime)
  }

  override fun createProtocol(
    tsServicePath: String,
  ): TypeScriptServiceStandardOutputProtocol {
    return VueTypeScriptServiceProtocol(
      project = project,
      settings = mySettings,
      eventConsumer = createEventConsumer(),
      serviceName = serviceName,
      tsServicePath = tsServicePath,
      servicePlugin = this@VuePluginTypeScriptService.servicePlugin,
    )
  }

  override fun getInitialOpenCommands(): List<JSLanguageServiceSimpleCommand> {
    return listOf(createConfigureCommand()) + super.getInitialOpenCommands()
  }

  private fun createConfigureCommand(): JSLanguageServiceSimpleCommand {
    return ConfigurePluginRequest(
      ConfigurePluginRequestArguments(
        pluginName = "typescript-vue-plugin",
        configuration = emptyMap(),
      ))
  }

  override fun isAcceptableForHighlighting(file: PsiFile): Boolean {
    return super.isAcceptableForHighlighting(file)
           || file.isVueFile
  }

  override fun isAcceptableNonTsFile(
    project: Project,
    service: TypeScriptConfigService,
    virtualFile: VirtualFile,
  ): Boolean {
    return super.isAcceptableNonTsFile(project, service, virtualFile)
           || virtualFile.isVueFile
  }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is VueJSLanguage
           || file.language is VueTSLanguage
  }

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem {
    return TypeScriptServiceWidgetItem(
      service = this,
      currentFile = currentFile,
      itemIcon = VuejsIcons.TypeScriptVue,
      statusBarIcon = VuejsIcons.TypeScriptVueStatusBar,
      settingsPageClass = VueConfigurable::class.java,
    )
  }

  override fun createLSCache(): TypeScriptLanguageServiceCache {
    return object : TypeScriptLanguageServiceCache(project) {
      override fun createOpenCommand(
        file: VirtualFile,
        info: LastUpdateInfo,
        text: CharSequence?,
        projectFileName: String?,
        timestamp: Long,
      ): TypeScriptOpenEditorCommand {
        return TypeScriptOpenEditorCommand(
          file,
          timestamp,
          info.myContentLength,
          info.myLineCount,
          info.myLastLineStartOffset,
          text,
          projectFileName,
          getProjectRootPath(file),
        )
      }
    }
  }

  override fun createOpenCommand(
    file: VirtualFile,
    timestamp: Long,
    contentLength: Long,
    lineCount: Int,
    lastLineStartOffset: Int,
    content: CharSequence,
  ): TypeScriptOpenEditorCommand {
    return TypeScriptOpenEditorCommand(
      file,
      timestamp,
      contentLength,
      lineCount,
      lastLineStartOffset,
      content,
      null,
      getProjectRootPath(file),
    )
  }

  /**
   * In theory, should be suitable for projects w/o tsconfig, so return our base dir.
   * Works together with `useInferredProjectPerProjectRoot`.
   */
  private fun getProjectRootPath(file: VirtualFile): @NonNls String? {
    val psiFile = PsiManager.getInstance(project).findFile(file)
    val configFile = getPreferableConfig(psiFile, false)?.configFile
                     ?: project.getBaseDirectories().firstOrNull { VfsUtilCore.isAncestor(it, file, false) }

    return configFile?.path
  }

  @JsonAdapter(TSServerRequestArgsAdapter::class)
  class TSServerRequestArgs(internal val body: JsonElement?) : JSLanguageServiceObject {
    override fun toString(): String = body?.toString() ?: "null"
  }

  private class TSServerRequestArgsAdapter : JsonSerializer<TSServerRequestArgs> {
    override fun serialize(
      src: TSServerRequestArgs?,
      typeOfSrc: Type?,
      context: JsonSerializationContext?,
    ): JsonElement? = src?.body
  }

  internal suspend fun executeTsserverRequest(
    command: String,
    args: JsonElement?,
  ): JsonElement? {
    val tsCommand = VueTsserverCommand(command, args)
    return executeCommandOnRequestQueue(tsCommand)
  }

  private class VueTsserverCommand(
    command: String,
    args: JsonElement?,
  ) : TypeScriptServiceQueueCommand<TSServerRequestArgs, JsonElement>(
    command = command,
    args = TSServerRequestArgs(args),
  ) {
    override fun processResult(result: TypeScriptServiceResult): JsonElement? {
      return result.body
    }
  }

  override fun getProcessName(): String =
    "Vue + TypeScript"

  override fun isTypeEvaluationEnabled(): Boolean =
    VueSettings.instance(project).useTypesFromServer

  override val typeEvaluationSupport: TypeScriptServiceEvaluationSupport =
    VueCompilerServiceEvaluationSupport(project)

  override fun supportsTypeEvaluation(
    virtualFile: VirtualFile,
    element: PsiElement,
  ): Boolean =
    virtualFile.isVueFile
    || super.supportsTypeEvaluation(virtualFile, element)

  private inner class VueCompilerServiceEvaluationSupport(
    project: Project,
  ) : TypeScriptCompilerServiceEvaluationSupport(project) {

    override fun getElementType(
      element: PsiElement,
      typeRequestKind: TypeScriptTypeRequestKind,
      virtualFile: VirtualFile,
      projectFile: VirtualFile?,
    ): TSType? {
      // copied from `Angular2CompilerServiceEvaluationSupport`
      if (element !is JSElement && element.parent !is JSElement) {
        return null
      }

      return super.getElementType(element, typeRequestKind, virtualFile, projectFile)
    }

    override val supportsTypeScriptInInjections: Boolean =
      true
  }
}