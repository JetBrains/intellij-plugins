// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.util.text.SemVer

const val NUXT_PKG = "nuxt"
const val NUXT_TYPES_PKG = "@nuxt/types"
const val NUXT_CONFIG_PKG = "@nuxt/config"

val NUXT_CONFIG_NAMES: List<String> = listOf("nuxt.config.js", "nuxt.config.ts")

val NUXT_2_9_0 = SemVer("2.9.0", 2, 9, 0)
val NUXT_2_13_0 = SemVer("2.13.0", 2, 13, 0)
val NUXT_2_15_0 = SemVer("2.15.0", 2, 15, 0)