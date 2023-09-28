// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.web.WebFrameworkTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

object Angular2TsConfigFile {

  val withStrictTemplates: WebFrameworkTestConfigurator
    get() = object : WebFrameworkTestConfigurator {

      override fun configure(fixture: CodeInsightTestFixture) {
        fixture.configureByText("tsconfig.json", """
          {
            "compileOnSave": false,
            "compilerOptions": {
              "baseUrl": "./",
              "outDir": "./dist/out-tsc",
              "forceConsistentCasingInFileNames": true,
              "strict": true,
              "noImplicitOverride": true,
              "noPropertyAccessFromIndexSignature": true,
              "noImplicitReturns": true,
              "noFallthroughCasesInSwitch": true,
              "sourceMap": true,
              "declaration": false,
              "downlevelIteration": true,
              "experimentalDecorators": true,
              "moduleResolution": "node",
              "importHelpers": true,
              "target": "es2020",
              "module": "es2020",
              "lib": [
                "es2020",
                "dom"
              ]
            },
            "angularCompilerOptions": {
              "enableI18nLegacyMessageIdFormat": false,
              "strictInjectionParameters": true,
              "strictInputAccessModifiers": true,
              "strictTemplates": true
            }
          }
        """.trimIndent())
      }
    }
}