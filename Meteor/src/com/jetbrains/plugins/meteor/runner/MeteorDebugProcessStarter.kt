package com.jetbrains.plugins.meteor.runner

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.jetbrains.debugger.wip.WipLocalVmConnection
import com.jetbrains.nodeJs.NodeChromeDebugProcess
import org.jetbrains.debugger.connection.open
import org.jetbrains.wip.IS_METEOR
import java.net.InetSocketAddress

class MeteorDebugProcessStarter(private val isNode8: Boolean,
                                private val finder: MeteorFileFinder,
                                private val socketAddress: InetSocketAddress,
                                private val executionResult: ExecutionResult)
  : XDebugProcessStarter() {

  @Throws(ExecutionException::class)
  override fun start(session: XDebugSession): XDebugProcess {
    val connection = WipLocalVmConnection()
    val process = NodeChromeDebugProcess(session, finder, connection, executionResult)
    connection.executeOnStart { it.putUserData(IS_METEOR, true) }
    connection.open(socketAddress, executionResult.processHandler)
    return process
  }
}
