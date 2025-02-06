// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

class Angular2TsConfigFile(
  private val strict: Boolean = true,
  private val strictNullChecks: Boolean? = null,

  // https://angular.io/guide/angular-compiler-options#strictinjectionparameters
  private val strictInjectionParameters: Boolean? = true,

  // https://angular.io/guide/template-typecheck#troubleshooting-template-errors and following flags
  private val strictTemplates: Boolean = true,
  private val strictInputTypes: Boolean? = null,
  private val strictInputAccessModifiers: Boolean? = true,
  private val strictNullInputTypes: Boolean? = null,
  private val strictAttributeTypes: Boolean? = null,
  private val strictSafeNavigationTypes: Boolean? = null,
  private val strictDomLocalRefTypes: Boolean? = null,
  private val strictOutputEventTypes: Boolean? = null,
  private val strictDomEventTypes: Boolean? = null,
  private val strictContextGenerics: Boolean? = null,
  private val strictLiteralTypes: Boolean? = null,
) : WebFrameworkTestConfigurator {

  override fun configure(fixture: CodeInsightTestFixture, disposable: Disposable?) {
    fixture.configureByText("tsconfig.json", """
        {
          "compileOnSave": false,
          "compilerOptions": {
            "baseUrl": "./",
            "outDir": "./dist/out-tsc",
            "forceConsistentCasingInFileNames": true,
            ${
              listOfNotNull(
                strict.let { "\"strict\": $it" },
                strictNullChecks?.let { "\"strictNullChecks\": $it" },
              ).joinToString(",\n" + "            ")
            },
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
            ${
      listOfNotNull(
        strictInjectionParameters?.let { "\"strictInjectionParameters\": $it" },
        strictTemplates.let { "\"strictTemplates\": $it" },
        strictInputTypes?.let { "\"strictInputTypes\": $it" },
        strictInputAccessModifiers?.let { "\"strictInputAccessModifiers\": $it" },
        strictNullInputTypes?.let { "\"strictNullInputTypes\": $it" },
        strictAttributeTypes?.let { "\"strictAttributeTypes\": $it" },
        strictSafeNavigationTypes?.let { "\"strictSafeNavigationTypes\": $it" },
        strictDomLocalRefTypes?.let { "\"strictDomLocalRefTypes\": $it" },
        strictOutputEventTypes?.let { "\"strictOutputEventTypes\": $it" },
        strictDomEventTypes?.let { "\"strictDomEventTypes\": $it" },
        strictContextGenerics?.let { "\"strictContextGenerics\": $it" },
        strictLiteralTypes?.let { "\"strictLiteralTypes\": $it" },
      ).joinToString(",\n" + "            ")
    }
          }
        }
      """.trimIndent())
    disposable?.let {
      Disposer.register(it) {
        WriteAction.run<Throwable> {
          fixture.tempDirFixture
            .getFile("tsconfig.json")
            ?.delete(Any())
        }
      }
    }
  }
}