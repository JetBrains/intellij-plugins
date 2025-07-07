// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import org.jetbrains.vuejs.lang.getVueTestDataPath

abstract class VueTestCase(
  override val testCasePath: String,
) : WebFrameworkTestCase() {

  override val testDataRoot: String
    get() = getVueTestDataPath()

  override val defaultExtension: String
    get() = "vue"

  override val defaultDependencies: Map<String, String>
    get() = mapOf()
}