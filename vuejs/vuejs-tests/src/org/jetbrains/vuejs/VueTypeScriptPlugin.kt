// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion

fun getRequiredHybridModeBundledVersion(
  fixture: CodeInsightTestFixture,
  testMode: VueTestMode,
): VueLanguageToolsVersion? =
  when (testMode) {
    VueTestMode.DEFAULT,
      -> if (useDefaultPlugin(fixture))
      VueLanguageToolsVersion.DEFAULT
    else
      VueLanguageToolsVersion.LEGACY

    VueTestMode.LEGACY_PLUGIN,
      -> VueLanguageToolsVersion.LEGACY

    VueTestMode.NO_PLUGIN,
      -> return null
  }

private fun useDefaultPlugin(
  fixture: CodeInsightTestFixture,
): Boolean {
  val packageJson = fixture.tempDirFixture.getFile("node_modules/vue/package.json")
                    ?: return true

  val version = PackageJsonData.getOrCreate(packageJson).version
                ?: return true

  return version.major != 2
}
