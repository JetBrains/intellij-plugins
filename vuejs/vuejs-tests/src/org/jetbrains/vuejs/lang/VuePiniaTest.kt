// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest

class VuePiniaTest : JSTempDirWithNodeInterpreterTest() {
  override fun getBasePath(): String {
    return vueRelativeTestDataPath() + "/pinia"
  }

  fun testDefineStoreInJSFile() {
    doCopyDirectoryWithNpmInstallHighlightingTest(".vue")
    myFixture.configureFromTempProjectFile(getTestName(false) + "_2.js")
    myFixture.checkHighlighting()
  }
}