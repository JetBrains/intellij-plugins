// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration

import com.intellij.idea.IJIgnore
import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.karma.runConfiguration.infrastructure.KarmaRunConfigurationTestsBase
import com.intellij.javascript.karma.scope.KarmaScopeKind
import com.intellij.javascript.testFramework.runConfigurations.fixturesDsl.FailedTest

@IJIgnore(issue = "For local development usage. Will be enabled on CI after setting up headless Chrome on CI for these kind of tests")
class KarmaRunConfigurationTreeTestNodeV23: KarmaRunConfigurationTestsBase() {

  override fun configureInterpreterVersion(): NodeJsAppRule = NodeJsAppRule.LATEST_23

  fun `test failedTestInAngularNodejsV23`() {
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.TEST)
          .setTestNames(listOf("AppComponent", "Title should be correct"))
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/app/app.component.spec.ts"))
      },
      FailedTest(
        "Chrome Headless .*",
        FailedTest(
          "AppComponent",
          FailedTest("Title should be correct")
        )
      ).withNameAsRegex()
    )
  }
}
