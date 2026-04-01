// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.jupiter.api.assertNull

class VueTsConfigFile :
  PolySymbolsTestConfigurator {

  override fun configure(
    fixture: CodeInsightTestFixture,
  ) {
    assertNull(fixture.tempDirFixture.getFile(FILE_NAME))
    assertNull(fixture.tempDirFixture.getFile("tsconfig.base.json"))

    fixture.configureByText(FILE_NAME, DEFAULT_TSCONFIG_CONTENT)
  }

  override fun beforeDirectoryComparison(fixture: CodeInsightTestFixture, resultsDir: VirtualFile, goldDir: VirtualFile) {
    WriteAction.run<Throwable> {
      fixture.tempDirFixture
        .getFile(FILE_NAME)
        ?.delete(Any())
    }
  }


  companion object {
    const val FILE_NAME: String = "tsconfig.json"

    // language=jsonc
    val DEFAULT_TSCONFIG_CONTENT = """
    {
      "extends": "@vue/tsconfig/tsconfig.dom.json",
      "include": [
        "**/*.ts", 
        "**/*.tsx",
        "**/*.vue"
      ]
      "compilerOptions": {
        // Extra safety for array and object lookups, but may have false positives.
        "noUncheckedIndexedAccess": true,
    
        // `vue-tsc --build` produces a .tsbuildinfo file for incremental type-checking.
        // Specified here to keep it out of the root directory.
        "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo"
      }
    }
    """.trimIndent()
  }
}