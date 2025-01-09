// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.openapi.project.DefaultProjectFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

abstract class AngularProject(internal val angularCliFolder: VirtualFile) {
  abstract val name: String

  abstract val rootDir: VirtualFile?

  abstract val sourceDir: VirtualFile?

  abstract val indexHtmlFile: VirtualFile?

  abstract val globalStyleSheets: List<VirtualFile>

  abstract val stylePreprocessorIncludeDirs: List<VirtualFile>

  abstract val cssResolveRootDir: VirtualFile?

  abstract val tsConfigFile: VirtualFile?

  abstract val karmaConfigFile: VirtualFile?

  abstract val protractorConfigFile: VirtualFile?

  abstract fun getTsLintConfigurations(project: Project): List<AngularLintConfiguration>

  abstract val type: AngularProjectType?

  abstract val inlineStyleLanguage: Language?

  abstract fun resolveFile(filePath: String?): VirtualFile?

  fun proximity(context: VirtualFile): Int {
    val rootDir = rootDir ?: return -1
    val sourceDir = sourceDir ?: rootDir
    if (!VfsUtil.isAncestor(rootDir, context, false))
      return -1
    return generateSequence(context) { it.parent }
      .takeWhile { it != rootDir && it != sourceDir }
      .count()
  }

  override fun toString(): String {
    return """
      |${javaClass.simpleName} {
      |       name: ${name}
      |       type: ${type}
      |       rootDir: ${rootDir}
      |       sourceDir: ${sourceDir}
      |       indexHtml: ${indexHtmlFile}
      |       tsConfig: ${tsConfigFile}
      |       globalStyleSheets: ${globalStyleSheets}
      |       stylePreprocessorIncludeDirs: ${stylePreprocessorIncludeDirs}
      |       karmaConfigFile: ${karmaConfigFile}
      |       protractorConfigFile: ${protractorConfigFile}
      |       tsLintConfigurations: [
      |         ${
      getTsLintConfigurations(DefaultProjectFactory.getInstance().defaultProject)
        .joinToString(",\n         ") { it.toString() }
    }
      |       ]
      |     }
    """.trimMargin()
  }

  enum class AngularProjectType {
    @JsonProperty("application")
    APPLICATION,

    @JsonProperty("library")
    LIBRARY
  }
}

open class AngularProjectImpl(override val name: String,
                              private val ngProject: AngularJsonProject,
                              angularCliFolder: VirtualFile)
  : AngularProject(angularCliFolder) {

  override val rootDir: VirtualFile? = ngProject.rootPath?.let { angularCliFolder.findFileByRelativePath(it) }

  override val sourceDir: VirtualFile?
    get() = ngProject.sourceRoot.let {
      if (it != null)
        angularCliFolder.findFileByRelativePath(it)
      else
        rootDir
    }

  override val cssResolveRootDir: VirtualFile? get() = rootDir

  override val indexHtmlFile: VirtualFile? get() = resolveFile(ngProject.targets?.build?.options?.index)

  override val globalStyleSheets: List<VirtualFile>
    get() = ngProject.targets?.build?.options?.styles
              ?.mapNotNull { rootDir?.findFileByRelativePath(it) }
            ?: emptyList()

  override val stylePreprocessorIncludeDirs: List<VirtualFile>
    get() = ngProject.targets?.build?.options?.stylePreprocessorOptions?.includePaths
              ?.mapNotNull { angularCliFolder.findFileByRelativePath(it) }
            ?: emptyList()

  override val tsConfigFile: VirtualFile?
    get() = resolveFile(ngProject.targets?.build?.options?.tsConfig)

  override val karmaConfigFile: VirtualFile? get() = resolveFile(ngProject.targets?.test?.options?.karmaConfig)

  override val protractorConfigFile: VirtualFile? get() = resolveFile(ngProject.targets?.e2e?.options?.protractorConfig)

  override fun resolveFile(filePath: String?): VirtualFile? {
    return filePath?.let { path ->
      rootDir?.takeIf { it.isValid }?.findFileByRelativePath(path)
      ?: angularCliFolder.takeIf { it.isValid }?.findFileByRelativePath(path)
    }
  }

  override fun getTsLintConfigurations(project: Project): List<AngularLintConfiguration> =
    ngProject.targets?.lint?.let { lint ->
      val result = mutableListOf<AngularLintConfiguration>()
      lint.options?.let { result.add(AngularLintConfiguration(project, this, it)) }
      lint.configurations.mapTo(result) { (name, config) ->
        AngularLintConfiguration(project, this, config, name)
      }
      result
    } ?: emptyList()

  override val type: AngularProjectType?
    get() = ngProject.projectType

  override val inlineStyleLanguage: Language?
    get() {
      val text = ngProject.targets?.build?.options?.inlineStyleLanguage
      return CSSLanguage.INSTANCE.dialects.firstOrNull { it.id.equals(text, ignoreCase = true) }
    }
}

internal class AngularLegacyProjectImpl(private val angularJson: AngularJson,
                                        private val app: AngularJsonLegacyApp,
                                        angularCliFolder: VirtualFile)
  : AngularProject(angularCliFolder) {

  override val name: String = app.name ?: angularJson.legacyProject?.name ?: "Angular project"

  override val rootDir: VirtualFile = app.appRoot?.let { angularCliFolder.findFileByRelativePath(it) } ?: angularCliFolder

  override val sourceDir: VirtualFile? get() = app.root?.let { rootDir.findFileByRelativePath(it) }

  override val cssResolveRootDir: VirtualFile? get() = sourceDir

  override val indexHtmlFile: VirtualFile? get() = resolveFile(app.index)

  override val globalStyleSheets: List<VirtualFile>
    get() = app.styles
              ?.mapNotNull { sourceDir?.findFileByRelativePath(it) }
            ?: emptyList()

  override val stylePreprocessorIncludeDirs: List<VirtualFile>
    get() = app.stylePreprocessorOptions?.includePaths
              ?.mapNotNull { sourceDir?.findFileByRelativePath(it) }
            ?: emptyList()

  override val tsConfigFile: VirtualFile?
    get() = resolveFile(app.tsConfig)

  override val karmaConfigFile: VirtualFile?
    get() = resolveFile(angularJson.legacyTest?.karma?.config)

  override val protractorConfigFile: VirtualFile?
    get() = resolveFile(angularJson.legacyE2E?.protractor?.config)

  override fun getTsLintConfigurations(project: Project): List<AngularLintConfiguration> =
    angularJson.legacyLint.map { config ->
      AngularLintConfiguration(project, this, config, null)
    }

  override val type: AngularProjectType?
    get() = null

  override val inlineStyleLanguage: Language?
    get() = null

  override fun resolveFile(filePath: String?): VirtualFile? {
    return filePath?.let {
      sourceDir?.findFileByRelativePath(it)
      ?: rootDir.findFileByRelativePath(it)
    }
  }
}
