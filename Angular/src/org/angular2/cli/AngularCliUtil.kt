// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.javascript.JSRunConfigurationBuilder
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.NodePackageVersionUtil
import com.intellij.javascript.nodejs.packageJson.notification.PackageJsonGetDependenciesAction
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import org.angular2.cli.config.AngularConfig
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CLI_PACKAGE
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

object AngularCliUtil {
  private const val NOTIFICATION_GROUP_ID = "Angular CLI"

  @NonNls
  private val ANGULAR_JSON_NAMES = listOf("angular.json", ".angular-cli.json", "angular-cli.json")

  @NonNls
  private val NG_CLI_DEFAULT_ADDRESS = "http://localhost:4200"

  @JvmStatic
  fun findCliJson(dir: VirtualFile?): VirtualFile? {
    if (dir == null || !dir.isValid) return null
    for (name in ANGULAR_JSON_NAMES) {
      val cliJson = dir.findChild(name)
      if (cliJson != null) {
        return cliJson
      }
    }
    return null
  }

  /**
   * Locates folder in which angular.json from which user would run Angular CLI
   */
  @JvmStatic
  fun findAngularCliFolder(project: Project, file: VirtualFile?): VirtualFile? {
    var current = file
    while (current != null) {
      if (current.isDirectory && findCliJson(current) != null) return current
      current = current.parent
    }
    @Suppress("DEPRECATION")
    return if (findCliJson(project.baseDir) != null) {
      project.baseDir
    }
    else null
  }

  @JvmStatic
  fun hasAngularCLIPackageInstalled(cli: VirtualFile): Boolean {
    return findAngularCliModuleInfo(cli) != null
  }

  @JvmStatic
  fun getAngularCliPackageVersion(cli: VirtualFile): SemVer? {
    val moduleInfo = findAngularCliModuleInfo(cli) ?: return null

    val nodePackageVersion = NodePackageVersionUtil.getPackageVersion(moduleInfo.virtualFile!!.path)
    return nodePackageVersion?.semVer
  }


  private fun findAngularCliModuleInfo(cli: VirtualFile): CompletionModuleInfo? {
    val modules = ArrayList<CompletionModuleInfo>()
    NodeModuleSearchUtil.findModulesWithName(modules, ANGULAR_CLI_PACKAGE, cli, null)
    val moduleInfo = modules.firstOrNull()
    return if (moduleInfo != null && moduleInfo.virtualFile != null) moduleInfo else null
  }

  @JvmStatic
  fun isAngularJsonFile(fileName: String): Boolean {
    return ANGULAR_JSON_NAMES.contains(fileName)
  }

  @JvmStatic
  fun notifyAngularCliNotInstalled(project: Project, cliFolder: VirtualFile, @Nls message: String) {
    val packageJson = PackageJsonUtil.findChildPackageJsonFile(cliFolder)
    val notification = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)
      .createNotification(message, Angular2Bundle.message("angular.notify.cli.required-package-not-installed"), NotificationType.WARNING)
    if (packageJson != null) {
      notification.addAction(PackageJsonGetDependenciesAction(project, packageJson, notification))
    }
    notification.notify(project)
  }

  @JvmStatic
  fun createRunConfigurations(project: Project, baseDir: VirtualFile) {
    ApplicationManager.getApplication().executeOnPooledThread {
      DumbService.getInstance(project).runReadActionInSmartMode {
        if (project.isDisposed) {
          return@runReadActionInSmartMode
        }

        val packageJsonPath = getPackageJson(baseDir)
                              ?: return@runReadActionInSmartMode
        val config: AngularConfig = AngularConfigProvider.getAngularConfig(project, baseDir)
                                    ?: return@runReadActionInSmartMode

        createKarmaConfigurations(project, config)
        createProtractorConfigurations(project, config)

        val nameSuffix = if (ModuleManager.getInstance(project).modules.size > 1)
          " (" + baseDir.name + ")"
        else
          ""

        createJSDebugConfiguration(project, "Angular Application$nameSuffix", NG_CLI_DEFAULT_ADDRESS)
        RunManager.getInstance(project).selectedConfiguration = createNpmConfiguration(
          project, packageJsonPath, "Angular CLI Server$nameSuffix", "start")
      }
    }
  }

  private fun getPackageJson(baseDir: VirtualFile): String? {
    val pkg = PackageJsonUtil.findChildPackageJsonFile(baseDir)
    return pkg?.path
  }

  private fun createJSDebugConfiguration(project: Project, @NonNls label: String, url: String) {
    createIfNoSimilar("jsdebug", project, label, null, null, mapOf("uri" to url))
  }

  private fun createNpmConfiguration(project: Project,
                                     packageJsonPath: String,
                                     @NonNls label: String,
                                     scriptName: String): RunnerAndConfigurationSettings? {
    return createIfNoSimilar("npm", project, label, null, packageJsonPath,
                             mapOf("run-script" to scriptName))
  }

  private fun createKarmaConfigurations(project: Project,
                                        config: AngularConfig) {
    config.projects.forEach { ngProject ->
      val karmaFile = ngProject.karmaConfigFile
      val rootDir = ngProject.rootDir
      if (karmaFile != null && rootDir != null)
        createIfNoSimilar("karma", project, "Tests (" + ngProject.name + ")",
                          rootDir, karmaFile.path,
                          emptyMap())
    }
  }

  private fun createProtractorConfigurations(project: Project,
                                             config: AngularConfig) {
    config.projects.forEach { ngProject ->
      val protractorConfigFile = ngProject.protractorConfigFile
      val rootDir = ngProject.rootDir
      if (protractorConfigFile != null && rootDir != null)
        createIfNoSimilar("protractor", project, "E2E Tests (" + ngProject.name + ")",
                          rootDir, protractorConfigFile.path,
                          emptyMap())
    }
  }

  private fun createIfNoSimilar(@NonNls rcType: String,
                                project: Project,
                                @NonNls label: String,
                                baseDir: VirtualFile?,
                                configPath: String?,
                                options: Map<String, Any>): RunnerAndConfigurationSettings? {
    return JSRunConfigurationBuilder.getForName(rcType, project)?.let { builder ->
      builder.findSimilarRunConfiguration(baseDir, configPath, options)
      ?: builder.createRunConfiguration(label, baseDir, configPath, options)
    }
  }

  @JvmStatic
  fun getCliParamText(name: String, cliVersion: SemVer): String {
    val toKebabCase = cliVersion.isGreaterOrEqualThan(12, 0, 0)
    val paramText = if (toKebabCase)
      JSStringUtil.toKebabCase(name, true, true, false)
    else
      name
    return "--$paramText"
  }
}
