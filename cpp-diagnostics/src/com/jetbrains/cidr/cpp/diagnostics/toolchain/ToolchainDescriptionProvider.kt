package com.jetbrains.cidr.cpp.diagnostics.toolchain

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.cidr.cpp.diagnostics.model.Reportable
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.system.HostMachine

interface ToolchainDescriptionProvider {
  fun describe(toolchain: CPPToolchains.Toolchain, host: HostMachine): Reportable?

  companion object {
    val EP_NAME: ExtensionPointName<ToolchainDescriptionProvider> = ExtensionPointName.create<ToolchainDescriptionProvider>(
      "com.intellij.clion.diagnostics.toolchainDescriptionProvider"
    )

    fun describe(toolchain: CPPToolchains.Toolchain, host: HostMachine): Reportable? {
      for (provider in EP_NAME.extensionList) {
        provider.describe(toolchain, host)?.let { return it }
      }
      return null
    }
  }
}