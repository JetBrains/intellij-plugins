// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.nodejs.npm.NpmUtil
import com.intellij.lang.javascript.linter.JSExternalToolFixture.Companion.jsExternalToolFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

/** The subclasses run the tests again with another package manager [mainPackageName]. */
abstract class VueIntegrationCompletionTest(mainPackageName: String) {
  private val vue by jsExternalToolFixture(
    JSTempDirFixtureOptions.forBasePath(vueRelativeTestDataPath() + "/completion")
      .withMainPackage(mainPackageName)
      .withNodeVersion(NodeJsAppRule.LATEST_20))

  @Test
  fun testImportInWorkspace() {
    copyTestDirectoryAndInstallDependencies()
    vue.fixture.configureFromTempProjectFile("projects/bar-project/src/App.vue")
    vue.fixture.type("ut")
    vue.fixture.completeBasic()
    vue.fixture.checkResultByFile("${vue.getTestName(true)}/projects/bar-project/src/App.after.vue")
  }

  private fun copyTestDirectoryAndInstallDependencies() {
    vue.fixture.copyDirectoryToProject(vue.getTestName(true), "")
    vue.performNpmInstallUsingPackageManager(vue.project, vue.nodePackage, "package.json")
  }
}

@TestApplication
class VueNpmIntegrationCompletionTest : VueIntegrationCompletionTest(NpmUtil.NPM_PACKAGE_NAME)

@TestApplication
class VueYarnIntegrationCompletionTest : VueIntegrationCompletionTest(NpmUtil.YARN_PACKAGE_NAME)
