// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.lang.javascript.frameworks.modules.JSPathMappingsUtil
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.util.AtomicNullableLazyValue
import com.intellij.openapi.vfs.VirtualFile

class AngularLintConfiguration internal constructor(private val project: AngularProject,
                                                    private val config: AngularJsonLintOptions,
                                                    val name: String? = null) {

  private val myIncludePattern = AtomicNullableLazyValue.createValue {
    TypeScriptConfigUtil.getRegularExpressionForGlobPattern(
      config.files, project.angularCliFolder, TypeScriptConfigUtil.WildCardType.FILES)
      ?.let { JSPathMappingsUtil.createMappingPattern(it, project.angularCliFolder) }
  }
  private val myExcludePattern = AtomicNullableLazyValue.createValue {
    TypeScriptConfigUtil.getRegularExpressionForGlobPattern(
      config.exclude, project.angularCliFolder, TypeScriptConfigUtil.WildCardType.EXCLUDE)
      ?.let { JSPathMappingsUtil.createMappingPattern(it, project.angularCliFolder) }
  }

  val tsLintConfig: VirtualFile?
    get() = if (config.tsLintConfig != null) {
      project.resolveFile(config.tsLintConfig)
    }
    else {
      project.rootDir?.findChild("tslint.json")
    }

  val tsConfigs: List<VirtualFile>
    get() = config.tsConfig.mapNotNull { project.resolveFile(it) }

  fun accept(file: VirtualFile): Boolean {
    val path = file.path
    return (myIncludePattern.value?.matcher(path)?.find() ?: true)
           && !(myExcludePattern.value?.matcher(path)?.find() ?: false)
  }

  override fun toString(): String {
    return """
      |TsLintConfiguration {
      |           name=${name}
      |           tsLintConfig=${tsLintConfig}
      |           tsConfigs=${tsConfigs}
      |           includePattern=${myIncludePattern.value}
      |           excludePattern=${myExcludePattern.value}
      |         }""".trimMargin()
  }
}
