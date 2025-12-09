// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("IO_FILE_USAGE")

package org.jetbrains.idea.perforce.checkout

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.perforce.application.ConnectionKey
import org.jetbrains.idea.perforce.perforce.ConnectionId
import org.jetbrains.idea.perforce.perforce.ExecResult
import org.jetbrains.idea.perforce.perforce.P4File
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI
import org.jetbrains.idea.perforce.perforce.connections.PerforceMultipleConnections
import java.io.File

internal class PerforceCloneConnectionManager(
  private val connection: P4Connection,
) : PerforceConnectionManagerI {
  override fun getMultipleConnectionObject(): PerforceMultipleConnections? = null
  override fun getAllConnections(): Map<VirtualFile, P4Connection> = emptyMap()
  override fun getConnectionForFile(file: File): P4Connection = connection
  override fun getConnectionForFile(file: P4File): P4Connection = connection
  override fun getConnectionForFile(file: VirtualFile): P4Connection = connection
  override fun isSingletonConnectionUsed(): Boolean = true
  override fun updateConnections() {}
  override fun isUnderProjectConnections(file: File): Boolean = true
  override fun isInitialized(): Boolean = true
}

internal class PerforceCloneConnection(
  private val workDir: File,
  private val connArgs: Array<String>,
) : AbstractP4Connection() {
  private val connectionId = ConnectionId(null, workDir.absolutePath)

  override fun runP4Command(
    parameters: PerforcePhysicalConnectionParametersI,
    p4args: Array<String>,
    retVal: ExecResult,
    inputStream: StringBuffer?,
  ) {
    runP4CommandImpl(parameters, connArgs, p4args, retVal, inputStream)
  }

  override fun getWorkingDirectory(): File = workDir

  override fun getConnectionKey(): ConnectionKey = ConnectionKey("", "", "")

  override fun getId(): ConnectionId = connectionId

  override fun handlesFile(file: File): Boolean = true
}
