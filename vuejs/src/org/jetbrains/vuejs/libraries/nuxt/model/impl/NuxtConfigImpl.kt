// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model.impl

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.findDefaultExport
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtConfig

class NuxtConfigImpl(override val file: PsiFile) : NuxtConfig {

  private val configLiteral = objectLiteralFor(findDefaultExport(file))

  override val sourceDir: VirtualFile? =
    configLiteral
      ?.findProperty("srcDir")
      ?.jsType
      ?.castSafelyTo<JSStringLiteralTypeImpl>()
      ?.literal
      ?.let { file.virtualFile?.parent?.findFileByRelativePath(it) }

  override val components: List<NuxtConfig.ComponentsDirectoryConfig> =
    configLiteral
      ?.findProperty("components")
      ?.let { property ->
        when (val propertyValue = property.value?.let { if (it is JSObjectLiteralExpression) it.findProperty("dirs")?.value else it }) {
          is JSLiteralExpression -> if (propertyValue.value == true) listOf(ComponentsDirectoryConfigImpl()) else emptyList()
          is JSArrayLiteralExpression -> propertyValue.expressions.mapNotNull { readComponentsDirectoriesConfig(it) }
          else -> emptyList()
        }
      }
    ?: emptyList()

  private fun readComponentsDirectoriesConfig(config: JSExpression): NuxtConfig.ComponentsDirectoryConfig? =
    when (config) {
      is JSLiteralExpression -> (config.value as? String)?.let { ComponentsDirectoryConfigImpl(it) }
      is JSObjectLiteralExpression -> ComponentsDirectoryConfigImpl(
        path = config.findProperty("path")?.value?.castSafelyTo<JSLiteralExpression>()?.value as? String ?: DEFAULT_PATH,
        prefix = (config.findProperty("prefix")?.value?.castSafelyTo<JSLiteralExpression>()?.value as? String)?.let { fromAsset(it) }
                 ?: DEFAULT_PREFIX,
        global = config.findProperty("global")?.value?.castSafelyTo<JSLiteralExpression>()?.value as? Boolean ?: false,
        extensions = config.findProperty("extensions")?.value?.castSafelyTo<JSArrayLiteralExpression>()
                       ?.expressions?.asSequence()
                       ?.mapNotNull { (it as? JSLiteralExpression)?.value as? String }
                       ?.toSet() ?: DEFAULT_EXTENSIONS,
        level = config.findProperty("level")?.value?.castSafelyTo<JSLiteralExpression>()?.value as? Int ?: DEFAULT_LEVEL
      )
      else -> null
    }

  companion object {
    const val DEFAULT_PATH = "~/components"
    const val DEFAULT_PREFIX = ""
    const val DEFAULT_GLOBAL = false
    val DEFAULT_EXTENSIONS = setOf("vue", "js", "ts", "tsx")
    const val DEFAULT_LEVEL = 0
  }

  private class ComponentsDirectoryConfigImpl(override val path: String = DEFAULT_PATH,
                                              override val prefix: String = DEFAULT_PREFIX,
                                              override val global: Boolean = DEFAULT_GLOBAL,
                                              override val extensions: Set<String> = DEFAULT_EXTENSIONS,
                                              override val level: Int = DEFAULT_LEVEL) : NuxtConfig.ComponentsDirectoryConfig

}