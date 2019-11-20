package com.intellij.grazie.ide.language

import com.intellij.grazie.GrazieTestBase
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess

class YamlSupportTest : GrazieTestBase() {
  override fun setUp() {
    super.setUp()
    VfsRootAccess.allowRootAccess(testRootDisposable, YamlSupportTest::class.java.getResource("/jsonSchemas").path)
  }

  fun `test grammar check in yaml file`() {
    runHighlightTestForFile("ide/language/yaml/Example.yaml")
  }
}
