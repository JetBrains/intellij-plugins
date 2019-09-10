// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class MarkdownSupportTest : GraziTestBase(true) {
  fun `test grammar check in file`() {
    runHighlightTestForFile("ide/language/markdown/Example.md")
  }

  fun `test multilanguage support in file`() {
    runHighlightTestForFile("ide/language/markdown/MultiLanguage.md")
  }
}
