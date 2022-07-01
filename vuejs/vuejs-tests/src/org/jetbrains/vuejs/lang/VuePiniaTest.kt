// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase

class VuePiniaTest : JSTempDirWithNodeInterpreterTest() {
  override fun getBasePath(): String {
    return vueRelativeTestDataPath() + "/pinia"
  }

  fun testDefineStoreInJSFile() {
    // WEB-54970
    doCopyDirectoryWithNpmInstallHighlightingTest(".vue")
    myFixture.configureFromTempProjectFile(getTestName(false) + "_2.js")
    myFixture.checkHighlighting()
  }

  fun testDefineStoreInJSFile_InnerVueDemi() {
    // WEB-54970
    myFixture.copyDirectoryToProject(getTestName(false), "")
    performNpmInstallForPackageJson("package.json")

    myFixture.configureFromTempProjectFile("usage_ts.ts")
    val resolveTs = JSTestUtils.getGotoDeclarationTarget(myFixture)!!
    UsefulTestCase.assertInstanceOf(resolveTs, JSProperty::class.java)
    TestCase.assertEquals("user_ts.ts", resolveTs.containingFile.name)

    myFixture.configureFromTempProjectFile("usage_js.js")
    val resolveJs = JSTestUtils.getGotoDeclarationTarget(myFixture)!!
    UsefulTestCase.assertInstanceOf(resolveJs, JSProperty::class.java)
    TestCase.assertEquals("user.js", resolveJs.containingFile.name)
  }
}