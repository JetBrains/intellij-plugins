package org.jetbrains.astro.lang

import com.intellij.lexer.Lexer
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.jetbrains.annotations.NonNls
import org.jetbrains.astro.getAstroTestDataPath
import org.jetbrains.astro.lang.lexer.AstroLexer
import org.junit.jupiter.api.Test

@TestApplication
open class AstroLexerTest {
  companion object {
    // needed for various XML extension points registration
    private val projectFixture = projectFixture()
  }

  private val lexer by lexerFixture(getAstroTestDataPath() + "/" + getDirPath()) { createLexer() }

  @Test
  fun testBasic1() = doTest("""
    |---
    |const a = 12 - 2
    |---
    |<div> { a } </div
  """)

  @Test
  fun testBasic2() = doTest("""
    |Some comment < 12
    |---
    |const a = new Text<Foo>("12")
    |---
    |Result is: { a }
  """)

  @Test
  fun testBasicComments() = doTest("""
    |---
    |import MyComponent from "./MyComponent.astro";
    |const Element = 'div'
    |const Component = MyComponent;
    |---
    |<Element>Hello!</Element> <!-- renders as <div>Hello!</div> -->
    |<Component /> <!-- renders as <MyComponent /> -->
  """)

  @Test
  fun testBasicExpressions() = doTest("""
    |---
    |const visible = true;
    |---
    |{visible && <p>Show me!</p>}
    |
    |{visible ? <p>Show me!</p> : <p>Else show me!</p>}
  """)

  @Test
  fun testBasicAttributeExpressions() = doTest("""
    |---
    |const name = "Astro";
    |---
    |<h1 class={name}>Attribute expressions are supported</h1>
    |
    |<MyComponent templateLiteralNameAttribute={`MyNameIstestBasicAttributeExpressions`} />
  """)

  @Test
  fun testBasicStyle() = doTest("""
    |---
    |// Your component script here!
    |---
    |<style>
    |  /* scoped to the component, other H1s on the page remain the same */
    |  h1 { color: red }
    |</style>
    |
    |<h1>Hello, world!</h1>
  """)

  @Test
  fun testScssStyle() = doTest("""
    |---
    |// Frontmatter
    |---
    |<style lang="scss">
    |  .card {
    |    @media screen {
    |    }
    |  }
    |</style>
    |
    |<div class="card">Content</div>
  """)

  @Test
  fun testLessStyle() = doTest("""
    |---
    |// Frontmatter
    |---
    |<style lang="less">
    |  @color-orange: #ff9900;
    |
    |  .concrete {
    |    color: @color-orange;
    |  }
    |</style>
  """)

  @Test
  fun testSassStyle() = doTest("""
    |---
    |// Frontmatter
    |---
    |<style lang="sass">
    |  ${"$"}family: 'serif';
    |
    |  body
    |    p
    |      font-family: ${"$"}family
    |</style>
  """)

  @Test
  fun testBasicScript() = doTest("""
    |<button data-confetti-button>Celebrate!</button>
    |
    |<script>
    |  // Import npm modules.
    |  import confetti from 'canvas-confetti';
    |
    |  // Find our component DOM on the page.
    |  const buttons = document.querySelectorAll('[data-confetti-button]');
    |
    |  // Add event listeners to fire confetti when a button is clicked.
    |  buttons.forEach((button) => {
    |    button.addEventListener('click', () => confetti());
    |  });
    |</script>
  """)

  @Test
  fun testLessThanTokenInsideScript() = doTest("""
    |<script>
    |  n<p
    |</script>
  """)

  @Test
  fun testHtmlInScript() = doTest("""
    |<script>
    |  const n = '<p></p>'
    |  const form = '<form><input><input></form>'
    |</script>
  """)

  @Test
  fun testScriptEmbedding() = doTest("""
    |<script type="foo/bar">
    |  <div></div>
    |</script>
  """)

  @Test
  fun testScriptInScript() = doTest("""
    |<script>
    |  <script><script>
    |</script>
  """)


  @Test
  fun testMultipleScriptBlocks() = doTest("""
    |<div>
    |  <script type="text/javascript"
    |          src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    |  <script src="/js/highcharts/highcharts.js" defer></script>
    |  <script src="/js/buoychart.js" data={JSON.stringify(chartData)} defer></script>
    |</div>
    """)

  @Test
  fun testEmptyFrontmatter1() = doTest("""
    |Some comment
    |------
    |const a = new Text<Foo>("12")
  """)

  @Test
  fun testEmptyFrontmatter2() = doTest("""
    |------
    |const a = new Text<Foo>("12")
  """)

  @Test
  fun testEmptyFrontmatter3() = doTest("""
    |------
  """)

  @Test
  fun testFrontmatterOnly1() = doTest("""
    |---
  """)

  @Test
  fun testFrontmatterOnly2() = doTest("""
    |---
    |const a = 12 -- 34 + "123---"
  """)

  @Test
  fun testFrontmatterOnly3() = doTest("""
    |---
    |const a = 12 -- 34 + "123---
  """)

  @Test
  fun testFrontmatterOnly4() = doTest("""
    |---
    |const a = /*12 --- */ 34 + /123---
  """)

  @Test
  fun testFrontmatterOnly5() = doTest("""
    |---
    |const a = /*12 --- 
  """)

  @Test
  fun testFrontmatterOnly6() = doTest("""
    |---
    |const a = /*12 --- */
  """)

  @Test
  fun testFrontmatterOnly7() = doTest("""
    |---
    |const a = /12 --- /
  """)

  @Test
  fun testNoFrontmatter1() = doTest("""
    |Foo Bar
    |Next line
  """)

  @Test
  fun testNoFrontmatter2() = doTest("""
    |Foo Bar '12---' foo --- bar
  """)

  @Test
  fun testNoFrontmatter3() = doTest("""
    |Foo Bar /*12 ---*/ foo --- bar
  """)

  @Test
  fun testNoFrontmatter4() = doTest("""
    |Foo Bar //12*/ foo bar
    |foo --- bar
  """)

  @Test
  fun testNoFrontmatter5() = doTest("""
    |Foo Bar /12/ foo --- bar
    |foo bar
  """)

  @Test
  fun testNoFrontmatter6() = doTest("""
    |Foo Bar /12 foo --- bar
    |foo --- bar
  """)

  @Test
  fun testNoFrontmatter7() = doTest("""
    |Foo Bar {12 --- 1} foo bar
    |foo bar
  """)

  @Test
  fun testNoFrontmatter8() = doTest("""
    |Foo Bar <a --- > Foo
  """)

  @Test
  fun testNoFrontmatter9() = doTest("""
    |<
  """)

  @Test
  fun testExpressionUnterminated() = doTest("""
    |------
    |{12<
  """)

  @Test
  fun testExpressionNotClosedIfBraceIsAttribute() = doTest("""
    |------
    |{12<a {/>} foo } bar> } fooBar
  """)

  @Test
  fun testExpressionNoNestedTemplateExpressions() = doTest("""
    |------
    |{12 + `this is ${'$'}{within `an }` expression}` }
  """)

  @Test
  fun testExpressionDoubleQuoteEscape() = doTest("""
    |------
    |{ "This } is \" escaped and ' } " } outside
  """)

  @Test
  fun testExpressionSingleQuoteEscape() = doTest("""
    |------
    |{ 'This } is \' escaped } and " ' }
  """)

  @Test
  fun testExpressionHtmlStyleComment() = doTest("""
    |------
    |{ <!-- this is a comment } ha --> }
  """)

  @Test
  fun testExpressionMultilineComment() = doTest("""
    |------
    |{ /* this is a comment } ha/ */ }
  """)

  @Test
  fun testExpressionSingleLineComment() = doTest("""
    |------
    |{ // this is a comment } ha/ }
    | and here is end}
  """)

  @Test
  fun testShorthandAttribute() = doTest("""
    |------
    |<a {foo}>
  """)

  @Test
  fun testShorthandAttributeBeforeAndAfterContent() = doTest("""
    |------
    |<a before{foo}after>
  """)

  @Test
  fun testNoShorthandAttributeIfValue() = doTest("""
    |------
    |<a {foo} = 12>
  """)

  @Test
  fun testShorthandAttributeUnterminated() = doTest("""
    |------
    |<a {foo} {"}"a fooBar>
  """)

  @Test
  fun testShorthandAttributeRegexBoundaryAndUnterminated() = doTest("""
    |------
    |<a {">" /} {/> fooBar>
  """)

  @Test
  fun testSpreadAttribute() = doTest("""
    |------
    |<a {... foo} { ...foo}  { .. .foo}>
  """)

  @Test
  fun testSpreadAttributeContentBeforeAndAfter() = doTest("""
    |------
    |<a before{ ...foo}after>
  """)

  @Test
  fun testNoSpreadAttributeIfValue() = doTest("""
    |------
    |<a {... "12}>`"} = 12>
  """)

  @Test
  fun testSpreadAttributeManyAttributes() = doTest("""
    |------
    |<a {....foo} {bar} fooBar>
  """)

  @Test
  fun testAttributeExpression() = doTest("""
    |------
    |<a foo={....foo}>
  """)

  @Test
  fun testAttributeExpressionNestedTemplateLiteralsSupported() = doTest("""
    |------
    |<a foo={`template${'$'}{expression + `template}` + expression } template}` attr_expression} attr}>}
  """)

  @Test
  fun testAttributeExpressionNoEscapeTemplateLiteral() = doTest("""
    |------
    |<a foo={`}>'"/\`}>
  """)

  @Test
  fun testAttributeExpressionNoEscapeMultilineComment() = doTest("""
    |------
    |<a foo={/*><}**\*/}>
  """)

  @Test
  fun testAttributeExpressionSingleQuoteEscape() = doTest("""
    |------
    |<a foo={'<">\'}'}>
  """)

  @Test
  fun testAttributeExpressionDoubleQuoteEscape() = doTest("""
    |------
    |<a foo={"<'>\"}"}>
  """)

  @Test
  fun testAttributeExpressionEscapeRegex() = doTest("""
    |------
    |<a foo={/regexp\/foo/}>
  """)

  @Test
  fun testAttributeExpressionRegexBoundary() = doTest("""
    |------
    |<a foo={/regexp} bar={/regexp"\}/}>
  """)

  @Test
  fun testAttributeExpressionSingleLineCommentBoundary() = doTest("""
    |------
    |<a foo={// comment }
    |end}>
  """)

  @Test
  fun testTemplateLiteralInFrontmatter() = doTest("""
    |---
    |const foo = `template`;
    |---
  """)

  @Test
  fun testTemplateInterpolationInFrontmatter() = doTest("""
    |---
    |const value = 'value';
    |const foo = `template ${'$'}{value}`;
    |---
  """)

  @Test
  fun testTemplateLiteralAttribute() = doTest("""
    |------
    |<a foo=`12`>
  """)

  @Test
  fun testTemplateLiteralAttributeNoEscape() = doTest("""
    |------
    |<a foo=`12\`>
  """)

  @Test
  fun testTemplateLiteralAttributeNoStacking() = doTest("""
    |------
    |<a foo=`12${'$'}{`foo>
  """)

  @Test
  fun testTemplateLiteralAttributeUnterminated() = doTest("""
    |------
    |<a foo=`12\>
  """)

  @Test
  fun testCharEntity() = doTest("""
    |------
    |{12 &lt; <span>&rarr;</span>}
  """)

  @Test
  fun testNestedExpressionEmptyTag() = doTest("""
    |foo<a><b>12</b>{23 + <c/> + 12} </a>foo
  """)

  @Test
  fun testNestedExpressionEmptyTagRandomBraces() = doTest("""
    |foo}<a><b>12</b>}{23 + <c/> + 12} </a>}
  """)

  @Test
  fun testComplexBroken() = doTest("""
    |------
    |<li class="link-card">
	  | <a title=`112 \` ${'$'}{12 + "12"}`
	  |   <h2>
	  | 		{12 + 34}
	  | 		<span>&rarr;</span>
	  | 	</h2>
	  | 	<p>
	  | 		{  <a foo={1223 + `121321${'$'}{``}`}> + 12 }
	  | 	</p>
	  | </a>
    |</li>
  """)

  @Test
  fun testAutoClosing() = doTest("""
    |{
    | <p>Foo
    | <p>Bar
    | </>
    | + 12
    |}
  """)

  @Test
  fun testAutoClosingNested() = doTest("""
    |{
    | <p>Foo
    | {
    |   <p>FooBar
    |   <p>Bar
    |   </>
    |   + 12
    | }
    | <p>Foo2
    | </>
    | +12
    |}
  """)

  @Test
  fun testEmptyTags() = doTest("""
    |{
    | <img> + 12
    |}
  """)

  @Test
  fun testWhitespaceBeforeFrontmatter() = doTest("""
    | ---
  """)

  @Test
  fun testContentWithWhitespacesBeforeFrontmatter() = doTest("""
    |  Some comment < 12
    |---
    |const a = new Text<Foo>("12")
    |---
  """)

  @Test
  fun testWhitespaceBeforeContent1() = doTest("""
    | < a
  """)

  @Test
  fun testWhitespaceBeforeContent2() = doTest("""
    | <a> foo </a>
  """)

  @Test
  fun testWhitespaceOnly() = doTest("""
    |
    |
  """)

  @Test
  fun testDoctype() = doTest("""
    |------
    |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN""http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    |<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  """)

  @Test
  fun testXmlPi() = doTest("""
    |<?xml processing ?>
    |<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  """)

  @Test
  fun testIsRaw() = doTest("""
    |<div>
    | {bar}
    | <div is:raw>
    |  {foo}<a title={foo} {...bar}\=12 fooBar=`12 3` {34}>
    |   {12}
    |  </a>
    | </div>
    | {fooBar}
    |</html>
  """)

  @Test
  fun testEmptyExpression() = doTest("""
    |<div title={}>{}</div>
  """.trimIndent())

  @Test
  fun testEmptyTag() = doTest("""
    |------
    |<>
  """.trimIndent())

  @Test
  fun testTitleComponent() {
    doTest("<head><title>This is <std>title</std></title></head><div><Title>This is <custom>title</custom></Title></div>")
  }

  @Test
  fun testLexerStateLoss() = doTest("""
    |{<div style="font-face: serif;"></div>}
  """.trimIndent())

  // WEB-62543 WEB-59705
  @Test
  fun testPDoesntCloseUl() = doTest("""
    |{
    |  <ul>
    |    <p>Foo</p>
    |  </ul>
    |}
  """.trimIndent())

  // WEB-62543 WEB-59705
  @Test
  fun testUlClosesP() = doTest("""
    |{
    |  <p>
    |    <ul>Foo</ul>
    |  </p>
    |}
  """.trimIndent())

  @Test
  fun testRawTextWithInterpolation() {
    doTest($$"""
      <title>{ title as number } and { 12 + "foo" }</title>
      <textarea>My { title ? `${title} foo` : `bar` } is cool</textarea>
      <div>My {title ? `${title} foo` : `bar`}</div>
    """.trimIndent())
  }

  @Test
  fun testJsxWithAndOperator() {
    doTest("""
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
    doTest("""
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
    doTest("""
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

  protected open fun createLexer(): Lexer = AstroLexer(projectFixture.get(), false, false)

  protected open fun getDirPath(): String = "lang/lexer"

  private fun doTest(@NonNls text: String) {
    lexer.doTest(text.trimMargin())
  }
}
