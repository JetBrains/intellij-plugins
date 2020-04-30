package com.jetbrains.cidr.cpp.diagnostics

import com.jetbrains.cidr.cpp.toolchains.CPPToolchains

fun collectToolchains(): String {
  val log = CdIndenter()

  val cppToolchains = CPPToolchains.getInstance()
  log.put("Default toolchain: ${cppToolchains.defaultToolchain?.name ?: "UNKNOWN"}")

  log.scope {
    for (toolchain in cppToolchains.toolchains) {
      log.put("Toolchain ${toolchain.name}")
      log.scope {
        // todo: add more info; merge with "Show Remote Hosts Info" action
        log.put("OS: ${toolchain.osType}")
        log.put("Kind: ${toolchain.toolSetKind}")
      }
    }
  }

  return log.result
}
