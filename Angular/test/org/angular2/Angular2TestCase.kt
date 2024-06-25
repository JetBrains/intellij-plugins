// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.web.WebFrameworkTestCase
import com.intellij.lang.javascript.HybridTestMode
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil.TypeScriptUseServiceState
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.ui.UIUtil
import org.angular2.lang.expr.service.Angular2TypeScriptService

abstract class Angular2TestCase(
  override val testCasePath: String,
  private val useTsc: Boolean,
) : WebFrameworkTestCase() {

  override val testDataRoot: String
    get() = Angular2TestUtil.getBaseTestDataPath()

  override val defaultDependencies: Map<String, String> =
    mapOf("@angular/core" to "*")

  override val defaultExtension: String
    get() = "ts"

  override fun setUp() {
    if (useTsc) {
      mode = HybridTestMode.CodeInsightFixture
    }
    super.setUp()
    Registry.get("ast.loading.filter").setValue(false, testRootDisposable)
  }

  override fun beforeConfiguredTest() {
    if (useTsc) {
      runInEdtAndWait {
        UIUtil.dispatchAllInvocationEvents()
      }
      TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture, TypeScriptUseServiceState.USE_FOR_EVERYTHING) {
        it is Angular2TypeScriptService
      }
      runInEdtAndWait {
        FileDocumentManager.getInstance().saveAllDocuments()
      }
    }
  }

  override fun afterConfiguredTest() {
  }

  override fun tearDown() {
    if (useTsc) {
      mode = INITIAL_MODE
    }
    super.tearDown()
  }
}