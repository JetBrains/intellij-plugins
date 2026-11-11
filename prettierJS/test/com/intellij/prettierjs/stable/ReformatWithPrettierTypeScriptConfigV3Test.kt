// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.javascript.debugger.NodeJsAppRule.Companion.LATEST_23
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.prettierjs.stable.PrettierPackageLockFixture.Companion.prettierPackageLockFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class ReformatWithPrettierTypeScriptConfigV3Test {
  private val prettier by prettierPackageLockFixture(nodeJsAppRule = LATEST_23)

  @Test
  fun testTypeScriptConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("ts")
  }
}
