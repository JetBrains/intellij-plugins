// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.javascript.typeEngine.JSServicePoweredTypeEngineUsageContext
import com.intellij.javascript.types.TSType
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.TypeScriptServiceEvaluationSupport
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptService
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequest
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.ConfigurePluginRequestArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.service.VueTSPluginLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptServiceProtocol
import org.jetbrains.vuejs.options.VueConfigurable
import org.jetbrains.vuejs.options.VueTSPluginVersion
import org.jetbrains.vuejs.options.getVueSettings

private fun getVueTypeScriptPlugin(
  version: VueTSPluginVersion,
): DownloadableTypeScriptServicePlugin =
  DownloadableTypeScriptServicePlugin(
    shortLabel = "Vue",
    activationRule = VueTSPluginLoaderFactory.getActivationRule(version),
  )

class VuePluginTypeScriptService(
  project: Project,
) : PluggableTypeScriptService(
  project = project,
  servicePlugin = getVueTypeScriptPlugin(getVueSettings(project).tsPluginVersion)
) {
  override fun createProtocol(
    tsServicePath: String,
  ): TypeScriptServiceStandardOutputProtocol {
    return VueTypeScriptServiceProtocol(
      project = project,
      settings = mySettings,
      eventConsumer = createEventConsumer(),
      serviceName = serviceName,
      tsServicePath = tsServicePath,
      servicePlugin = servicePlugin,
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
      itemIcon = VuejsIcons.Vue,
      statusBarIcon = VuejsIcons.Vue,
      settingsPageClass = VueConfigurable::class.java,
    )
  }

  override fun getProcessName(): String =
    "Vue + TypeScript"

  override fun isTypeEvaluationEnabled(): Boolean =
    getVueSettings(project).useTypesFromServer

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

    override fun isEnabledInUsageContext(
      usageContext: JSServicePoweredTypeEngineUsageContext,
    ): Boolean =
      true

    override val supportsTypeScriptInInjections: Boolean =
      true
  }
}