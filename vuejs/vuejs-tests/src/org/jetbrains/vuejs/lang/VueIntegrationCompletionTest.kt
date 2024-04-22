// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.nodejs.npm.NpmUtil
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest

abstract class VueIntegrationCompletionTest : JSExternalToolIntegrationTest() {
  override fun getBasePath(): String = vueRelativeTestDataPath() + "/completion"

  fun testImportInWorkspace() {
    copyTestDirectoryAndInstallDependencies()
    myFixture.configureFromTempProjectFile("projects/bar-project/src/App.vue")
    myFixture.type("ut")
    myFixture.completeBasic()
    myFixture.checkResultByFile("${getTestName(true)}/projects/bar-project/src/App.after.vue")
  }

  private fun copyTestDirectoryAndInstallDependencies() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    performNpmInstallUsingPackageManager(project, nodePackage, "package.json")
  }

  override fun configureInterpreterVersion(): NodeJsAppRule = NodeJsAppRule.LATEST_20
}

class VueNpmIntegrationCompletionTest : VueIntegrationCompletionTest() {
  override fun getMainPackageName(): String = NpmUtil.NPM_PACKAGE_NAME
}

class VueYarnIntegrationCompletionTest : VueIntegrationCompletionTest() {
  override fun getMainPackageName(): String = NpmUtil.YARN_PACKAGE_NAME
}