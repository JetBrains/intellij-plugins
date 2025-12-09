// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("IO_FILE_USAGE")

package org.jetbrains.idea.perforce.checkout

import com.intellij.openapi.project.Project
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.TestLoginManager
import java.io.File

internal object PerforceCloneRunnerFactory {

  fun createRunner(project: Project, params: PerforceCloneParams, workDir: File): PerforceRunner {
    val settings = PerforceSettings(project).apply {
      useP4CONFIG = false
      port = params.server
      user = params.user
      client = params.client
      USE_LOGIN = false
    }

    val connArgs = buildConnectArgs(params)
    val connection = PerforceCloneConnection(workDir, connArgs)
    val connectionManager = PerforceCloneConnectionManager(connection)
    val loginManager = TestLoginManager(project, settings, connectionManager)

    return PerforceRunner(connectionManager, settings, loginManager)
  }

  fun buildConnectArgs(params: PerforceCloneParams): Array<String> {
    val args = mutableListOf<String>()
    if (params.server.isNotBlank()) {
      args.add("-p")
      args.add(params.server)
    }
    if (params.user.isNotBlank()) {
      args.add("-u")
      args.add(params.user)
    }
    if (params.password.isNotBlank()) {
      args.add("-P")
      args.add(params.password)
    }
    if (params.client.isNotBlank()) {
      args.add("-c")
      args.add(params.client)
    }
    return args.toTypedArray()
  }
}
