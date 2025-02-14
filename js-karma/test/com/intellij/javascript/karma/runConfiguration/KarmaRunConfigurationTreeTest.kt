// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration

import com.intellij.idea.IJIgnore
import com.intellij.javascript.karma.runConfiguration.infrastructure.KarmaRunConfigurationTestsBase
import com.intellij.javascript.karma.scope.KarmaScopeKind
import com.intellij.javascript.testFramework.runConfigurations.fixturesDsl.*

@IJIgnore(issue = "For local development usage. Will be enabled on CI after setting up headless Chrome on CI for these kind of tests")
class KarmaRunConfigurationTreeTest: KarmaRunConfigurationTestsBase() {

  fun `test jasmineFailedTest`() {
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.TEST_FILE)
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/user.spec.js"))
          .setConfigPath(getAbsolutePathToProjectDirOrFile("/karma.conf.js"))
      },
      FailedTest(
        "karma.conf.js",
        FailedTest(
          "Chrome Headless .*",
          FailedTest(
            "user",
            FailedTest(
              "should be tested",
              OutputError("Expected true to be false.", Location("/src/user.spec.js", 3, 18))
            ),
            PassedTest("should be tested 2"),
          ),
        ).withNameAsRegex()
      )
    )
  }

  fun `test jasmineNavigateFromTree`() {
    val baseLocation = Location("/src/user.spec.js")
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.TEST_FILE)
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/user.spec.js"))
          .setConfigPath(getAbsolutePathToProjectDirOrFile("/karma.conf.js"))
      },
      PassedTest(
        "karma.conf.js",
        Location("/karma.conf.js", 1, 1),
        PassedTest(
          "Chrome Headless .*",
          PassedTest(
            "user",
            baseLocation.position(1, 10),
            PassedTest("should be tested", baseLocation.position(2, 6)),
            PassedTest("should be tested 2", baseLocation.position(5, 6)),
          ),
        ).withNameAsRegex()
      )
    )
  }

  fun `test qUnitNavigateFromTree`() {
    val baseLocation = Location("/src/user.spec.js", shouldVerifyTestName = false)
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.TEST_FILE)
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/user.spec.js"))
          .setConfigPath(getAbsolutePathToProjectDirOrFile("/karma.conf.js"))
      },
      PassedTest(
        "karma.conf.js",
        Location("/karma.conf.js", 1, 1),
        PassedTest(
          "Chrome Headless .*",
          PassedTest(
            "user",
            baseLocation.position(5, 1),
            PassedTest("should be tested", baseLocation.position(7, 1)),
            PassedTest("should be tested 2", baseLocation.position(11, 1)),
          ),
        ).withNameAsRegex()
      )
    )
  }

  fun `test scopeJasmineSuite`() {
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.SUITE)
          .setTestNames(listOf("user"))
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/user.spec.js"))
          .setConfigPath(getAbsolutePathToProjectDirOrFile("/karma.conf.js"))
      },
      PassedTest(
        "karma.conf.js",
        PassedTest(
          "Chrome Headless .*",
          PassedTest(
            "user",
            PassedTest("should be tested"),
            PassedTest("should be tested 2")
          )
        ).withNameAsRegex()
      )
    )
  }

  fun `test scopeJasmineTest`() {
    doTreeTest(
      {
        it.setScopeKind(KarmaScopeKind.TEST)
          .setTestNames(listOf("user", "should be tested 2"))
          .setTestFilePath(getAbsolutePathToProjectDirOrFile("/src/user.spec.js"))
          .setConfigPath(getAbsolutePathToProjectDirOrFile("/karma.conf.js"))
      },
      PassedTest(
        "karma.conf.js",
        PassedTest(
          "Chrome Headless .*",
          PassedTest(
            "user",
            PassedTest("should be tested 2")
          )
        ).withNameAsRegex()
      )
    )
  }
}
