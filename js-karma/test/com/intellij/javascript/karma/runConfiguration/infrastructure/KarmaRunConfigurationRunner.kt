// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration.infrastructure

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.karma.execution.KarmaConfigurationType
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunSettings
import com.intellij.javascript.karma.execution.KarmaServerSettings
import com.intellij.javascript.karma.server.KarmaServer
import com.intellij.javascript.karma.server.KarmaServerRegistry
import com.intellij.javascript.testFramework.runConfigurations.JsRunConfigurationRunner
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.project.Project
import com.intellij.testFramework.PlatformTestUtil
import junit.framework.TestCase.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class KarmaRunConfigurationRunner: JsRunConfigurationRunner<KarmaRunConfiguration, KarmaRunSettings.Builder>() {
  override fun getConfigurationFactory(): ConfigurationFactory = KarmaConfigurationType.getInstance()

  override fun setupConfiguration(
    configuration: KarmaRunConfiguration,
    configureSettings: ((KarmaRunSettings.Builder) -> KarmaRunSettings.Builder)?
  ) {
    if (configureSettings != null) {
      var initialSettingsBuilder = configuration.runSettings.toBuilder()
      val configuredSettingsBuilder = configureSettings(initialSettingsBuilder)
      configuration.runSettings = configuredSettingsBuilder.build()
    }
  }

  override fun executeConfiguration(configuration: KarmaRunConfiguration): ExecutionEnvironment {
    val executor = DefaultRunExecutor.getRunExecutorInstance()

    // Should prepare Karma server before running tests, because the configuration execution has issue with waiting correct state in tests.
    startKarmaServerAndWaitBrowsersReady(configuration.project, configuration, executor)

    return PlatformTestUtil.executeConfigurationAndWait(configuration, executor.id, 60)
  }

  private fun startKarmaServerAndWaitBrowsersReady(project: Project, karmaConfiguration: KarmaRunConfiguration, executor: Executor) {
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
}
