// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil
import com.intellij.lang.javascript.linter.tslint.TslintUtil
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.util.AtomicNullableLazyValue
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.NullableLazyValue
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.nullize
import com.intellij.util.text.minimatch.Minimatch

class AngularLintConfiguration internal constructor(private val ngProject: AngularProject,
                                                    private val config: AngularJsonLintOptions,
                                                    val name: String? = null) {
  private val myIncludes = NullableLazyValue.createValue {
    config.files.mapNotNull(::createGlobMatcher).nullize()
  }
  private val myExcludes = NotNullLazyValue.lazy {
    config.exclude.mapNotNull(::createGlobMatcher)
  }

  private val tsLintConfig: VirtualFile?
    get() = ngProject.resolveFile(config.tsLintConfig)

  val tsConfigs: List<VirtualFile>
    get() = config.tsConfig.mapNotNull { ngProject.resolveFile(it) }

  fun getTsLintConfig(file: VirtualFile): VirtualFile? {
    if (!accept(file)) return null
    tsLintConfig?.let { return it }
    return JSLinterConfigFileUtil.findFileUpToRoot(file, TslintUtil.CONFIG_FILE_NAMES, ngProject.angularCliFolder)
  }

  fun accept(file: VirtualFile): Boolean {
    val rootPath = (ngProject.rootDir ?: return false).path + "/"
    val filePath = file.path
    val relativePath = FileUtil.getRelativePath(rootPath, filePath, '/') ?: return false

    myIncludes.value?.let { includes ->
      return includes.any { it.match(relativePath) }
             && !myExcludes.value.any { it.match(relativePath) }
    }
    return !myExcludes.value.any { it.match(relativePath) }
           && tsConfigs.any { TypeScriptConfigUtil.configGraphIncludesFile(ngProject.project, file, it) }
  }

  override fun toString(): String {
    return """
      |TsLintConfiguration {
      |           name=${name}
      |           tsLintConfig=${tsLintConfig}
      |           tsConfigs=${tsConfigs}
      |           includes=${myIncludes.value}
      |           excludes=${myExcludes.value}
      |         }""".trimMargin()
  }

  companion object {
    fun createGlobMatcher(globString: String) = try {
      Minimatch(globString)
    }
    catch (e: Exception) {
      null
    }
  }
}
