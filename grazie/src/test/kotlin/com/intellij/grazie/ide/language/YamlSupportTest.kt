package com.intellij.grazie.ide.language

import com.intellij.grazie.GrazieTestBase

class YamlSupportTest : GrazieTestBase() {
  fun `test grammar check in yaml file`() {
    runHighlightTestForFile("ide/language/yaml/Example.yaml")
  }
}
