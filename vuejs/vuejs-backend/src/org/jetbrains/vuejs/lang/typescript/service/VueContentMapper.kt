// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.lang.javascript.psi.util.JSPluginPathManager
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.TypeScriptContentMapperContribution
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.toNioPathOrNull
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import java.io.IOException
import java.nio.file.Path

const val vueContentMapperPackageName: String = "@vue/content-mapper"

/**
 * The version of the `@vue/content-mapper` package that the plugin bundles.
 */
const val vueContentMapperVersion: String = "4.0.0-alpha.3"

private val LOG = logger<VueContentMapperContribution>()

/**
 * The directory of the bundled `@vue/content-mapper` package, or `null` if the plugin has no such directory.
 */
fun getBundledVueContentMapperDir(): Path? =
  try {
    JSPluginPathManager.getPluginResource(
      VueContentMapperContribution::class.java,
      "vue-language-tools/content-mapper",
      vuePluginPath,
    )
  }
  catch (e: IOException) {
    LOG.warn("Cannot find the bundled $vueContentMapperPackageName", e)
    null
  }

internal object VueContentMapperContribution {
  /**
   * The bundled `@vue/content-mapper` for the `.vue` files outside a tsconfig.
   * The mapper runs with the Node.js interpreter of the TypeScript settings.
   * Without a tsconfig, the mapper resolves TypeScript from its working directory, as in the Vue extension for VS Code.
   * The `target` option selects the latest Vue version, so the mapper does not look for the `vue` package.
   */
  fun create(project: Project): TypeScriptContentMapperContribution? {
    val mapperDir = getBundledVueContentMapperDir() ?: return null
    val node = (TypeScriptLanguageServiceUtil.getNodeInterpreter(project) as? NodeJsLocalInterpreter)
                 ?.interpreterSystemDependentPath
               ?: return null
    return TypeScriptContentMapperContribution(
      extensions = listOf(VUE_FILE_EXTENSION),
      name = vueContentMapperPackageName,
      version = vueContentMapperVersion,
      exec = listOf(node, mapperDir.resolve("lib/server.js").toString()),
      cwd = project.guessProjectDir()?.toNioPathOrNull()?.toString(),
      options = mapOf("target" to 99, "format" to "sfc"),
      dynamicConfig = true,
    )
  }
}
