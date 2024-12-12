// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration.infrastructure

import com.intellij.execution.Executor
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.javascript.karma.execution.KarmaConfigurationType
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunSettings
import com.intellij.javascript.karma.execution.KarmaServerSettings
import com.intellij.javascript.karma.server.KarmaServer
import com.intellij.javascript.karma.server.KarmaServerRegistry
import com.intellij.javascript.testFramework.runConfigurations.JsTestsRunConfigurationTest
import com.intellij.javascript.testFramework.runConfigurations.outputExtraction.PrintedError
import com.intellij.javascript.testFramework.runConfigurations.outputExtraction.TestsErrorExtractor
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.testFramework.PlatformTestUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

abstract class KarmaRunConfigurationTestsBase: JsTestsRunConfigurationTest<KarmaRunConfiguration, KarmaRunSettings.Builder>() {
  override fun getBasePath(): String? = "/contrib/js-karma/testData/runConfiguration"

  override fun getConfigurationType(): SimpleConfigurationType = KarmaConfigurationType.getInstance()

  override fun setupConfiguration(configuration: KarmaRunConfiguration, configureSettings: ((KarmaRunSettings.Builder) -> KarmaRunSettings.Builder)?) {
    configureSettings?.let {
      var builder = configuration.runSettings.toBuilder()
      it(builder)
      configuration.runSettings = builder.build()
    }
  }

  override fun isStableTestsOrderingInTree(): Boolean = false

  private fun startKarmaServerAndWaitBrowsersReady(karmaConfiguration: KarmaRunConfiguration, executor: Executor) {
    val interpreter = karmaConfiguration.runSettings.interpreterRef.resolveNotNull(project)
    val serverSettings = KarmaServerSettings(
      executor,
      interpreter,
      karmaConfiguration.karmaPackage,
      karmaConfiguration.runSettings,
      karmaConfiguration
    )
    val serverStartedLatch = CountDownLatch(1)
    val startedServer = AtomicReference<KarmaServer?>(null)
    KarmaServerRegistry.getInstance(project).startServer(serverSettings)
      .onSuccess {
        startedServer.set(it)
        serverStartedLatch.countDown()
      }
      .onError {
        fail("Can't start Karma server")
      }

    if (!serverStartedLatch.await(10, TimeUnit.SECONDS)) {
      fail("Can't wait Karma server to start")
    }

    val server = startedServer.get()!!
    try {
      JSTestUtils.waitForConditionWithTimeout(server::areBrowsersReady, 10000)
    } catch (e: Exception) {
      throw RuntimeException("Can't wait for browsers are ready", e)
    }
  }

  override fun executeConfiguration(configuration: KarmaRunConfiguration): ExecutionEnvironment {
    val executor = DefaultRunExecutor.getRunExecutorInstance()

    // Should prepare Karma server before running tests, because the configuration execution has issue with waiting correct state in tests.
    startKarmaServerAndWaitBrowsersReady(configuration, executor)

    return PlatformTestUtil.executeConfigurationAndWait(configuration, executor.id, 60)
  }

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
