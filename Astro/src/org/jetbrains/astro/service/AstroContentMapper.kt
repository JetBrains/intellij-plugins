// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.lang.javascript.psi.util.JSPluginPathManager
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.TypeScriptContentMapperContribution
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.toNioPathOrNull
import java.io.IOException
import java.nio.file.Path

const val astroContentMapperPackageName: String = "@astrojs/ts-content-mapper"

/**
 * The version of the `@astrojs/ts-content-mapper` package that the plugin bundles.
 */
const val astroContentMapperVersion: String = "0.2.0"

private val LOG = logger<AstroContentMapperContribution>()

/**
 * The directory of the bundled `@astrojs/ts-content-mapper` package, or `null` if the plugin has no such directory.
 */
fun getBundledAstroContentMapperDir(): Path? =
  try {
    JSPluginPathManager.getPluginResource(
      AstroContentMapperContribution::class.java,
      "astro-content-mapper",
      "Astro",
    )
  }
  catch (e: IOException) {
    LOG.warn("Cannot find the bundled $astroContentMapperPackageName", e)
    null
  }

internal object AstroContentMapperContribution {
  /**
   * The bundled `@astrojs/ts-content-mapper` for the `.astro` files outside a tsconfig.
   * The mapper runs with the Node.js interpreter of the TypeScript settings.
   * The mapper reads no options and needs no TypeScript package.
   */
  fun create(project: Project): TypeScriptContentMapperContribution? {
    val mapperDir = getBundledAstroContentMapperDir() ?: return null
    val node = (TypeScriptLanguageServiceUtil.getNodeInterpreter(project) as? NodeJsLocalInterpreter)
                 ?.interpreterSystemDependentPath
               ?: return null
    return TypeScriptContentMapperContribution(
      extensions = listOf(".astro"),
      name = astroContentMapperPackageName,
      version = astroContentMapperVersion,
      exec = listOf(node, mapperDir.resolve("lib/server.mjs").toString()),
      cwd = project.guessProjectDir()?.toNioPathOrNull()?.toString(),
      options = null,
      dynamicConfig = false,
    )
  }
}
