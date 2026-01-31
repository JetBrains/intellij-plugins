// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.perforce.perforce.connections

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.annotations.Nls
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.ClientRootsCache
import org.jetbrains.idea.perforce.application.ConnectionDiagnoseRefresher
import org.jetbrains.idea.perforce.application.ConnectionInfo
import org.jetbrains.idea.perforce.application.P4RootsInformation
import org.jetbrains.idea.perforce.application.PerforceClientRootsChecker
import org.jetbrains.idea.perforce.application.PerforceInfoAndClient
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import java.nio.file.Path

internal class PerforceConnectionTester(
  private val project: Project,
  private val settings: PerforceSettings,
) {
  private val connectionManager = TestPerforceConnectionManager(project, !settings.useP4CONFIG)
  private val loginManager = TestLoginManager(project, settings, connectionManager)

  /**
   * Test Perforce connection.
   * Uses P4CONFIG mode [P4ConfigConnectionTestDataProvider] or existing connection mode based if [PerforceSettings.useP4CONFIG] set.
   *
   * @param connection the P4Connection to test (required for connection mode, ignored for P4CONFIG mode)
   * @param clientRoot optional directory to use to check connection. Use project root if not specified.
   * @return TestConnectionResult with checker, refresher (P4CONFIG mode), or cancellation status
   */
  fun testConnection(connection: P4Connection? = null, clientRoot: Path? = null): TestConnectionResult {
    return if (settings.useP4CONFIG) {
      testConnectionWithP4Config()
    }
    else {
      requireNotNull(connection) { "Connection is required for existing connection mode" }
      testExistingConnection(connection, clientRoot)
    }
  }

  private fun testExistingConnection(connection: P4Connection, clientRoot: Path?): TestConnectionResult {
    connectionManager.setSingletonConnection(connection)
    var checker: PerforceClientRootsChecker? = null
    val runner = PerforceRunner(connectionManager, settings, loginManager)

    val isSuccess = ProgressManager.getInstance().runProcessWithProgressSynchronously(
      {
        var allConnections = connectionManager.getAllConnections()
        val clientRootVf = clientRoot?.let { LocalFileSystem.getInstance().refreshAndFindFileByNioFile(it) }
        if (allConnections.isEmpty() && clientRootVf != null) {
          allConnections = mapOf(clientRootVf to connection)
        }
        val cache = if (clientRoot == null) ClientRootsCache.getClientRootsCache(project) else ClientRootsCache()
        val info = PerforceInfoAndClient.calculateInfos(allConnections.values, runner, cache)
        checker = PerforceClientRootsChecker(info, allConnections, clientRootVf)
      },
      PerforceBundle.message("connection.test"),
      true,
      project
    )

    return TestConnectionResult(
      isSuccess = isSuccess && checker?.hasAnyErrors() != true,
      isCancelled = !isSuccess,
      info = checker
    )
  }

  private fun testConnectionWithP4Config(): TestConnectionResult {
    val runner = PerforceRunner(connectionManager, settings, loginManager)
    val refresher = P4ConfigConnectionTestDataProvider(project, connectionManager, runner)

    val isSuccess = ProgressManager.getInstance().runProcessWithProgressSynchronously(
      { refresher.refresh() },
      PerforceBundle.message("connection.test"),
      true,
      project
    )

    val checker = if (isSuccess) refresher.getP4RootsInformation() else null
    return TestConnectionResult(
      isSuccess = isSuccess && checker?.hasAnyErrors() != true,
      isCancelled = !isSuccess,
      info = checker,
      refresher = if (isSuccess) refresher else null
    )
  }

  data class TestConnectionResult(
    val isSuccess: Boolean,
    val isCancelled: Boolean,
    val info: P4RootsInformation?,
    val refresher: ConnectionDiagnoseRefresher? = null,
    val errorMessage: @Nls String? = null,
  )

  private class P4ConfigConnectionTestDataProvider(
    private val project: Project,
    private val connectionManager: TestPerforceConnectionManager,
    private val runner: PerforceRunner,
  ) : ConnectionDiagnoseRefresher {
    private var checker = PerforceClientRootsChecker()
    private var info = emptyMap<P4Connection, ConnectionInfo>()
    private var mc: PerforceMultipleConnections? = null

    override fun refresh() {
      val calculator = P4ConnectionCalculator(project)
      calculator.execute()
      mc = calculator.multipleConnections
      val map = mc!!.allConnections
      connectionManager.setMultipleConnectionObject(mc)
      info = PerforceInfoAndClient.recalculateInfos(
        info, map.values, runner, ClientRootsCache.getClientRootsCache(project)
      ).newInfo
      checker = PerforceClientRootsChecker(info, map)
    }

    override fun getMultipleConnections(): PerforceMultipleConnections = mc!!
    override fun getP4RootsInformation(): P4RootsInformation = checker
  }
}
