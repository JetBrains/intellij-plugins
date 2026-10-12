// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.formatter.HtmlFormatterTest
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.lang.AstroFileType
import org.junit.jupiter.api.Test

@TestApplication
class AstroHtmlFormatterTest : HtmlFormatterTest() {
  override fun getFileType(fileName: String): FileType {
    return AstroFileType
  }

  @Test
  override fun testWeb18909() {
    // replace '{' with '[', otherwise we have JSX expressions
    doTextTest("""
                 <!doctype html>
                 <html>
                 <body>
                 <section>
                     <pre><code class="language-javascript">function test(i) [
                     if (i===1) [
                         console.log('output');
                     }
                 }</code></pre>
                 </section>
                 </body>
                 </html>
                 """.trimIndent(),
               """
                 <!doctype html>
                 <html>
                 <body>
                 <section>
                     <pre><code class="language-javascript">function test(i) [
                     if (i===1) [
                         console.log('output');
                     }
                 }</code></pre>
                 </section>
                 </body>
                 </html>
                 """.trimIndent())
  }

  @Test
  override fun test1() {
    // ignore - doctype formatting
  }

  @Test
  override fun test2() {
    // ignore - doctype formatting
  }

  @Test
  override fun test3() {
    // ignore - doctype formatting
  }

  @Test
  override fun test4() {
    // ignore - doctype formatting
  }

}