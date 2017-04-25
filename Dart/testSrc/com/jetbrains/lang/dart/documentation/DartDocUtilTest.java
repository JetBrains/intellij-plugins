package com.jetbrains.lang.dart.documentation;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil;
import com.jetbrains.lang.dart.psi.DartComponent;

import static com.jetbrains.lang.dart.util.DartPresentableUtil.RIGHT_ARROW;

public class DartDocUtilTest extends DartCodeInsightFixtureTestCase {

  private void doTest(String expectedDoc, String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.addFileToProject("test.dart", realContents);
    final DartComponent dartComponent = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    assertNotNull("target element not found at offset " + caretOffset, dartComponent);
    assertEquals(expectedDoc, DartDocUtil.generateDoc(dartComponent));
  }

  public void testAbstractClassSig() throws Exception {
    doTest("<code>abstract class <b>Foo</b> extends Bar<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>abstract class Foo extends Bar { }\nclass Bar { }");
  }

  public void testParamClassSig() throws Exception {
    doTest("<code>class <b>Foo</b>&lt;T&gt;<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>class Foo<T>{ }");
  }

  public void testParamClassSig2() throws Exception {
    doTest("<code>class <b>Foo</b>&lt;T, Z&gt;<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>class Foo<T,Z>{ }");
  }

  public void testParamClassSig3() throws Exception {
    doTest("<code>class <b>Foo</b> implements Bar<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>class Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() throws Exception {
    doTest("<code>class <b>Foo</b> implements Bar, Baz<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>class Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() throws Exception {
    doTest("<code>class <b>Foo</b>&lt;A, B&gt; extends Bar&lt;A,B&gt;<br><br><b>Containing library:</b> test.dart<br></code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }

  public void testParamClassSig6() throws Exception {
    doTest("<code>List&lt;String&gt; <b>ids</b><br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> A<br></code>",
           "class A { foo() { List<String> <caret>ids; }}");
  }

  public void testParamClassSig7() throws Exception {
    doTest("<code>List&lt;Map&lt;String, int&gt;&gt; <b>ids</b><br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> A<br></code>",
           "class A { foo() { List<Map<String, int>> <caret>ids; }}");
  }

  public void testParamClassSig8() throws Exception {
    doTest(
      "<code>List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt; <b>list</b><br><br>" +
      "<b>Containing library:</b> test.dart<br><b>Containing class:</b> A<br></code>",
      "class A { foo() { List<List<Map<String, List<Object>>>> <caret>list; }}");
  }

  public void testMetaClassSig1() throws Exception {
    doTest("<code>class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>",
           " @deprecated class <caret>A {}");
  }

  public void testMetaClassSig2() throws Exception {
    doTest("<code>class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>",
           "@Meta(\'foo\') class <caret>A {};\n" +
           "class Meta {\n" +
           "  final String name;\n" +
           "  const Meta([this.name]);\n" +
           "}");
  }

  public void testLibraryClassDoc() throws Exception {
    doTest("<code>class <b>A</b><br><br><b>Containing library:</b> c.b.a<br></code>",
           "library c.b.a;\nclass <caret>A {}");
  }

  public void testImplementsSig1() throws Exception {
    doTest("<code>abstract class <b>Foo</b> implements Bar&lt;T&gt;<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>abstract class Foo implements Bar<T> { }\nclass Bar { }");
  }

  public void testMixinSig1() throws Exception {
    doTest(
      "<code>class <b>Foo2</b> extends Bar1&lt;E&gt; with Baz1&lt;K&gt;, Baz2<br><br>" +
      "<b>Containing library:</b> test.dart<br></code>",
      "<caret>class Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testMixinSig2() throws Exception {
    doTest("<code>class <b>X</b> extends Y with Z<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>class X extends Y with Z { }");
  }

  public void testEnumSig() throws Exception {
    doTest("<code>enum <b>Foo</b><br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>enum Foo { BAR }");
  }

  public void testFunctionSig1() throws Exception {
    doTest("<code><b>calc</b>(int x) " + RIGHT_ARROW + " int<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>int calc(int x) => x + 42;");
  }

  public void testFunctionSig2() throws Exception {
    doTest("<code><b>foo</b>([int x = 3]) " + RIGHT_ARROW + " dynamic<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() throws Exception {
    doTest("<code><b>foo</b>([int x = 3]) " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>void foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() throws Exception {
    doTest("<code><b>foo</b>(int x, {int y, int z}) " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>void foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() throws Exception {
    doTest("<code><b>x</b>(List&lt;E&gt; e) " + RIGHT_ARROW + " E<br><br><b>Containing library:</b> test.dart<br></code>",
           "E <caret>x(List<E> e) { }");
  }

  public void testFunctionSig6() throws Exception {
    doTest(
      "<code><b>calc</b>(x() " + RIGHT_ARROW + " int) " + RIGHT_ARROW + " int<br><br><b>Containing library:</b> test.dart<br></code>",
      "<caret>int calc(int x()) => null;");
  }

  public void testFunctionSig7() throws Exception {
    doTest("<code><b>foo</b>(Map&lt;int, String&gt; p) " + RIGHT_ARROW + " Map&lt;String, int&gt;<br><br>" +
           "<b>Containing library:</b> test.dart<br></code>",
           "Map<String, int> <caret>foo(Map<int, String> p) => null;");
  }

  public void testFunctionSig8() throws Exception {
    doTest("<code><b>x</b>() " + RIGHT_ARROW + " dynamic<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>x() => null;");
  }

  public void testFunctionSig9() throws Exception {
    doTest("<code><b>x</b>({bool b: true}) " + RIGHT_ARROW + " dynamic<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>x({bool b: true}){};");
  }

  public void testFunctionSig10() throws Exception {
    doTest("<code><b>x</b>({bool b}) " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br></code>",
           "void <caret>x({bool b}){};");
  }

  public void testFunctionType() throws Exception {
    doTest("<code><b>x</b>({bool b}) " + RIGHT_ARROW + " Function<br><br><b>Containing library:</b> test.dart<br></code>",
           "Function<T>(y) <caret>x({bool b}){};");
  }

  public void testTypedefSig() throws Exception {
    doTest("<code>typedef <b>a</b>(int x) " + RIGHT_ARROW + " int<br><br><b>Containing library:</b> test.dart<br></code>",
           "<caret>typedef int a(int x);");
  }

  public void testFieldSig1() throws Exception {
    doTest("<code>int <b>y</b><br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>int y = 42; }");
  }

  public void testFieldSig2() throws Exception {
    doTest("<code>int <b>y</b><br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>int y; }");
  }

  public void testMethodSig1() throws Exception {
    doTest("<code><b>y</b>() " + RIGHT_ARROW + " int<br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>int y() => 42; }");
  }

  public void testNamedConstructorSig() throws Exception {
    doTest("<code><b>Z.</b><b>z</b>() " + RIGHT_ARROW + " Z<br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>Z.z(); }");
  }

  public void testConstructorSig() throws Exception {
    doTest("<code><b>Z</b>() " + RIGHT_ARROW + " Z<br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>Z(); }");
  }

  public void testGetterSig() throws Exception {
    doTest("<code>get <b>x</b> " + RIGHT_ARROW + " int<br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>int get x => 0; }");
  }

  public void testSetterSig() throws Exception {
    doTest("<code>set <b>x</b>(int x) " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br><b>Containing class:</b> Z<br></code>",
           "class Z { <caret>void set x(int x) { } }");
  }

  public void testTopLevelVarDoc1() throws Exception {
    doTest("<code>var <b>x</b><br><br><b>Containing library:</b> a.b.c<br></code>\n<p>docs1\ndocs2</p>",
           "library a.b.c;\n" +
           "/// docs1\n" +
           "/// docs2\n" +
           "<caret>@deprecated var x = 'foo';");
  }

  public void testTopLevelVarDoc2() throws Exception {
    doTest("<code>int <b>x</b><br><br><b>Containing library:</b> a.b.c<br></code>",
           "library a.b.c;\n<caret>int x = 3;\n");
  }

  public void testFunctionDoc1() throws Exception {
    doTest("<code><b>foo</b>(int x) " + RIGHT_ARROW + " void<br><br>" +
           "<b>Containing library:</b> test.dart<br></code>\n<p>A function on <code>x</code>s.</p>",
           "/// A function on <code>x</code>s.\n<caret>void foo(int x) { }");
  }

  public void testFunctionDoc2() throws Exception {
    doTest("<code><b>foo</b>(int x) " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br></code>\n" +
           "<p>Good for:</p>\n\n" +
           "<ul>\n" +
           "<li>this</li>\n" +
           "<li>that</li>\n" +
           "</ul>",
           "/** Good for:\n\n" +
           " * * this\n" +
           " * * that\n" +
           "*/\n" +
           "\n<caret>void foo(int x) { }");
  }

  public void testClassMultilineDoc1() throws Exception {
    doTest("<code>class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>\n<p>doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code\n" +
           "</code></pre>",

           "/** 1 */\n" +
           "/**\n" +
           " *      doc1\n" +
           " * doc2\n" +
           " *  doc3\n" +
           " *\n" +
           " *    doc4\n" +
           " * \n" +
           " *     code\n" +
           " */\n" +
           "// non-doc\n" +
           "<caret>class A{}");
  }

  public void testClassMultilineDoc2() throws Exception {
    doTest("<code>abstract class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>\n<p>doc1\n" +
           "doc2\n" +
           " doc3\n" +
           "doc4\n" +
           "doc5\n" +
           " doc6</p>",

           "@deprecated\n" +
           "/**\n" +
           "*doc1\n" +
           "* doc2\n" +
           "*  doc3\n" +
           "     *doc4\n" +
           "     * doc5\n" +
           "     *  doc6\n" +
           " */\n" +
           "<caret>abstract class A{}");
  }

  public void testClassSingleLineDocs1() throws Exception {
    doTest("<code>class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>\n<p>doc1 <br />\n" +
           "doc2</p>",

           "// not doc \n"    +
           "///   doc1  \n"   +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n"   +
           "<caret>class A{}");
  }

  public void testClassSingleLineDocs2() throws Exception {
    doTest("<code>class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>\n<p>doc1 <br />\n" +
           "doc2</p>",

           "@deprecated"      +
           "// not doc \n"    +
           "///   doc1  \n"   +
           " /* not doc */\n" +
           "   ///doc2   \n"  +
           " // not doc \n"   +
           "<caret>class A{}");
  }

  public void testMethodMultilineDoc() throws Exception {
    doTest("<code><b>foo</b>() " + RIGHT_ARROW + " dynamic<br><br><b>Containing library:</b> test.dart<br>" +
           "<b>Containing class:</b> A<br></code>\n<p>doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code\n" +
           "</code></pre>",

           "class A{\n" +
           "/** 1 */\n" +
           "/**\n" +
           " *      doc1\n" +
           " * doc2\n" +
           " *  doc3\n" +
           " *\n" +
           " *    doc4\n" +
           " * \n" +
           " *     code\n" +
           " */\n" +
           "// non-doc\n" +
           "<caret>foo(){}\n" +
           "}");
  }

  public void testMethodSingleLineDocs() throws Exception {
    doTest("<code><b>foo</b>() " + RIGHT_ARROW + " dynamic<br><br><b>Containing library:</b> test.dart<br>" +
           "<b>Containing class:</b> A<br></code>\n<p>doc1  </p>\n"                                                       +
           "\n"                                                                                                               +
           "<pre><code>    doc2\n"                                                                                            +
           "</code></pre>",

           "class A{\n"           +
           "// not doc \n"        +
           "///   doc1  \n"       +
           " /* not doc */\n"     +
           "   ///\n"             +
           "   ///     doc2   \n" +
           "   ///\n"             +
           " // not doc \n"       +
           "<caret>foo(){}\n"     +
           "}");
  }

  public void testHyperlink() throws Exception {
    doTest("<code><b>foo</b>() " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br></code>\n" +
           "<p>my <a href=\"www.cheese.com\">fancy link</a></p>",
           "/// my [fancy link](www.cheese.com)\nvoid <caret>foo() => null;\n");
  }

  public void testHyperlinkMultiLine() throws Exception {
    doTest("<code><b>foo</b>() " + RIGHT_ARROW + " void<br><br><b>Containing library:</b> test.dart<br></code>\n" +
           "<p>my <a href=\"www.cheese.com\">fancy\n" +
           "link</a></p>",
           "/// my [fancy\n/// link](www.cheese.com)\nvoid <caret>foo() => null;\n");
  }

}
