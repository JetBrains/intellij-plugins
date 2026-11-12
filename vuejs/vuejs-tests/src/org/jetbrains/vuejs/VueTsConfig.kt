// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.vuejs.config.VueCompilerOptions

@Serializable
data class VueTsConfig(
  val extends: String? = null,
  val include: List<String>? = null,
  val compilerOptions: CompilerOptions? = null,
  val vueCompilerOptions: VueCompilerOptions? = null,
  val contentMappers: List<ContentMapper>? = null,
) {
  @Serializable
  data class CompilerOptions(
    val types: List<String>? = null,
    val noUncheckedIndexedAccess: Boolean? = null,
    val tsBuildInfoFile: String? = null,
  )

  @Serializable
  data class ContentMapper(
    @SerialName("package")
    val packageName: String,
    val extensions: List<String>,
    val options: VueCompilerOptions? = null,
  )
}