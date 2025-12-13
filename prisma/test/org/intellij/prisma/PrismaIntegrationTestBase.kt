package org.intellij.prisma

import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest

abstract class PrismaIntegrationTestBase : JSTempDirWithNodeInterpreterTest() {
  override fun configureInterpreterVersion(): NodeJsAppRule {
    return NodeJsAppRule.LATEST_22
  }
}