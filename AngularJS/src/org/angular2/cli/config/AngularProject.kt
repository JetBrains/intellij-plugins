// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.openapi.vfs.VirtualFile

class AngularProject internal constructor(val name: String,
                                          private val project: AngularJsonProject,
                                          internal val angularCliFolder: VirtualFile) {

  val rootDir = project.rootPath?.let { angularCliFolder.findFileByRelativePath(it) }

  val sourceDir = (project.sourceRoot ?: "").ifBlank { project.rootPath }?.let { angularCliFolder.findFileByRelativePath(it) }

  val indexHtmlFile get() = resolveFile(project.targets?.build?.options?.index ?: project.index)

  val globalStyleSheets
    get() = (project.targets?.build?.options?.styles ?: project.styles)
              ?.mapNotNull { rootDir?.findFileByRelativePath(it) }
            ?: emptyList()

  val stylePreprocessorIncludeDirs
    get() = (project.targets?.build?.options?.stylePreprocessorOptions ?: project.stylePreprocessorOptions)
              ?.includePaths
              ?.mapNotNull { rootDir?.findFileByRelativePath(it) }
            ?: emptyList()

  val karmaConfigFile get() = resolveFile(project.targets?.test?.options?.karmaConfig)

  val protractorConfigFile get() = resolveFile(project.targets?.e2e?.options?.protractorConfig)

  val tsLintConfigurations = project.targets?.lint?.let { lint ->
    val result = mutableListOf<AngularLintConfiguration>()
    lint.options?.let { result.add(AngularLintConfiguration(this, it)) }
    lint.configurations.mapTo(result) { (name, config) ->
      AngularLintConfiguration(this, config, name)
    }
    result
  } ?: emptyList<AngularLintConfiguration>()

  internal fun resolveFile(filePath: String?): VirtualFile? {
    return angularCliFolder.findFileByRelativePath(filePath ?: return null)
           ?: rootDir?.findFileByRelativePath(filePath)
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
      |AngularProject {
      |       name: ${name}
      |       rootDir: ${rootDir}
      |       sourceDir: ${sourceDir}
      |       indexHtml: ${indexHtmlFile}
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

}
