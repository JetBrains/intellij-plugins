package org.jetbrains.astro.lang

import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.javascript.web.JSHtmlParsingTest
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.javascript.dialects.TypeScriptParserDefinition
import org.jetbrains.astro.getAstroTestDataPath
import org.jetbrains.astro.lang.frontmatter.AstroFrontmatterLanguage
import org.jetbrains.astro.lang.parser.AstroEmbeddedContentSupport
import org.jetbrains.astro.lang.parser.AstroParserDefinition
import org.junit.AssumptionViolatedException

class AstroParserTest : JSHtmlParsingTest("astro", AstroParserDefinition()) {

  override fun testUnclosedTag() {
    throw AssumptionViolatedException("disable")
  }

  override fun testContent1() {
    throw AssumptionViolatedException("disable")
  }

  fun testBasic1() {
    doTestAstro("""
      ---
      const a = 12 - 2
      ---
      <div> { a } </div
    """)
  }

  fun testBasic2() {
    doTestAstro("""
      Some comment < 12
      ---
      const a = new Text<Foo>("12")
      ---
      Result is: { a }
    """)
  }

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

  fun testBasicExpressions() {
    doTestAstro("""
      ---
      const visible = true;
      ---
      {visible && <p>Show me!</p>}
      
      {visible ? <p>Show me!</p> : <p>Else show me!</p>}
    """)
  }

  fun testBasicAttributeExpressions() {
    doTestAstro("""
      ---
      const name = "Astro";
      ---
      <h1 class={name}>Attribute expressions are supported</h1>
      
      <MyComponent templateLiteralNameAttribute={`MyNameIs${name}`} />
    """)
  }

  fun testBasicExpression() {
    doTestAstro("""
      { name }
    """)
  }

  fun testBasicExpressionWithTags() {
    doTestAstro("""
      { 12 + <a>foo</a> + 32 }
    """)
  }

  fun testNestedExpressionsWithTags() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + 12}bar</a> + 32 }
    """)
  }

  fun testNestedExpressionEmptyTag() {
    doTestAstro("""
      foo<a><b>12</b>{23 + <c/> + 12} </a>foo
    """)
  }

  fun testNestedExpressionEmptyTagRandomBraces() {
    doTestAstro("""
      foo}<a><b>12</b>}{23 + <c/> + 12} </a>}
    """)
  }

  fun testBroken1() {
    doTestAstro("""
      <a><b>12</b>{23 +<c/></a>
    """)
  }

  fun testBroken2() {
    doTestAstro("""
      { 12 + <a>foo{12 + <b>bar</> + 12 bar</a> + 32 }
    """)
  }

  fun testAttributeExpression() {
    doTestAstro("""
      { <a href={url}>{url}</a> }
    """)
  }

  fun testAttributeExpressionNoTags() {
    doTestAstro("""
      { <a href={url<foo>bar}></a> }
    """)
  }

  fun testAttributeExpressionNestedTemplateLiteral() {
    doTestAstro("""
      { <a href={`https://${"$"}{site + `foo`}bar`}>{url}</a> }
    """)
  }

  fun testSpreadAttribute() {
    doTestAstro("""
      { <a {...url}>{url}</a> }
    """)
  }

  fun testShorthandAttribute() {
    doTestAstro("""
      { <a {url}>{url}</a> }
    """)
  }

  fun testTemplateLiteralAttribute() {
    doTestAstro("""
      { <a url=`https://${"$"}{site}`>{site}</a> }
    """)
  }

  fun testTemplateLiteralAttributeNoNested() {
    doTestAstro("""
      { <a url=`https://${"$"}{site + `>{site}</a> }
    """)
  }

  fun testTemplateLiteralAttributeNoTags() {
    doTestAstro("""
      { <a url=`https://${"$"}{site<foo>bar}`>{site}</a> }
    """)
  }

  fun testTemplateLiteralInFrontmatter() {
    doTestAstro("""
      ---
      const foo = `template`;
      ---
    """)
  }

  fun testTemplateInterpolationInFrontmatter() {
    doTestAstro("""
      ---
      const value = 'value';
      const foo = `template ${'$'}{value}`;
      ---
    """)
  }

  fun testExpressionUnterminated() {
    doTestAstro("""
      {12<
    """)
  }

  fun testExpressionNotClosedIfBraceIsAttribute() {
    doTestAstro("""
      {12 + <a {/>} foo } bar> } fooBar
    """)
  }

  fun testExpressionNoNestedTemplateExpressions() {
    doTestAstro("""
      {12 + `this is ${'$'}{within `an }` expression}` }
    """)
  }

  fun testExpressionDoubleQuoteEscape() {
    doTestAstro("""
      { "This } is \" escaped and ' } " } outside
    """)
  }

  fun testExpressionSingleQuoteEscape() {
    doTestAstro("""
      { 'This } is \' escaped } and " ' }
    """)
  }

  fun testExpressionHtmlStyleComment() {
    doTestAstro("""
      { <!-- this is a comment } ha --> }
    """)
  }

  fun testExpressionMultilineComment() {
    doTestAstro("""
      { /* this is a comment } ha/ */ }
    """)
  }

  fun testExpressionSingleLineComment() {
    doTestAstro("""
      { // this is a comment } ha/ }
       and here is end}
    """)
  }

  fun testShorthandAttributeBeforeAndAfterContent() {
    doTestAstro("""
      <a before{foo}after>
    """)
  }

  fun testNoShorthandAttributeIfValue() {
    doTestAstro("""
      <a {foo} = 12>
    """)
  }

  fun testShorthandAttributeUnterminated() {
    doTestAstro("""
      <a {foo} {"}"a fooBar>
    """)
  }

  fun testShorthandAttributeRegexBoundaryAndUnterminated() {
    doTestAstro("""
      <a {">" /} {/> fooBar>
    """)
  }

  fun testSpreadAttributeContentBeforeAndAfter() {
    doTestAstro("""
      <a before{ ...foo}after>
    """)
  }

  fun testNoSpreadAttributeIfValue() {
    doTestAstro("""
      <a {... "12}>`"} = 12>
    """)
  }

  fun testSpreadAttributeManyAttributes() {
    doTestAstro("""
      <a {....foo} {bar} fooBar>
    """)
  }

  fun testAttributeExpressionNestedTemplateLiteralsSupported() {
    doTestAstro("""
      <a foo={`template${'$'}{expression + `template}` + expression } template}` + attr_expression} attr}>}
    """)
  }

  fun testAttributeExpressionNoEscapeTemplateLiteral() {
    doTestAstro("""
      <a foo={`}>'"/\`}>
    """)
  }

  fun testAttributeExpressionNoEscapeMultilineComment() {
    doTestAstro("""
      <a foo={/*><}**\*/}>
    """)
  }

  fun testAttributeExpressionSingleQuoteEscape() {
    doTestAstro("""
      <a foo={'<">\'}'}>
    """)
  }

  fun testAttributeExpressionDoubleQuoteEscape() {
    doTestAstro("""
      <a foo={"<'>\"}"}>
    """)
  }

  fun testAttributeExpressionEscapeRegex() {
    doTestAstro("""
      <a foo={/regexp\/foo/}>
    """)
  }

  fun testAttributeExpressionRegexBoundary() {
    doTestAstro("""
      <a foo={/regexp} bar={/regexp"\}/}>
    """)
  }

  fun testAttributeExpressionSingleLineCommentBoundary() {
    doTestAstro("""
      <a foo={// comment }
      end}>
    """)
  }

  fun testTemplateLiteralAttributeNoEscape() {
    doTestAstro("""
      <a foo=`12\`>
    """)
  }

  fun testTemplateLiteralAttributeUnterminated() {
    doTestAstro("""
      <a foo=`12\>
    """)
  }

  fun testCharEntity() {
    doTestAstro("""
      {12 &lt; <span>&rarr;</span>}
    """)
  }

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

  fun testAutoClosingOverExpression() {
    doTestAstro("""  
      <main>
        <p>Foo
        {<p>Bar
    """)
  }

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

  fun testEmptyTags() {
    doTestAstro("""
      {
       <img> + 12
      }
    """)
  }

  fun testMalformedExpression() {
    doTestAstro("""
      <foo>
        { 12 <p></> }
      </foo>
    """)
  }

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

  fun testAutoCloseTableWithinExpression() {
    doTestAstro("""
      <table>{ hasContent && <tr><td>12</td><td>14</td></tr> }</table>
    """)
  }

  fun testTypeScriptInTemplate() {
    doTestAstro("""
      <script>
          const value: string = 'foo';
      </script>
    """)
  }

  fun testInlineScriptInTemplate() {
    doTestAstro("""
      <script is:inline>
          const value: string = 'foo';
      </script>
    """)
  }

  fun testTitleHtml() {
    doTestAstro("""
      <head><title>This is <std>title</std></title>
      <div><Title>This is <custom>title</custom></Title></div>
    """)
  }

  fun testSingleTagOverride() {
    doTestAstro("""
      <Link>This should not throw an error</Link>      
    """)
  }

  override fun setUp() {
    super.setUp()
    addExplicitExtension(LanguageParserDefinitions.INSTANCE, AstroFrontmatterLanguage.INSTANCE, TypeScriptParserDefinition())
    HtmlEmbeddedContentSupport.register(application, testRootDisposable, AstroEmbeddedContentSupport::class.java)
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