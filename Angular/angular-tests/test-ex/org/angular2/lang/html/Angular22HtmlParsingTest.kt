// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.testFramework.junit5.TestApplication

@TestApplication
class Angular22HtmlParsingTest : Angular20HtmlParsingTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_22

}
