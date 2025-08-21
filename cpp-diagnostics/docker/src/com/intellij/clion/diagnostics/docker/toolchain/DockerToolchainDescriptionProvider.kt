package com.intellij.clion.diagnostics.docker.toolchain

import com.jetbrains.cidr.cpp.diagnostics.CdIndenter
import com.jetbrains.cidr.cpp.diagnostics.toolchain.ToolchainDescriptionProvider
import com.jetbrains.cidr.cpp.toolchains.CPPToolSet
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.cpp.toolchains.docker.getDockerServerTypeId
import com.jetbrains.cidr.cpp.toolchains.docker.host.DockerHostBase
import com.jetbrains.cidr.system.HostMachine

class DockerToolchainDescriptionProvider: ToolchainDescriptionProvider {
  override fun describe(toolchain: CPPToolchains.Toolchain, host: HostMachine, log: CdIndenter): Boolean {
    if (toolchain.toolSetKind != CPPToolSet.Kind.DOCKER) {
      return false
    }

    if (host !is DockerHostBase<*>) {
      throw IllegalStateException("Host should be DockerHostBase for Docker toolchain")
    }

    val id = host.getDockerServerTypeId()

    log.put("Docker Server Type: ${id ?: "(null)"}")

    return true
  }
}