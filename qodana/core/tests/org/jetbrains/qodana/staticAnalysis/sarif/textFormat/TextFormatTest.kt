package org.jetbrains.qodana.staticAnalysis.sarif.textFormat

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

@Suppress("HtmlRequiredTitleElement")
class TextFormatTest {
  @Test
  fun `test simple html to markdown`() {
    @Language("HTML")
    val html = """
      <html>
      <body>
      <p>Text</p>
      </body>
      </html>
    """.trimIndent()

    @Language("Markdown")
    val expectedMarkdown = """
      Text
    """.trimIndent()

    assertThat(htmlToMarkdown(html)).isEqualTo(expectedMarkdown)
  }

  @Test
  fun `test simple html without html and body elements to markdown`() {
    @Language("HTML")
    val html = """
      <p>Text</p>
    """.trimIndent()

    @Language("Markdown")
    val expectedMarkdown = """
      Text
    """.trimIndent()

    assertThat(htmlToMarkdown(html)).isEqualTo(expectedMarkdown)
  }

  @Test
  fun `test complex html to markdown`() {
    @Language("HTML")
    val html = """
      <html>
      <body>
      <p>Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.</p>
      <p>Types of function parameters can be specified in
      docstrings or in Python 3 function annotations.</p>
      <p><strong>Example:</strong></p>
      <pre><code>def foo() -> int:
          return &quot;abc&quot; # Expected int, got str
      
      
      a: str
      a = foo() # Expected str, got int
      </code></pre>
      <p>With the quick-fix, you can modify the problematic types:</p>
      <pre><code>def foo() -> str:
          return &quot;abc&quot;
      
      
      a: str
      a = foo()
      </code></pre>
      </body>
      </html>
    """.trimIndent()

    @Language("Markdown")
    val expectedMarkdown = """
      Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.
      
      Types of function parameters can be specified in
      docstrings or in Python 3 function annotations.
      
      **Example:**
      
          def foo() -> int:
              return "abc" # Expected int, got str
      
      
          a: str
          a = foo() # Expected str, got int
      
      With the quick-fix, you can modify the problematic types:
      
          def foo() -> str:
              return "abc"
      
      
          a: str
          a = foo()
      
    """.trimIndent()

    assertThat(htmlToMarkdown(html)).isEqualTo(expectedMarkdown)
  }

  @Test
  fun `test special symbols in nested html inside pre code block`() {
    @Language("HTML")
    val html = """
      <html>
      <body>
      <pre><code>
      special symbols: <b>\*~^&<>[]|</b>`
      </code></pre>
      </body>
      </html>
    """.trimIndent()

    val expectedMarkdown = """
    |
    |    special symbols: \*~^&<>[]|`
    |
    """.trimMargin()

    assertThat(htmlToMarkdown(html)).isEqualTo(expectedMarkdown)
  }

  @Test
  fun `test unclosed br tags are rendered like closed br tags`() {
    @Language("HTML")
    val closed = """
      <html>
      <body>
      before
      <br/>
      after
      </body>
      </html>
    """.trimIndent()

    @Language("HTML")
    val open = """
      <html>
      <body>
      before
      <br>
      after
      </body>
      </html>
    """.trimIndent()
    assertThat(htmlToMarkdown(open)).isEqualTo(htmlToMarkdown(closed))
  }

  @Test
  fun `test unclosed br tags in html are rendered as line breaks`() {
    @Language("HTML")
    val complexHtml = """
      <html>
      <body>
      Reports a <code>typeof</code> or <code>instanceof</code> unsound type guard check.
      The <code>typeof x</code> type guard can be unsound in one of the following two cases:
      <ul>
      <li><code>typeof x</code> never corresponds to the specified value (for example, <code>typeof x === 'number'</code> when <code>x</code> is of the type 'string | boolean')</li>
      <li><code>typeof x</code> always corresponds to the specified value (for example,  <code>typeof x === 'string'</code> when <code>x</code> is of the type 'string')</li>
      </ul>
      <br>
      The <code>x instanceof A</code> type guard can be unsound in one of the following two cases:
      <ul>
        <li>The type of <code>x</code> is not related to <code>A</code></li>
        <li>The type of <code>x</code> is <code>A</code> or a subtype of <code>A</code></li>
      </ul>
      </body>
      </html>
    """.trimIndent()

    val expected = """
      Reports a `typeof` or `instanceof` unsound type guard check. The `typeof x` type guard can be unsound in one of the following two cases:
  
      * `typeof x` never corresponds to the specified value (for example, `typeof x === 'number'` when `x` is of the type 'string \| boolean')
      * `typeof x` always corresponds to the specified value (for example, `typeof x === 'string'` when `x` is of the type 'string')
      
      The `x instanceof A` type guard can be unsound in one of the following two cases:
      
      * The type of `x` is not related to `A`
      * The type of `x` is `A` or a subtype of `A`
    """.trimIndent()

    assertThat(htmlToMarkdown(complexHtml)).isEqualTo(expected)
  }

  @Test
  fun `test simple markdown to html`() {
    @Language("Markdown")
    val markdown = """
      Text
    """.trimIndent()

    @Language("HTML")
    val expectedHtml = """
      <html>
      <head></head>
      <body>
      <p>Text</p>
      </body>
      </html>
    """.trimIndent()

    assertThat(markdownToHtml(markdown)).isEqualTo(expectedHtml)
  }

  @Test
  fun `test complex markdown to html`() {
    @Language("Markdown")
    val markdown = """
      Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.
      
      Types of function parameters can be specified in
      docstrings or in Python 3 function annotations.
      
      **Example:**
      
          def foo() -> int:
              return "abc" # Expected int, got str
      
      
          a: str
          a = foo() # Expected str, got int
      
      With the quick-fix, you can modify the problematic types:
      
          def foo() -> str:
              return "abc"
      
      
          a: str
          a = foo()
      
    """.trimIndent()

    @Language("HTML")
    val expectedHtml = """
      <html>
      <head></head>
      <body>
      <p>Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.</p>
      <p>Types of function parameters can be specified in docstrings or in Python 3 function annotations.</p>
      <p><strong>Example:</strong></p>
      <pre><code>def foo() -&gt; int:
          return "abc" # Expected int, got str
      
      
      a: str
      a = foo() # Expected str, got int
      </code></pre>
      <p>With the quick-fix, you can modify the problematic types:</p>
      <pre><code>def foo() -&gt; str:
          return "abc"
      
      
      a: str
      a = foo()
      </code></pre>
      </body>
      </html>
    """.trimIndent()

    assertThat(markdownToHtml(markdown)).isEqualTo(expectedHtml)
  }

  @Test
  fun `test simple html to plain text`() {
    @Language("HTML")
    val html = """
      <html>
      <body>
      <p>Text</p>
      </body>
      </html>
    """.trimIndent()

    @Language("TXT")
    val expectedPlainText = """
      Text
    """.trimIndent()

    assertThat(htmlToPlainText(html)).isEqualTo(expectedPlainText)
  }

  @Test
  fun `test complex html to plain text`() {
    @Language("HTML")
    val html = """
      <html>
      <body>
      <p>Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.</p>
      <p>Types of function parameters can be specified in
      docstrings or in Python 3 function annotations.</p>
      <p><strong>Example:</strong></p>
      <pre><code>def foo() -> int:
          return &quot;abc&quot; # Expected int, got str
      
      
      a: str
      a = foo() # Expected str, got int
      </code></pre>
      <p>With the quick-fix, you can modify the problematic types:</p>
      <pre><code>def foo() -> str:
          return &quot;abc&quot;
      
      
      a: str
      a = foo()
      </code></pre>
      </body>
      </html>
    """.trimIndent()

    @Language("TXT")
    val expectedPlainText = """
      Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases. Types of function parameters can be specified in docstrings or in Python 3 function annotations. Example: 'def foo() -> int:
          return "abc" # Expected int, got str
      
      
      a: str
      a = foo() # Expected str, got int' With the quick-fix, you can modify the problematic types: 'def foo() -> str:
          return "abc"
      
      
      a: str
      a = foo()'
    """.trimIndent()

    assertThat(htmlToPlainText(html)).isEqualTo(expectedPlainText)
  }

  @Test
  fun `test escapeContentInTag`() {
    val data = "<p>Hello World</p>"
    val tagName = "p"
    val expected = "<p>Hello World</p>"

    val result = escapeContentInTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeContentInTag without matching closing`() {
    val data = "<p>Hello <p>World</p>"
    val tagName = "p"
    val expected = "<p>Hello &lt;p&gt;World</p>"

    val result = escapeContentInTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeContentInTag with special characters`() {
    val data = "<code>Optional<Map<String, Integer>> map = Optional.empty();</code>"
    val tagName = "code"
    val expected = "<code>Optional&lt;Map&lt;String, Integer&gt;&gt; map = Optional.empty();</code>"

    val result = escapeContentInTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeContentInTag with multiple occurrences and special characters`() {
    val data = """
      <code>Optional<Map<String, Integer>> map = Optional.empty();</code>
      <code>Optional<List<String>> list = Optional.empty();</code>
    """.trimIndent()
    val tagName = "code"
    val expected = """
      <code>Optional&lt;Map&lt;String, Integer&gt;&gt; map = Optional.empty();</code>
      <code>Optional&lt;List&lt;String&gt;&gt; list = Optional.empty();</code>
    """.trimIndent()

    val result = escapeContentInTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeExceptTag`() {
    val data = "Hello <> <tag> World"
    val tagName = "tag"
    val expected = "Hello &lt;&gt; <tag> World"

    val result = escapeExceptTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeExceptTag closing`() {
    val data = "Hello <> <tag> World </tag>"
    val tagName = "tag"
    val expected = "Hello &lt;&gt; <tag> World </tag>"

    val result = escapeExceptTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeExceptTag with content inside`() {
    val data = "Hello <\"> <tag content=\"\"> World </tag>"
    val tagName = "tag"
    val expected = "Hello &lt;&quot;&gt; <tag content=\"\"> World </tag>"

    val result = escapeExceptTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeExceptTag with content inside and outside`() {
    val data = "Hello <\"> <tag content=\"\"> & </tag> & <tag/>"
    val tagName = "tag"
    val expected = "Hello &lt;&quot;&gt; <tag content=\"\"> &amp; </tag> &amp; <tag/>"

    val result = escapeExceptTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `test escapeContentInTag with complex data`() {
    val data = "<code><span style=\"\">&lt;T&#32;</span><span style=\"color:#cf8e6d;\">extends&#32;</span><span style=\"\">" +
               "Node&gt;(child:&#32;T)&#32;=&gt;&#32;T</span></code>"
    val tagName = "code"
    val expected = "<code><span style=\"\">&lt;T </span><span style=\"color:#cf8e6d;\">extends </span><span style=\"\">" +
                   "Node&gt;(child: T) =&gt; T</span></code>"

    val result = escapeContentInTag(data, tagName)

    assertThat(result).isEqualTo(expected)
  }
}