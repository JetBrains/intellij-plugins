// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  public void testAbstractClassSig() {
    doTest("<code><b>test.dart</b><br>abstract class <b>Foo</b> extends Bar<br><br></code>",
           "<caret>abstract class Foo extends Bar { }\nclass Bar { }");
  }

  public void testParamClassSig() {
    doTest("<code><b>test.dart</b><br>class <b>Foo</b>&lt;T&gt;<br><br></code>",
           "<caret>class Foo<T>{ }");
  }

  public void testParamClassSig2() {
    doTest("<code><b>test.dart</b><br>class <b>Foo</b>&lt;T, Z&gt;<br><br></code>",
           "<caret>class Foo<T,Z>{ }");
  }

  public void testParamClassSig3() {
    doTest("<code><b>test.dart</b><br>class <b>Foo</b> implements Bar<br><br></code>",
           "<caret>class Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() {
    doTest("<code><b>test.dart</b><br>class <b>Foo</b> implements Bar, Baz<br><br></code>",
           "<caret>class Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() {
    doTest("<code><b>test.dart</b><br>class <b>Foo</b>&lt;A, B&gt; extends Bar&lt;A,B&gt;<br><br></code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }

  public void testParamClassSig6() {
    doTest("<code><b>test.dart</b><br>List&lt;String&gt; <b>ids</b><br><br><b>Containing class:</b> A<br><br></code>",
           "class A { foo() { List<String> <caret>ids; }}");
  }

  public void testParamClassSig7() {
    doTest(
      "<code><b>test.dart</b><br>List&lt;Map&lt;String, int&gt;&gt; <b>ids</b><br><br><b>Containing class:</b> A<br><br></code>",
      "class A { foo() { List<Map<String, int>> <caret>ids; }}");
  }

  public void testParamClassSig8() {
    doTest(
      "<code><b>test.dart</b><br>List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt; <b>list</b><br><br><b>Containing class:</b> A<br><br></code>",
      "class A { foo() { List<List<Map<String, List<Object>>>> <caret>list; }}");
  }

  public void testMetaClassSig1() {
    doTest("<code><b>test.dart</b><br>class <b>A</b><br><br></code>",
           " @deprecated class <caret>A {}");
  }

  public void testMetaClassSig2() {
    doTest("<code><b>test.dart</b><br>class <b>A</b><br><br></code>",
           "@Meta(\'foo\') class <caret>A {};\n" +
           "class Meta {\n" +
           "  final String name;\n" +
           "  const Meta([this.name]);\n" +
           "}");
  }

  public void testLibraryClassDoc() {
    doTest("<code><b>c.b.a</b><br>class <b>A</b><br><br></code>",
           "library c.b.a;\nclass <caret>A {}");
  }

  public void testImplementsSig1() {
    doTest("<code><b>test.dart</b><br>abstract class <b>Foo</b> implements Bar&lt;T&gt;<br><br></code>",
           "<caret>abstract class Foo implements Bar<T> { }\nclass Bar { }");
  }

  public void testMixinSig1() {
    doTest(
      "<code><b>test.dart</b><br>class <b>Foo2</b> extends Bar1&lt;E&gt; with Baz1&lt;K&gt;, Baz2<br><br></code>",
      "<caret>class Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testMixinSig2() {
    doTest("<code><b>test.dart</b><br>class <b>X</b> extends Y with Z<br><br></code>",
           "<caret>class X extends Y with Z { }");
  }

  public void testEnumSig() {
    doTest("<code><b>test.dart</b><br>enum <b>Foo</b><br><br></code>",
           "<caret>enum Foo { BAR }");
  }

  public void testFunctionSig1() {
    doTest("<code><b>test.dart</b><br><b>calc</b>(int x) " + RIGHT_ARROW + " int<br><br></code>",
           "<caret>int calc(int x) => x + 42;");
  }

  public void testFunctionSig2() {
    doTest("<code><b>test.dart</b><br><b>foo</b>([int x = 3]) " + RIGHT_ARROW + " dynamic<br><br></code>",
           "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() {
    doTest("<code><b>test.dart</b><br><b>foo</b>([int x = 3]) " + RIGHT_ARROW + " void<br><br></code>",
           "<caret>void foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() {
    doTest("<code><b>test.dart</b><br><b>foo</b>(int x, {int y, int z}) " + RIGHT_ARROW + " void<br><br></code>",
           "<caret>void foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() {
    doTest("<code><b>test.dart</b><br><b>x</b>(List&lt;E&gt; e) " + RIGHT_ARROW + " E<br><br></code>",
           "E <caret>x(List<E> e) { }");
  }

  public void testFunctionSig6() {
    doTest(
      "<code><b>test.dart</b><br><b>calc</b>(x() " + RIGHT_ARROW + " int) " + RIGHT_ARROW + " int<br><br></code>",
      "<caret>int calc(int x()) => null;");
  }

  public void testFunctionSig7() {
    doTest("<code><b>test.dart</b><br><b>foo</b>(Map&lt;int, String&gt; p) " + RIGHT_ARROW + " Map&lt;String, int&gt;<br><br></code>",
           "Map<String, int> <caret>foo(Map<int, String> p) => null;");
  }

  public void testFunctionSig8() {
    doTest("<code><b>test.dart</b><br><b>x</b>() " + RIGHT_ARROW + " dynamic<br><br></code>",
           "<caret>x() => null;");
  }

  public void testFunctionSig9() {
    doTest("<code><b>test.dart</b><br><b>x</b>({bool b: true}) " + RIGHT_ARROW + " dynamic<br><br></code>",
           "<caret>x({bool b: true}){};");
  }

  public void testFunctionSig10() {
    doTest("<code><b>test.dart</b><br><b>x</b>({bool b}) " + RIGHT_ARROW + " void<br><br></code>",
           "void <caret>x({bool b}){};");
  }

  public void testFunctionType() {
    doTest("<code><b>test.dart</b><br><b>x</b>({bool b}) " + RIGHT_ARROW + " Function<br><br></code>",
           "Function<T>(y) <caret>x({bool b}){};");
  }

  public void testTypedefSig() {
    doTest("<code><b>test.dart</b><br>typedef <b>a</b>(int x) " + RIGHT_ARROW + " int<br><br></code>",
           "<caret>typedef int a(int x);");
  }

  public void testFieldSig1() {
    doTest("<code><b>test.dart</b><br>int <b>y</b><br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>int y = 42; }");
  }

  public void testFieldSig2() {
    doTest("<code><b>test.dart</b><br>int <b>y</b><br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>int y; }");
  }

  public void testMethodSig1() {
    doTest("<code><b>test.dart</b><br><b>y</b>() " + RIGHT_ARROW + " int<br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>int y() => 42; }");
  }

  public void testNamedConstructorSig() {
    doTest("<code><b>test.dart</b><br><b>Z.</b><b>z</b>() " + RIGHT_ARROW + " Z<br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>Z.z(); }");
  }

  public void testConstructorSig() {
    doTest("<code><b>test.dart</b><br><b>Z</b>() " + RIGHT_ARROW + " Z<br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>Z(); }");
  }

  public void testGetterSig() {
    doTest("<code><b>test.dart</b><br>get <b>x</b> " + RIGHT_ARROW + " int<br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>int get x => 0; }");
  }

  public void testSetterSig() {
    doTest("<code><b>test.dart</b><br>set <b>x</b>(int x) " + RIGHT_ARROW + " void<br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { <caret>void set x(int x) { } }");
  }

  public void testTopLevelVarDoc1() {
    doTest("<code><b>a.b.c</b><br>var <b>x</b><br><br></code>\n<p>docs1\ndocs2</p>",
           "library a.b.c;\n" +
           "/// docs1\n" +
           "/// docs2\n" +
           "<caret>@deprecated var x = 'foo';");
  }

  public void testTopLevelVarDoc2() {
    doTest("<code><b>a.b.c</b><br>int <b>x</b><br><br></code>",
           "library a.b.c;\n<caret>int x = 3;\n");
  }

  public void testFunctionDoc1() {
    doTest("<code><b>test.dart</b><br><b>foo</b>(int x) " + RIGHT_ARROW + " void<br><br></code>\n" +
           "<p>A function on <code>x</code>s.</p>",
           "/// A function on <code>x</code>s.\n<caret>void foo(int x) { }");
  }

  public void testFunctionDoc2() {
    doTest("<code><b>test.dart</b><br><b>foo</b>(int x) " + RIGHT_ARROW + " void<br><br></code>\n" +
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

  public void testClassMultilineDoc1() {
    doTest("<code><b>test.dart</b><br>class <b>A</b><br><br></code>\n<p>doc1\n" +
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

  public void testClassMultilineDoc2() {
    doTest("<code><b>test.dart</b><br>abstract class <b>A</b><br><br></code>\n<p>doc1\n" +
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

  public void testClassSingleLineDocs1() {
    doTest("<code><b>test.dart</b><br>class <b>A</b><br><br></code>\n" +
           "<p>doc1 <br />\n" +
           "doc2</p>",

           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "<caret>class A{}");
  }

  public void testClassSingleLineDocs2() {
    doTest("<code><b>test.dart</b><br>class <b>A</b><br><br></code>\n" +
           "<p>doc1 <br />\n" +
           "doc2</p>",

           "@deprecated" +
           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   ///doc2   \n" +
           " // not doc \n" +
           "<caret>class A{}");
  }

  public void testMethodMultilineDoc() {
    doTest(
      "<code><b>test.dart</b><br><b>foo</b>() " + RIGHT_ARROW + " dynamic<br><br><b>Containing class:</b> A<br><br></code>\n<p>doc1\n" +
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

  public void testMethodSingleLineDocs() {
    doTest("<code><b>test.dart</b><br><b>foo</b>() " + RIGHT_ARROW + " dynamic<br><br>" +
           "<b>Containing class:</b> A<br><br></code>\n<p>doc1  </p>\n" +
           "\n" +
           "<pre><code>    doc2\n" +
           "</code></pre>",

           "class A{\n" +
           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   ///\n" +
           "   ///     doc2   \n" +
           "   ///\n" +
           " // not doc \n" +
           "<caret>foo(){}\n" +
           "}");
  }

  public void testHyperlink() {
    doTest("<code><b>test.dart</b><br><b>foo</b>() " + RIGHT_ARROW + " void<br><br></code>\n" +
           "<p>my <a href=\"www.cheese.com\">fancy link</a></p>",
           "/// my [fancy link](www.cheese.com)\nvoid <caret>foo() => null;\n");
  }

  public void testHyperlinkMultiLine() {
    doTest("<code><b>test.dart</b><br><b>foo</b>() " + RIGHT_ARROW + " void<br><br></code>\n" +
           "<p>my <a href=\"www.cheese.com\">fancy\n" +
           "link</a></p>",
           "/// my [fancy\n/// link](www.cheese.com)\nvoid <caret>foo() => null;\n");
  }
}
