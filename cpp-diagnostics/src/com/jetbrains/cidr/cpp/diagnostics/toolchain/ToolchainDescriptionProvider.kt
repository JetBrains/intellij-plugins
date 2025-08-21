package com.jetbrains.cidr.cpp.diagnostics.toolchain

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.cidr.cpp.diagnostics.CdIndenter
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.system.HostMachine

interface ToolchainDescriptionProvider {
  fun describe(toolchain: CPPToolchains.Toolchain, host: HostMachine, log: CdIndenter): Boolean

  companion object {
    val EP_NAME: ExtensionPointName<ToolchainDescriptionProvider> = ExtensionPointName.create<ToolchainDescriptionProvider>(
      "com.intellij.clion.diagnostics.toolchainDescriptionProvider"
    )

    fun describe(toolchain: CPPToolchains.Toolchain, host: HostMachine, log: CdIndenter): Boolean {
      for (provider in EP_NAME.extensionList) {
        if (provider.describe(toolchain, host, log)) {
          return true
        }
      }

      return false
    }
  }
}