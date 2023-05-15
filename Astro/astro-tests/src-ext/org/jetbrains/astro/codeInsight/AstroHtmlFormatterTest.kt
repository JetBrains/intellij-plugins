// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.formatter.HtmlFormatterTest
import org.jetbrains.astro.lang.AstroFileType

class AstroHtmlFormatterTest : HtmlFormatterTest() {
  override fun getFileType(fileName: String): FileType {
    return AstroFileType.INSTANCE
  }

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

  override fun test1() {
    // ignore - doctype formatting
  }

  override fun test2() {
    // ignore - doctype formatting
  }

  override fun test3() {
    // ignore - doctype formatting
  }

  override fun test4() {
    // ignore - doctype formatting
  }

}