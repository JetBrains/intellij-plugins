package org.jetbrains.astro.lang

import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.javascript.testFramework.web.JSHtmlParsingTest
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.javascript.dialects.TypeScriptParserDefinition
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.getAstroTestDataPath
import org.jetbrains.astro.lang.frontmatter.AstroFrontmatterLanguage
import org.jetbrains.astro.lang.parser.AstroEmbeddedContentSupport
import org.jetbrains.astro.lang.parser.AstroParserDefinition
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class AstroParserTest : JSHtmlParsingTest("astro", AstroParserDefinition()) {

  @Test
  override fun testUnclosedTag() {
    Assumptions.abort<Unit>("disable")
  }

  @Test
  override fun testContent1() {
    Assumptions.abort<Unit>("disable")
  }

  @Test
  override fun testSpecialTagsParsing() {
    Assumptions.abort<Unit>("disable")
  }

  @Test
  override fun testScriptEmbeddingParsing() {
    Assumptions.abort<Unit>("disable")
  }

  @Test
  fun testBasic1() {
    doTestAstro("""
      ---
      const a = 12 - 2
      ---
      <div> { a } </div
    """)
  }

  @Test
  fun testBasic2() {
    doTestAstro("""
      Some comment < 12
      ---
      const a = new Text<Foo>("12")
      ---
      Result is: { a }
    """)
  }

  @Test
  fun testBasicComments() {
    doTestAstro("""
      ---
      import MyComponent from "./MyComponent.astro";
      const Element = 'div'
      const Component = MyComponent;
      ---
      <Element>Hello!</Element> <!-- renders as <div>Hello!</div> -->
      <Component /> <!-- renders as <MyComponent /> -->
    """)
  }

  @Test
  fun testBasicExpressions() {
    doTestAstro("""
      ---
      const visible = true;
      ---
      {visible && <p>Show me!</p>}
      
      {visible ? <p>Show me!</p> : <p>Else show me!</p>}
    """)
  }

  @Test
  fun testBasicAttributeExpressions() {
    doTestAstro("""
      ---
      const name = "Astro";
      ---
      <h1 class={name}>Attribute expressions are supported</h1>
      
      <MyComponent templateLiteralNameAttribute={`MyNameIstestBasicAttributeExpressions`} />
    """)
  }

  @Test
  fun testBasicExpression() {
    doTestAstro("""
      { name }
    """)
  }

  @Test
  fun testBasicExpressionWithTags() {
    doTestAstro("""
      { 12 + <a>foo</a> + 32 }
    """)
  }

  @Test
  fun testNestedExpressionsWithTags() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + 12}bar</a> + 32 }
    """)
  }

  @Test
  fun testNestedExpressionEmptyTag() {
    doTestAstro("""
      foo<a><b>12</b>{23 + <c/> + 12} </a>foo
    """)
  }

  @Test
  fun testNestedExpressionEmptyTagRandomBraces() {
    doTestAstro("""
      foo}<a><b>12</b>}{23 + <c/> + 12} </a>}
    """)
  }

  @Test
  fun testBroken1() {
    doTestAstro("""
      <a><b>12</b>{23 +<c/></a>
    """)
  }

  @Test
  fun testBroken2() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + 12 bar</a> + 32 }
    """)
  }

  @Test
  fun testBroken3() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + <foo> { bar</a> + 32 }
    """)
  }

  @Test
  fun testAttributeExpression() {
    doTestAstro("""
      { <a href={url}>{url}</a> }
    """)
  }

  @Test
  fun testAttributeExpressionNoTags() {
    doTestAstro("""
      { <a href={url<foo>bar}></a> }
    """)
  }

  @Test
  fun testAttributeExpressionNestedTemplateLiteral() {
    doTestAstro("""
      { <a href={`https://${"$"}{site + `foo`}bar`}>{url}</a> }
    """)
  }

  @Test
  fun testSpreadAttribute() {
    doTestAstro("""
      { <a {...url}>{url}</a> }
    """)
  }

  @Test
  fun testShorthandAttribute() {
    doTestAstro("""
      { <a {url}>{url}</a> }
    """)
  }

  @Test
  fun testTemplateLiteralAttribute() {
    doTestAstro("""
      { <a url=`https://${"$"}{site}`>{site}</a> }
    """)
  }

  @Test
  fun testTemplateLiteralAttributeNoNested() {
    doTestAstro("""
      { <a url=`https://${"$"}{site + `>{site}</a> }
    """)
  }

  @Test
  fun testTemplateLiteralAttributeNoTags() {
    doTestAstro("""
      { <a url=`https://${"$"}{site<foo>bar}`>{site}</a> }
    """)
  }

  @Test
  fun testTemplateLiteralInFrontmatter() {
    doTestAstro("""
      ---
      const foo = `template`;
      ---
    """)
  }

  @Test
  fun testTemplateInterpolationInFrontmatter() {
    doTestAstro("""
      ---
      const value = 'value';
      const foo = `template ${'$'}{value}`;
      ---
    """)
  }

  @Test
  fun testExpressionUnterminated() {
    doTestAstro("""
      {12<
    """)
  }

  @Test
  fun testExpressionNotClosedIfBraceIsAttribute() {
    doTestAstro("""
      {12 + <a {/>} foo } bar> } fooBar
    """)
  }

  @Test
  fun testExpressionNoNestedTemplateExpressions() {
    doTestAstro("""
      {12 + `this is ${'$'}{within `an }` expression}` }
    """)
  }

  @Test
  fun testExpressionDoubleQuoteEscape() {
    doTestAstro("""
      { "This } is \" escaped and ' } " } outside
    """)
  }

  @Test
  fun testExpressionSingleQuoteEscape() {
    doTestAstro("""
      { 'This } is \' escaped } and " ' }
    """)
  }

  @Test
  fun testExpressionHtmlStyleComment() {
    doTestAstro("""
      { <!-- this is a comment } ha --> }
    """)
  }

  @Test
  fun testExpressionMultilineComment() {
    doTestAstro("""
      { /* this is a comment } ha/ */ }
    """)
  }

  @Test
  fun testExpressionSingleLineComment() {
    doTestAstro("""
      { // this is a comment } ha/ }
       and here is end}
    """)
  }

  @Test
  fun testShorthandAttributeBeforeAndAfterContent() {
    doTestAstro("""
      <a before{foo}after>
    """)
  }

  @Test
  fun testNoShorthandAttributeIfValue() {
    doTestAstro("""
      <a {foo} = 12>
    """)
  }

  @Test
  fun testShorthandAttributeUnterminated() {
    doTestAstro("""
      <a {foo} {"}"a fooBar>
    """)
  }

  @Test
  fun testShorthandAttributeRegexBoundaryAndUnterminated() {
    doTestAstro("""
      <a {">" /} {/> fooBar>
    """)
  }

  @Test
  fun testSpreadAttributeContentBeforeAndAfter() {
    doTestAstro("""
      <a before{ ...foo}after>
    """)
  }

  @Test
  fun testNoSpreadAttributeIfValue() {
    doTestAstro("""
      <a {... "12}>`"} = 12>
    """)
  }

  @Test
  fun testSpreadAttributeManyAttributes() {
    doTestAstro("""
      <a {....foo} {bar} fooBar>
    """)
  }

  @Test
  fun testAttributeExpressionNestedTemplateLiteralsSupported() {
    doTestAstro("""
      <a foo={`template${'$'}{expression + `template}` + expression } template}` + attr_expression} attr}>}
    """)
  }

  @Test
  fun testAttributeExpressionNoEscapeTemplateLiteral() {
    doTestAstro("""
      <a foo={`}>'"/\`}>
    """)
  }

  @Test
  fun testAttributeExpressionNoEscapeMultilineComment() {
    doTestAstro("""
      <a foo={/*><}**\*/}>
    """)
  }

  @Test
  fun testAttributeExpressionSingleQuoteEscape() {
    doTestAstro("""
      <a foo={'<">\'}'}>
    """)
  }

  @Test
  fun testAttributeExpressionDoubleQuoteEscape() {
    doTestAstro("""
      <a foo={"<'>\"}"}>
    """)
  }

  @Test
  fun testAttributeExpressionEscapeRegex() {
    doTestAstro("""
      <a foo={/regexp\/foo/}>
    """)
  }

  @Test
  fun testAttributeExpressionRegexBoundary() {
    doTestAstro("""
      <a foo={/regexp} bar={/regexp"\}/}>
    """)
  }

  @Test
  fun testAttributeExpressionSingleLineCommentBoundary() {
    doTestAstro("""
      <a foo={// comment }
      end}>
    """)
  }

  @Test
  fun testTemplateLiteralAttributeNoEscape() {
    doTestAstro("""
      <a foo=`12\`>
    """)
  }

  @Test
  fun testTemplateLiteralAttributeUnterminated() {
    doTestAstro("""
      <a foo=`12\>
    """)
  }

  @Test
  fun testCharEntity() {
    doTestAstro("""
      {12 &lt; <span>&rarr;</span>}
    """)
  }

  @Test
  fun testComplexBroken() {
    doTestAstro("""
      <li class="link-card">
       <a title=`112 \` ${'$'}{12 + "12"}`
         <h2>
           {12 + 34}
           <span>&rarr;</span>
         </h2>
         <p>
           {  <a foo={1223 + `121321${'$'}{``}`}> + 12 }
         </p>
       </a>
      </li>
    """)
  }

  @Test
  fun testBlockStatementParsing() {
    doTestAstro("""
      ---
      {
        const a = 12
      }
      ---
      <div>{ (() => { return <a></a> })() }</div>
    """)
  }

  @Test
  fun testAutoClosingOverExpression() {
    doTestAstro("""  
      <main>
        <p>Foo
        {<p>Bar
    """)
  }

  @Test
  fun testAutoClosing() {
    doTestAstro("""
      {
       <p>Foo
       <p>Bar
       </>
       + 12
      }
    """)
  }

  @Test
  fun testAutoClosingNested() {
    doTestAstro("""
      {
       <p>Foo
       {
         <p>FooBar
         <p>Bar
         </>
         + 12
       }
       <p>Foo2
       </>
       +12
      }
    """)
  }

  @Test
  fun testEmptyTags() {
    doTestAstro("""
      {
       <img> + 12
      }
    """)
  }

  @Test
  fun testMalformedExpression() {
    doTestAstro("""
      <foo>
        { 12 <p></> }
      </foo>
    """)
  }

  @Test
  fun testContentBeforeFrontmatterWithWhitespace() {
    doTestAstro("""
     
          foo - <
      bar
      
           ---export interface Props {title: string;
      body: string; href: string;
      }
      const {
        title, href,
        body} =
        Astro.props; ---
    """)
  }

  @Test
  fun testIsRaw() {
    doTestAstro("""
      <div>
       {bar}
       <div is:raw>
        {foo}<a title={foo} {...bar}\=12 fooBar=`12 3` {34}>
         {12}
        </a>
       </div>
       {
          12 + <div is:raw>{12}</div> + 12
       }
      </div>
    """)
  }

  @Test
  fun testAutoCloseTableWithinExpression() {
    doTestAstro("""
      <table>{ hasContent && <tr><td>12</td><td>14</td></tr> }</table>
    """)
  }

  @Test
  fun testTypeScriptInTemplate() {
    doTestAstro("""
      <script>
          const value: string = 'foo';
      </script>
    """)
  }

  @Test
  fun testInlineScriptInTemplate() {
    doTestAstro("""
      <script is:inline>
          const value: string = 'foo';
      </script>
    """)
  }

  @Test
  fun testTitleHtml() {
    doTestAstro("""
      <head><title>This is <std>title</std></title>
      <div><Title>This is <custom>title</custom></Title></div>
    """)
  }

  @Test
  fun testSingleTagOverride() {
    doTestAstro("""
      <Link>This should not throw an error</Link>      
    """)
  }

  @Test
  fun testContentAfterScriptBlock() {
    doTestAstro("""
      <div></div>
      <script>const a = 12</script>
      <style>
        div { margin: 0; }
      </style>
      <style>
        p { margin: 0; }
      </style>
    """)
  }

  @Test
  fun testMultipleScriptBlocks() {
    doTestAstro("""
      <div>
        <script type="text/javascript"
                src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
        <script src="/js/highcharts/highcharts.js" defer></script>
        <script src="/js/buoychart.js" data={JSON.stringify(chartData)} defer></script>
      </div>
    """.trimIndent())
  }

  @Test
  fun testRawTextWithInterpolation() {
    doTestAstro($$"""
      <title>{ title as number } and { 12 + "foo" }</title>
      <textarea>My { title ? `${title} foo` : `bar` } is cool</textarea>
      <div>My {title ? `${title} foo` : `bar`}</div>
    """.trimIndent())
  }

  @Test
  fun testJsxWithAndOperator() {
    doTestAstro("""
       <table>
         {data.map((wd:any) =>
           <tr>
             <td>{wd.wdir !== null && <i></i>}</td>
             <td></td>
           </tr>
         )}
       </table>
    """.trimIndent())
  }

  @Test
  fun testJsxWithAndOperatorBroken1() {
    doTestAstro("""
       <table>
         {data.map((wd:any) =>
           <tr>
             <td>{wd.wdir !== null && </i>}</td>
             <td></td>
           </tr>
         )}
       </table>
    """.trimIndent())
  }

  @Test
  fun testJsxWithAndOperatorBroken2() {
    doTestAstro("""
       <table>
         {data.map((wd:any) =>
           <tr>
             <td>{wd.wdir !== null && </i></td>
             <td></td>
           </tr>
         )}
       </table>
    """.trimIndent())
  }

  @Test
  fun testRawTextUnclosedBrace() {
    doTestAstro("<title>{</title>")
  }

  @Test
  fun testRawTextUnclosedTemplateLiteral() {
    doTestAstro("<title>{`</title>")
  }

  @Test
  fun testRawTextEmptyBraces() {
    doTestAstro("<title>{}</title>")
  }

  @Test
  fun testRawTextUnclosedNestedTemplateExpression() {
    doTestAstro($$"<title>{`foo ${</title>")
  }

  @Test
  fun testTextareaUnclosedBrace() {
    doTestAstro("<textarea>{</textarea>")
  }

  @Test
  fun testTextareaUnclosedTemplateLiteral() {
    doTestAstro("<textarea>{`</textarea>")
  }

  @Test
  fun testTextareaUnclosedNestedTemplateExpression() {
    doTestAstro($$"<textarea>{`foo ${</textarea>")
  }

  @Test
  fun testTextareaEmptyBraces() {
    doTestAstro("<textarea>{}</textarea>")
  }

  @BeforeEach
  fun setUp() {
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, AstroFrontmatterLanguage.INSTANCE, TypeScriptParserDefinition())
  }

  override fun getEmbeddedContentSupports(): List<HtmlEmbeddedContentSupport> {
    return super.getEmbeddedContentSupports() + AstroEmbeddedContentSupport()
  }

  override fun getTestDataPath(): String = getAstroTestDataPath() + "/lang/parser"

  private fun doTestAstro(text: String) {
    doTest(text.trimIndent(), "test.astro")
  }

  override fun doTest(text: String, fileName: String) {
    super.doTest(
      if (fileName.endsWith(".astro"))
        text
      else
        "------\n$text",
      "${fileName.substring(0, fileName.lastIndexOf('.'))}.astro")
  }

}