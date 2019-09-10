// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class XMLSupportTest : GraziTestBase(true) {
    fun `test grammar check in xsd file`() {
        runHighlightTestForFile("ide/language/xml/Example.xsd")
    }

    fun `test grammar check in xml file`() {
        runHighlightTestForFile("ide/language/xml/Example.xml")
    }

    fun `test grammar check in html file`() {
        runHighlightTestForFile("ide/language/xml/Example.html")
    }
}
