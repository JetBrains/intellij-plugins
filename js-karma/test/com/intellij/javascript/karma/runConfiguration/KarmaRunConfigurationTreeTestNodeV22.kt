// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration

import com.intellij.idea.IJIgnore
import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.karma.runConfiguration.infrastructure.KarmaRunConfigurationTestsBase
import com.intellij.javascript.karma.scope.KarmaScopeKind
import com.intellij.javascript.testFramework.runConfigurations.fixturesDsl.PassedTest

@IJIgnore(issue = "For local development usage. Will be enabled on CI after setting up headless Chrome on CI for these kind of tests")
class KarmaRunConfigurationTreeTestNodeV22: KarmaRunConfigurationTestsBase() {
  override fun configureInterpreterVersion(): NodeJsAppRule = NodeJsAppRule.LATEST_22

  // WEB-73511
  fun `test angularV20`() {
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.TEST_FILE)
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/app/app.spec.ts"))
          .setConfigPath(getAbsolutePathToProjectDirOrFile("/karma.conf.js"))
      },
      PassedTest(
        "karma.conf.js",
        PassedTest(
          "Chrome Headless .*",
          PassedTest(
            "App",
            PassedTest("should create the app"),
            PassedTest("should render title"),
          )
        ).withNameAsRegex()
      )
    )
  }
}