// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

abstract class AngularProject(internal val angularCliFolder: VirtualFile, internal val project: Project) {
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

  abstract val tsLintConfigurations: List<AngularLintConfiguration>

  abstract val type: AngularProjectType?

  abstract val inlineStyleLanguage: Language?

  internal open fun resolveFile(filePath: String?): VirtualFile? {
    return filePath?.let { path ->
      rootDir?.takeIf { it.isValid }?.findFileByRelativePath(path)
      ?: angularCliFolder.takeIf { it.isValid }?.findFileByRelativePath(path)
    }
  }

  internal fun proximity(context: VirtualFile): Int {
    val rootDirPath = (rootDir ?: return -1).path + "/"
    val contextPath = context.path
    if (!contextPath.startsWith(rootDirPath)) {
      return -1
    }
    return contextPath.length - rootDirPath.length
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
      |         ${tsLintConfigurations.joinToString(",\n         ") { it.toString() }}
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

internal class AngularProjectImpl(override val name: String,
                                  private val ngProject: AngularJsonProject,
                                  angularCliFolder: VirtualFile,
                                  project: Project)
  : AngularProject(angularCliFolder, project) {

  override val rootDir = ngProject.rootPath?.let { angularCliFolder.findFileByRelativePath(it) }

  override val sourceDir get() = ngProject.sourceRoot?.let { angularCliFolder.findFileByRelativePath(it) } ?: rootDir

  override val cssResolveRootDir: VirtualFile? get() = rootDir

  override val indexHtmlFile get() = resolveFile(ngProject.targets?.build?.options?.index)

  override val globalStyleSheets
    get() = ngProject.targets?.build?.options?.styles
              ?.mapNotNull { rootDir?.findFileByRelativePath(it) }
            ?: emptyList()

  override val stylePreprocessorIncludeDirs
    get() = ngProject.targets?.build?.options?.stylePreprocessorOptions?.includePaths
              ?.mapNotNull { angularCliFolder.findFileByRelativePath(it) }
            ?: emptyList()

  override val tsConfigFile: VirtualFile?
    get() = resolveFile(ngProject.targets?.build?.options?.tsConfig)

  override val karmaConfigFile get() = resolveFile(ngProject.targets?.test?.options?.karmaConfig)

  override val protractorConfigFile get() = resolveFile(ngProject.targets?.e2e?.options?.protractorConfig)

  override val tsLintConfigurations = ngProject.targets?.lint?.let { lint ->
    val result = mutableListOf<AngularLintConfiguration>()
    lint.options?.let { result.add(AngularLintConfiguration(this, it)) }
    lint.configurations.mapTo(result) { (name, config) ->
      AngularLintConfiguration(this, config, name)
    }
    result
  } ?: emptyList<AngularLintConfiguration>()

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
                                        angularCliFolder: VirtualFile,
                                        project: Project)
  : AngularProject(angularCliFolder, project) {

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

  override val tsLintConfigurations
    get() = angularJson.legacyLint.map { config ->
      AngularLintConfiguration(this, config, null)
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
