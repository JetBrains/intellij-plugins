// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.getAppropriateVueLSVersion

fun getRequiredHybridModeBundledVersion(
  fixture: CodeInsightTestFixture,
  testMode: VueTestMode,
): VueLanguageToolsVersion? =
  when (testMode) {
    VueTestMode.DEFAULT,
      -> getLanguageToolsVersion(fixture)

    VueTestMode.TS_GO_PROXY,
    VueTestMode.NO_PLUGIN,
      -> null
  }

private fun getLanguageToolsVersion(
  fixture: CodeInsightTestFixture,
): VueLanguageToolsVersion {
  val packageJson = fixture.tempDirFixture.getFile("node_modules/vue/package.json")
                    ?: return VueLanguageToolsVersion.DEFAULT

  return runReadActionBlocking {
    getAppropriateVueLSVersion(fixture.project, packageJson)
  }
}
