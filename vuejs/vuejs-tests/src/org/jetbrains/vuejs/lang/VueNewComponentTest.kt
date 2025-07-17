// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.testFramework.web.filterOutStandardHtmlSymbols
import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.vuejs.VueProjects.isTypeScriptProjectDirectory
import org.jetbrains.vuejs.VueTestCase

class VueNewComponentTest : VueTestCase("new_component") {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  private fun doProjectLanguageCheck(
    isTypeScript: Boolean,
  ) {
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "index.html",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

    val componentsDirectory = PsiUtilCore.findFileSystemItem(
      myFixture.project,
      myFixture.findFileInTempDir("core/components"),
    ) as PsiDirectory

    assertEquals(
      isTypeScript,
      isTypeScriptProjectDirectory(componentsDirectory),
    )
  }

  fun testProjectLanguageIsJavaScript() {
    doProjectLanguageCheck(isTypeScript = false)
  }

  fun testProjectLanguageIsTypeScript() {
    doProjectLanguageCheck(isTypeScript = true)
  }
}

