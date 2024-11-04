// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.cli

import com.intellij.execution.filters.Filter
import com.intellij.javascript.CreateRunConfigurationUtil
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterUtil
import com.intellij.javascript.nodejs.npm.InstallNodeLocalDependenciesAction
import com.intellij.javascript.nodejs.packages.NodePackageUtil
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VuejsIcons
import java.io.File
import javax.swing.Icon


class NuxtProjectGenerator : NpmPackageProjectGenerator() {

  private val NUXT_CLI_PACKAGE_NAME = "nuxi@latest"
  private val CREATE_COMMAND = "init"

  override fun getId(): String {
    return VueBundle.message("vue.project.generator.nuxt.name")
  }

  override fun getName(): String {
    return VueBundle.message("vue.project.generator.nuxt.name")
  }

  override fun getDescription(): String {
    return VueBundle.message("vue.project.generator.nuxt.description")
  }

  override fun getIcon(): Icon {
    return VuejsIcons.Nuxt
  }


  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry) {}

  override fun generatorArgs(project: Project?, dir: VirtualFile, settings: Settings): Array<String> {
    val workingDir = dir.name
    return arrayOf(CREATE_COMMAND, workingDir, "--no-install")
  }

  override fun generatorArgs(project: Project, baseDir: VirtualFile): Array<String> {
    return emptyArray()
  }

  override fun filters(project: Project, baseDir: VirtualFile): Array<Filter> {
    return emptyArray()
  }

  override fun packageName(): String {
    return NUXT_CLI_PACKAGE_NAME
  }

  override fun presentablePackageName(): String {
    return VueBundle.message("vue.project.generator.nuxt.presentable.package.name")
  }

  override fun getNpxCommands(): List<NpxPackageDescriptor.NpxCommand> {
    return listOf(
      NpxPackageDescriptor.NpxCommand(NUXT_CLI_PACKAGE_NAME, NUXT_CLI_PACKAGE_NAME)
    )
  }

  override fun validateProjectPath(path: String): String? {
    val error = NodePackageUtil.validateNpmPackageName(PathUtil.getFileName(path))
    return error ?: super.validateProjectPath(path)
  }

  override fun onGettingSmartAfterProjectGeneration(project: Project, baseDir: VirtualFile) {
    super.onGettingSmartAfterProjectGeneration(project, baseDir)
    CreateRunConfigurationUtil.debugConfiguration(project, 3000)
    CreateRunConfigurationUtil.npmConfiguration(project, "dev")
  }

  override fun generateInTemp(): Boolean = true

  // trigger npm install
  override fun postInstall(project: Project,
                           baseDir: VirtualFile,
                           workingDir: File): Runnable {
    return Runnable {
      ApplicationManager.getApplication().executeOnPooledThread {
        val packageJson = baseDir.findChild(PackageJsonUtil.FILE_NAME)
        if (packageJson != null && NodeJsLocalInterpreterUtil.detectAllLocalInterpreters().isNotEmpty()) {
          InstallNodeLocalDependenciesAction.runAndShowConsole(project, packageJson)
        }
        super.postInstall(project, baseDir, workingDir).run()
      }
    }
  }
}
