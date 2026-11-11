// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration

import com.intellij.idea.IJIgnore
import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.karma.runConfiguration.infrastructure.KarmaRunConfigurationTestsBase
import com.intellij.javascript.karma.scope.KarmaScopeKind
import com.intellij.javascript.testFramework.runConfigurations.fixturesDsl.FailedTest
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@IJIgnore(issue = "For local development usage. Will be enabled on CI after setting up headless Chrome on CI for these kind of tests")
@TestApplication
class KarmaRunConfigurationTreeTestNodeV23: KarmaRunConfigurationTestsBase(NodeJsAppRule.LATEST_23) {
  @Test
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
