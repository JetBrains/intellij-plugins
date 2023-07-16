package org.jetbrains.astro.lang

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.jetbrains.annotations.NonNls
import org.jetbrains.astro.getAstroTestDataPath
import org.jetbrains.astro.lang.lexer.AstroLexerImpl
import kotlin.properties.Delegates

open class AstroLexerTest : LexerTestCase() {
  private var fixture: IdeaProjectTestFixture by Delegates.notNull()

  override fun setUp() {
    super.setUp()

    // needed for various XML extension points registration
    fixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).fixture
    fixture.setUp()
  }

  override fun tearDown() {
    try {
      fixture.tearDown()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testBasic1() = doTest("""
    |---
    |const a = 12 - 2
    |---
    |<div> { a } </div
  """)

  fun testBasic2() = doTest("""
    |Some comment < 12
    |---
    |const a = new Text<Foo>("12")
    |---
    |Result is: { a }
  """)

  fun testBasicComments() = doTest("""
    |---
    |import MyComponent from "./MyComponent.astro";
    |const Element = 'div'
    |const Component = MyComponent;
    |---
    |<Element>Hello!</Element> <!-- renders as <div>Hello!</div> -->
    |<Component /> <!-- renders as <MyComponent /> -->
  """)

  fun testBasicExpressions() = doTest("""
    |---
    |const visible = true;
    |---
    |{visible && <p>Show me!</p>}
    |
    |{visible ? <p>Show me!</p> : <p>Else show me!</p>}
  """)

  fun testBasicAttributeExpressions() = doTest("""
    |---
    |const name = "Astro";
    |---
    |<h1 class={name}>Attribute expressions are supported</h1>
    |
    |<MyComponent templateLiteralNameAttribute={`MyNameIs${name}`} />
  """)

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

  fun testEmptyFrontmatter1() = doTest("""
    |Some comment
    |------
    |const a = new Text<Foo>("12")
  """)

  fun testEmptyFrontmatter2() = doTest("""
    |------
    |const a = new Text<Foo>("12")
  """)

  fun testEmptyFrontmatter3() = doTest("""
    |------
  """)

  open fun testFrontmatterOnly1() = doTest("""
    |---
  """)

  fun testFrontmatterOnly2() = doTest("""
    |---
    |const a = 12 -- 34 + "123---"
  """)

  fun testFrontmatterOnly3() = doTest("""
    |---
    |const a = 12 -- 34 + "123---
  """)

  fun testFrontmatterOnly4() = doTest("""
    |---
    |const a = /*12 --- */ 34 + /123---
  """)

  fun testFrontmatterOnly5() = doTest("""
    |---
    |const a = /*12 --- 
  """)

  fun testFrontmatterOnly6() = doTest("""
    |---
    |const a = /*12 --- */
  """)

  fun testFrontmatterOnly7() = doTest("""
    |---
    |const a = /12 --- /
  """)

  fun testNoFrontmatter1() = doTest("""
    |Foo Bar
    |Next line
  """)

  fun testNoFrontmatter2() = doTest("""
    |Foo Bar '12---' foo --- bar
  """)

  fun testNoFrontmatter3() = doTest("""
    |Foo Bar /*12 ---*/ foo --- bar
  """)

  fun testNoFrontmatter4() = doTest("""
    |Foo Bar //12*/ foo bar
    |foo --- bar
  """)

  fun testNoFrontmatter5() = doTest("""
    |Foo Bar /12/ foo --- bar
    |foo bar
  """)

  fun testNoFrontmatter6() = doTest("""
    |Foo Bar /12 foo --- bar
    |foo --- bar
  """)

  fun testNoFrontmatter7() = doTest("""
    |Foo Bar {12 --- 1} foo bar
    |foo bar
  """)

  fun testNoFrontmatter8() = doTest("""
    |Foo Bar <a --- > Foo
  """)

  fun testNoFrontmatter9() = doTest("""
    |<
  """)

  fun testExpressionUnterminated() = doTest("""
    |------
    |{12<
  """)

  fun testExpressionNotClosedIfBraceIsAttribute() = doTest("""
    |------
    |{12<a {/>} foo } bar> } fooBar
  """)

  fun testExpressionNoNestedTemplateExpressions() = doTest("""
    |------
    |{12 + `this is ${'$'}{within `an }` expression}` }
  """)

  fun testExpressionDoubleQuoteEscape() = doTest("""
    |------
    |{ "This } is \" escaped and ' } " } outside
  """)

  fun testExpressionSingleQuoteEscape() = doTest("""
    |------
    |{ 'This } is \' escaped } and " ' }
  """)

  fun testExpressionHtmlStyleComment() = doTest("""
    |------
    |{ <!-- this is a comment } ha --> }
  """)

  fun testExpressionMultilineComment() = doTest("""
    |------
    |{ /* this is a comment } ha/ */ }
  """)

  fun testExpressionSingleLineComment() = doTest("""
    |------
    |{ // this is a comment } ha/ }
    | and here is end}
  """)

  fun testShorthandAttribute() = doTest("""
    |------
    |<a {foo}>
  """)

  fun testShorthandAttributeBeforeAndAfterContent() = doTest("""
    |------
    |<a before{foo}after>
  """)

  fun testNoShorthandAttributeIfValue() = doTest("""
    |------
    |<a {foo} = 12>
  """)

  fun testShorthandAttributeUnterminated() = doTest("""
    |------
    |<a {foo} {"}"a fooBar>
  """)

  fun testShorthandAttributeRegexBoundaryAndUnterminated() = doTest("""
    |------
    |<a {">" /} {/> fooBar>
  """)

  fun testSpreadAttribute() = doTest("""
    |------
    |<a {... foo} { ...foo}  { .. .foo}>
  """)

  fun testSpreadAttributeContentBeforeAndAfter() = doTest("""
    |------
    |<a before{ ...foo}after>
  """)

  fun testNoSpreadAttributeIfValue() = doTest("""
    |------
    |<a {... "12}>`"} = 12>
  """)

  fun testSpreadAttributeManyAttributes() = doTest("""
    |------
    |<a {....foo} {bar} fooBar>
  """)

  fun testAttributeExpression() = doTest("""
    |------
    |<a foo={....foo}>
  """)

  fun testAttributeExpressionNestedTemplateLiteralsSupported() = doTest("""
    |------
    |<a foo={`template${'$'}{expression + `template}` + expression } template}` attr_expression} attr}>}
  """)

  fun testAttributeExpressionNoEscapeTemplateLiteral() = doTest("""
    |------
    |<a foo={`}>'"/\`}>
  """)

  fun testAttributeExpressionNoEscapeMultilineComment() = doTest("""
    |------
    |<a foo={/*><}**\*/}>
  """)

  fun testAttributeExpressionSingleQuoteEscape() = doTest("""
    |------
    |<a foo={'<">\'}'}>
  """)

  fun testAttributeExpressionDoubleQuoteEscape() = doTest("""
    |------
    |<a foo={"<'>\"}"}>
  """)

  fun testAttributeExpressionEscapeRegex() = doTest("""
    |------
    |<a foo={/regexp\/foo/}>
  """)

  fun testAttributeExpressionRegexBoundary() = doTest("""
    |------
    |<a foo={/regexp} bar={/regexp"\}/}>
  """)

  fun testAttributeExpressionSingleLineCommentBoundary() = doTest("""
    |------
    |<a foo={// comment }
    |end}>
  """)

  fun testTemplateLiteralInFrontmatter() = doTest("""
    |---
    |const foo = `template`;
    |---
  """)

  fun testTemplateInterpolationInFrontmatter() = doTest("""
    |---
    |const value = 'value';
    |const foo = `template ${'$'}{value}`;
    |---
  """)

  fun testTemplateLiteralAttribute() = doTest("""
    |------
    |<a foo=`12`>
  """)

  fun testTemplateLiteralAttributeNoEscape() = doTest("""
    |------
    |<a foo=`12\`>
  """)

  fun testTemplateLiteralAttributeNoStacking() = doTest("""
    |------
    |<a foo=`12${'$'}{`foo>
  """)

  fun testTemplateLiteralAttributeUnterminated() = doTest("""
    |------
    |<a foo=`12\>
  """)

  fun testCharEntity() = doTest("""
    |------
    |{12 &lt; <span>&rarr;</span>}
  """)

  fun testNestedExpressionEmptyTag() = doTest("""
    |foo<a><b>12</b>{23 + <c/> + 12} </a>foo
  """)

  fun testNestedExpressionEmptyTagRandomBraces() = doTest("""
    |foo}<a><b>12</b>}{23 + <c/> + 12} </a>}
  """)

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

  fun testAutoClosing() = doTest("""
    |{
    | <p>Foo
    | <p>Bar
    | </>
    | + 12
    |}
  """)

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

  fun testEmptyTags() = doTest("""
    |{
    | <img> + 12
    |}
  """)

  fun testWhitespaceBeforeFrontmatter() = doTest("""
    | ---
  """)

  fun testContentWithWhitespacesBeforeFrontmatter() = doTest("""
    |  Some comment < 12
    |---
    |const a = new Text<Foo>("12")
    |---
  """)

  fun testWhitespaceBeforeContent1() = doTest("""
    | < a
  """)

  fun testWhitespaceBeforeContent2() = doTest("""
    | <a> foo </a>
  """)

  fun testWhitespaceOnly() = doTest("""
    |
    |
  """)

  fun testDoctype() = doTest("""
    |------
    |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN""http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    |<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  """)

  fun testXmlPi() = doTest("""
    |<?xml processing ?>
    |<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  """)

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

  fun testEmptyExpression() = doTest("""
    |<div title={}>{}</div>
  """.trimIndent())

  fun testEmptyTag() = doTest("""
    |------
    |<>
  """.trimIndent())

  fun testTitleComponent(){
    doTest("<head><title>This is <std>title</std></title></head><div><Title>This is <custom>title</custom></Title></div>")
  }

  fun testLexerStateLoss() = doTest("""
    |{<div style="font-face: serif;"></div>}
  """.trimIndent())

  override fun createLexer(): Lexer = AstroLexerImpl(fixture.project)

  override fun getDirPath() = "lang/lexer"

  override fun getPathToTestDataFile(extension: String?): String = getAstroTestDataPath() + "/$dirPath/" + getTestName(true) + extension

  override fun doTest(@NonNls text: String) {
    val withoutMargin = text.trimMargin()
    super.doTest(withoutMargin)
    checkCorrectRestart(withoutMargin)
  }
}