// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model.impl

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.codeInsight.findDefaultExport
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtConfig

class NuxtConfigImpl(override val file: PsiFile, nuxt2_15: Boolean) : NuxtConfig {

  private val configLiteral = objectLiteralFor(findDefaultExport(file as? JSFile))

  private val globalDefault = nuxt2_15

  private val pathPrefixDefault = nuxt2_15

  override val sourceDir: VirtualFile? =
    configLiteral
      ?.findProperty("srcDir")
      ?.jsType
      ?.let { it as? JSStringLiteralTypeImpl }
      ?.literal
      ?.let { file.virtualFile?.parent?.findFileByRelativePath(it) }

  override val components: List<NuxtConfig.ComponentsDirectoryConfig> =
    configLiteral
      ?.findProperty("components")
      ?.let { property ->
        when (val propertyValue = property.value?.let { if (it is JSObjectLiteralExpression) it.findProperty("dirs")?.value else it }) {
          is JSLiteralExpression -> if (propertyValue.value == true)
            listOf(ComponentsDirectoryConfigImpl(pathPrefix = pathPrefixDefault, global = globalDefault))
          else emptyList()
          is JSArrayLiteralExpression -> propertyValue.expressions.mapNotNull { readComponentsDirectoriesConfig(it) }
          else -> emptyList()
        }
      }
    ?: emptyList()

  private fun readComponentsDirectoriesConfig(config: JSExpression): NuxtConfig.ComponentsDirectoryConfig? =
    when (config) {
      is JSLiteralExpression -> (config.value as? String)?.let {
        ComponentsDirectoryConfigImpl(it, pathPrefix = pathPrefixDefault, global = globalDefault)
      }
      is JSObjectLiteralExpression -> ComponentsDirectoryConfigImpl(
        path = config.findProperty("path")?.value?.let { it as? JSLiteralExpression }?.value as? String ?: DEFAULT_PATH,
        prefix = (config.findProperty("prefix")?.value?.let { it as? JSLiteralExpression }?.value as? String)?.let { fromAsset(it) }
                 ?: DEFAULT_PREFIX,
        pathPrefix = config.findProperty("pathPrefix")?.value?.let { it as? JSLiteralExpression }?.value as? Boolean ?: pathPrefixDefault,
        global = config.findProperty("global")?.value?.let { it as? JSLiteralExpression }?.value as? Boolean ?: globalDefault,
        extensions = config.findProperty("extensions")?.value?.let { it as? JSArrayLiteralExpression }
                       ?.expressions?.asSequence()
                       ?.mapNotNull { (it as? JSLiteralExpression)?.value as? String }
                       ?.toSet() ?: DEFAULT_EXTENSIONS,
        level = config.findProperty("level")?.value?.let { it as? JSLiteralExpression }?.value?.let { it as? Number }?.toInt()
                ?: DEFAULT_LEVEL
      )
      else -> null
    }

  companion object {
    const val DEFAULT_PATH = "~/components"
    const val DEFAULT_PREFIX = ""
    val DEFAULT_EXTENSIONS = setOf("vue", "js", "ts", "tsx")
    const val DEFAULT_LEVEL = 0
  }

  private class ComponentsDirectoryConfigImpl(override val path: String = DEFAULT_PATH,
                                              override val prefix: String = DEFAULT_PREFIX,
                                              override val pathPrefix: Boolean,
                                              override val global: Boolean,
                                              override val extensions: Set<String> = DEFAULT_EXTENSIONS,
                                              override val level: Int = DEFAULT_LEVEL) : NuxtConfig.ComponentsDirectoryConfig

}