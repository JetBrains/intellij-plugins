// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.perforce.perforce.connections

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ArrayUtil
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.*
import org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl
import org.jetbrains.idea.perforce.perforce.login.LoginSupport
import java.io.File

/**
 * LoginSupport implementation for test connections.
 * Prompts for password if needed.
 */
internal class TestLoginManager(
  private val project: Project,
  private val settings: PerforceSettings,
  private val connectionManager: PerforceConnectionManagerI,
) : LoginSupport {

  @Throws(VcsException::class)
  override fun silentLogin(connection: P4Connection): Boolean {
    var password = if (connection is P4ParametersConnection)
      connection.parameters.password
    else
      settings.passwd

    val loginPerformer = LoginPerformerImpl(project, connection, connectionManager)
    if (password != null && loginPerformer.login(password).isSuccess) {
      return true
    }

    while (true) {
      password = settings.requestForPassword(if (settings.useP4CONFIG) connection else null)
      if (password == null) return false
      val login = loginPerformer.login(password)
      if (login.isSuccess) {
        PerforceConnectionManager.getInstance(project).updateConnections()
        return true
      }
    }
  }

  override fun notLogged(connection: P4Connection) {}
}

internal class TestPerforceConnectionManager(
  private val project: Project,
  private val singleton: Boolean,
) : PerforceConnectionManagerI {

  private var singletonConnection: P4Connection? = null
  private var mc: PerforceMultipleConnections? = null

  fun setSingletonConnection(connection: P4Connection?) {
    singletonConnection = connection
  }

  fun setMultipleConnectionObject(mc: PerforceMultipleConnections?) {
    this.mc = mc
  }

  override fun getMultipleConnectionObject(): PerforceMultipleConnections? = mc

  override fun getAllConnections(): Map<VirtualFile, P4Connection> {
    if (singleton) {
      val result = LinkedHashMap<VirtualFile, P4Connection>()
      for (root in ProjectLevelVcsManager.getInstance(project).getRootsUnderVcs(PerforceVcs.getInstance(project))) {
        result[root] = singletonConnection!!
      }
      return result
    }
    return mc!!.allConnections
  }

  override fun getConnectionForFile(file: File): P4Connection? {
    return if (singleton) {
      singletonConnection
    }
    else {
      val vf = PerforceConnectionManager.findNearestLiveParentFor(file) ?: return null
      mc!!.getConnection(vf)
    }
  }

  override fun getConnectionForFile(file: P4File): P4Connection? =
    if (singleton) singletonConnection else getConnectionForFile(file.localFile)

  override fun getConnectionForFile(file: VirtualFile): P4Connection? =
    if (singleton) singletonConnection else mc!!.getConnection(file)

  override fun isSingletonConnectionUsed() = singleton
  override fun updateConnections() {}
  override fun isUnderProjectConnections(file: File) = true
  override fun isInitialized() = true
}

/**
 * P4ParametersConnection extension that includes password in connection args.
 * Used for test connections with explicit credentials.
 */
internal class TestParametersConnection(
  parameters: P4ConnectionParameters,
  connectionId: ConnectionId,
) : P4ParametersConnection(parameters, connectionId) {

  override fun runP4Command(
    parameters: PerforcePhysicalConnectionParametersI?,
    p4args: Array<out String?>?,
    retVal: ExecResult?,
    inputStream: StringBuffer?,
  ) {
    val connArgs = buildConnectionArgs()
    runP4CommandImpl(parameters!!, connArgs, p4args, retVal, inputStream)
  }

  private fun buildConnectionArgs(): Array<String?> {
    val args = mutableListOf<String?>()
    myParameters.server?.takeIf { it.isNotBlank() }?.let {
      args.add("-p")
      args.add(it)
    }
    myParameters.user?.takeIf { it.isNotBlank() }?.let {
      args.add("-u")
      args.add(it)
    }
    myParameters.password?.takeIf { it.isNotBlank() }?.let {
      args.add("-P")
      args.add(it)
    }
    myParameters.client?.takeIf { it.isNotBlank() }?.let {
      args.add("-c")
      args.add(it)
    }
    return ArrayUtil.toStringArray(args)
  }
}
