package com.intellij.clion.diagnostics

import com.jetbrains.cidr.CidrPathManager

internal val cppDiagnosticsTestDataRoot = CidrPathManager.cidrSourcesRoot.parent.resolve("contrib/cpp-diagnostics/cmake/testData")
