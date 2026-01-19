// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.configureVueDependencies

class VueTsConfigFile :
  WebFrameworkTestConfigurator {

  override fun configure(
    fixture: CodeInsightTestFixture,
    disposable: Disposable?,
  ) {
    fixture.configureVueDependencies()
    fixture.configureByText(FILE_NAME, DEFAULT_TSCONFIG_CONTENT)

    disposable?.let {
      Disposer.register(it) {
        WriteAction.run<Throwable> {
          fixture.tempDirFixture
            .getFile(FILE_NAME)
            ?.delete(Any())
        }
      }
    }
  }
  
  companion object {
    const val FILE_NAME: String = "tsconfig.json"

    // language=jsonc
    val DEFAULT_TSCONFIG_CONTENT = """
    {
      "extends": "@vue/tsconfig/tsconfig.dom.json",
      "compilerOptions": {
        "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.tsbuildinfo",
        "types": [],
    
        /* Linting */
        "strict": true,
        "noUnusedLocals": true,
        "noUnusedParameters": true,
        "erasableSyntaxOnly": true,
        "noFallthroughCasesInSwitch": true,
        "noUncheckedSideEffectImports": true
      },
      "include": [
        "src/**/*.ts", 
        "src/**/*.tsx",
        "src/**/*.vue"
      ]
    }
    """.trimIndent()
  }
}