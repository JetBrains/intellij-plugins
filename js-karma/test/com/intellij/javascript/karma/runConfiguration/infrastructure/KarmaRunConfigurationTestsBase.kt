// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration.infrastructure

import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunSettings
import com.intellij.javascript.testFramework.runConfigurations.JsTestsRunConfigurationTest
import com.intellij.javascript.testFramework.runConfigurations.outputExtraction.PrintedError
import com.intellij.javascript.testFramework.runConfigurations.outputExtraction.TestsErrorExtractor

abstract class KarmaRunConfigurationTestsBase: JsTestsRunConfigurationTest<KarmaRunConfiguration, KarmaRunSettings.Builder>() {
  override fun getBasePath(): String? = "/contrib/js-karma/testData/runConfiguration"

  override fun createRunConfigurationRunner(): KarmaRunConfigurationRunner = KarmaRunConfigurationRunner()

  override fun isStableTestsOrderingInTree(): Boolean = false

  override fun tryExtractError(smTestProxy: SMTestProxy): PrintedError? {
    /**
     * In Karma context, SMTestProxy doesn't print to {com.intellij.execution.testframework.Printer}.
     * For we try to work with error data directly
     */
    val errorMessage = smTestProxy.errorMessage
    val stacktrace = smTestProxy.stacktrace
    if (errorMessage != null && stacktrace != null) {
      return TestsErrorExtractor(smTestProxy).tryExtractPrintedError(errorMessage + "\n" + stacktrace)
    }
    return null
  }
}
