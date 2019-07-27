// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:JvmName("AngularCliConfigLoader")

package org.angular2.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import one.util.streamex.StreamEx
import org.angular2.cli.config.AngularConfig
import org.angular2.cli.config.AngularConfigProvider

@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@Deprecated("Use AngularConfigProvider instead")
fun load(project: Project, context: VirtualFile): AngularCliConfig {
  return AngularConfigProvider.getAngularConfig(project, context)
           ?.let { AngularCliJsonFileConfig(it) }
         ?: AngularCliEmptyConfig()
}

@Deprecated("Use AngularConfigProvider and AngularConfig instead")
interface AngularCliConfig {

  fun getIndexHtmlFile(): VirtualFile?

  fun getGlobalStyleSheets(): Collection<VirtualFile>

  /**
   * @return root folders according to apps -> root in .angular-cli.json; usually it is a single 'src' folder.
   */
  fun getRootDirs(): Collection<VirtualFile>

  /**
   * @return folders that are precessed as root folders by style preprocessor according to apps -> stylePreprocessorOptions -> includePaths in .angular-cli.json
   */
  fun getStylePreprocessorIncludeDirs(): Collection<VirtualFile>

  fun getKarmaConfigFile(): VirtualFile?

  fun getProtractorConfigFile(): VirtualFile?

  fun exists(): Boolean

}

@Suppress("DEPRECATION")
private class AngularCliEmptyConfig : AngularCliConfig {

  override fun getIndexHtmlFile(): VirtualFile? = null

  override fun getGlobalStyleSheets(): Collection<VirtualFile> = emptyList()

  override fun getRootDirs(): Collection<VirtualFile> = emptyList()

  override fun getStylePreprocessorIncludeDirs(): Collection<VirtualFile> = emptyList()

  override fun getKarmaConfigFile(): VirtualFile? = null

  override fun getProtractorConfigFile(): VirtualFile? = null

  override fun exists(): Boolean = false

}

@Suppress("DEPRECATION")
private class AngularCliJsonFileConfig(private val config: AngularConfig) : AngularCliConfig {

  override fun getIndexHtmlFile(): VirtualFile? {
    return config.projects.mapNotNull { it.indexHtmlFile }.firstOrNull()
  }

  override fun getGlobalStyleSheets(): Collection<VirtualFile> {
    return StreamEx.of(config.projects).flatCollection { it.globalStyleSheets }.toList()
  }

  override fun getRootDirs(): Collection<VirtualFile> {
    return config.projects.mapNotNull { it.rootDir }
  }

  override fun getStylePreprocessorIncludeDirs(): Collection<VirtualFile> {
    return StreamEx.of(config.projects).flatCollection { it.stylePreprocessorIncludeDirs }.toList()
  }

  override fun getKarmaConfigFile(): VirtualFile? {
    return config.projects.mapNotNull { it.karmaConfigFile }.firstOrNull()
  }

  override fun getProtractorConfigFile(): VirtualFile? {
    return config.projects.mapNotNull { it.protractorConfigFile }.firstOrNull()
  }

  override fun exists(): Boolean = true
}

