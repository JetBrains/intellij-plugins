// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.cli

import com.intellij.execution.filters.Filter
import com.intellij.javascript.CreateRunConfigurationUtil
import com.intellij.javascript.nodejs.packages.NodePackageUtil
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VuejsIcons
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

  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) {}

  override fun generatorArgs(project: Project, dir: VirtualFile, settings: Settings): Array<String> {
    return arrayOf(
      CREATE_COMMAND,
      "--force",
      "--gitInit",
      /**
       * - Removes the user's ability to choose for himself
       * + Prevents an error if the selected manager is not installed
       *
       * It's suggested to remove this flag in the future
       * E.g. it can be removed after implementation of the possibility to choose
       * a package manager in UI
       */
      "--packageManager=npm",
    )
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
}
