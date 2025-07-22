// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.runConfiguration.infrastructure

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView
import com.intellij.javascript.karma.execution.KarmaConfigurationType
import com.intellij.javascript.karma.execution.KarmaConsoleView
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunSettings
import com.intellij.javascript.karma.execution.KarmaServerSettings
import com.intellij.javascript.karma.server.KarmaServer
import com.intellij.javascript.karma.server.KarmaServerRegistry
import com.intellij.javascript.karma.util.ArchivedOutputListener
import com.intellij.javascript.testFramework.runConfigurations.JsRunConfigurationRunner
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.containers.CollectionFactory
import junit.framework.TestCase.fail
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class KarmaRunConfigurationRunner: JsRunConfigurationRunner<KarmaRunConfiguration, KarmaRunSettings.Builder>() {
  private val serversOutputListeners: ConcurrentMap<KarmaServer, KarmaServerOutputListener> = CollectionFactory.createConcurrentWeakMap()

  override fun getConfigurationFactory(): ConfigurationFactory = KarmaConfigurationType.getInstance()

  override fun setupConfiguration(
    configuration: KarmaRunConfiguration,
    configureSettings: ((KarmaRunSettings.Builder) -> KarmaRunSettings.Builder)?
  ) {
    if (configureSettings != null) {
      val initialSettingsBuilder = configuration.runSettings.toBuilder()
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

  override fun getFullConsoleOutput(smtRunnerConsoleView: SMTRunnerConsoleView): String {
    val fullConsoleOutput = super.getFullConsoleOutput(smtRunnerConsoleView)
    if (smtRunnerConsoleView is KarmaConsoleView) {
      val karmaServer = smtRunnerConsoleView.karmaServer
      val serverOutputListener = serversOutputListeners[karmaServer]
      serverOutputListener?.outputText.let { serverOutputText ->
        // some errors are displayed only in the Karma server output, for these cases we should have the output too
        return "---[KarmaServerOutput]---\n${serverOutputText}\n\n---[KarmaTestsOutput]---\n${fullConsoleOutput}"
      }
    }
    return fullConsoleOutput
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
    val serverOutputListener = KarmaServerOutputListener()
    KarmaServerRegistry.getInstance(project).startServer(serverSettings)
      .onSuccess {
        startedServer.set(it)
        it.processOutputManager.addOutputListener(serverOutputListener)
        serversOutputListeners.put(it, serverOutputListener)
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
      JSTestUtils.waitForConditionWithTimeout(server::areBrowsersReady, 20000)
    } catch (e: Exception) {
      val serverOutputText = serverOutputListener.outputText
      throw RuntimeException("Can't wait for browsers are ready\n---[KarmaServerOutput]---\n${serverOutputText}", e)
    }
  }

  internal class KarmaServerOutputListener: ArchivedOutputListener {
    val texts: MutableList<String> = mutableListOf()

    override fun onOutputAvailable(text: String, outputType: Key<*>?, archived: Boolean) {
      texts.add(text)
    }

    val outputText: String get() = texts.joinToString("")
  }
}
