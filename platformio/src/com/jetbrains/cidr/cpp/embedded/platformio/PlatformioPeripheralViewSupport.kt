package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.clion.embedded.debugger.peripheralview.PeripheralViewSupport
import com.intellij.openapi.components.service
import com.intellij.openapi.util.io.toNioPathOrNull
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess
import java.nio.file.Path

internal class PlatformioPeripheralViewSupport : PeripheralViewSupport {
  override fun isAvailable(process: CidrDebugProcess): Boolean = process.session.runProfile is PlatformioDebugConfiguration

  override suspend fun findSvdFiles(process: CidrDebugProcess): List<Path> =
    listOfNotNull(process.project.service<PlatformioService>().svdPath?.toNioPathOrNull())
}
