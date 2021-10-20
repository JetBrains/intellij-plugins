// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.ide.documentation.DartDocumentationProvider;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.dartlang.analysis.server.protocol.HoverInformation;

public class DartServerDocUtilTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  public void testAbstractClassSig() {
    doTest("<code><b>test.dart</b><br>abstract&nbsp;class&nbsp;Foo&nbsp;extends&nbsp;Bar<br><br></code>",
           "abstract class <caret>Foo extends Bar { }\nclass Bar { }");
  }

  public void testClassMultilineDoc1() {
    doTest("<code><b>test.dart</b><br>class&nbsp;A<br><br></code>\n" +
           "<p>doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code</code></pre>",

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
           "class <caret>A{}");
  }

  public void testClassMultilineDoc2() {
    doTest("<code><b>test.dart</b><br>abstract&nbsp;class&nbsp;A<br><br></code>\n<p>doc1\n" +
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
           "abstract class <caret>A{}");
  }

  public void testClassSingleLineDocs1() {
    doTest("<code><b>test.dart</b><br>class&nbsp;A<br><br></code>\n<p>doc1\n" +
           "doc2</p>",

           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "class <caret>A{}");
  }

  public void testClassSingleLineDocs2() {
    doTest("<code><b>test.dart</b><br>class&nbsp;A<br><br></code>\n<p>doc1\n" +
           "doc2</p>",

           "@deprecated" +
           "// not doc \n" +
           "///   doc1  \n" +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n" +
           "class <caret>A{}");
  }

  public void testConstructorSig1() {
    // unnamed constructor at declaration
    doTest("<code><b>test.dart</b><br>A&nbsp;A(int&nbsp;one,&nbsp;int&nbsp;two)<br><br><b>Containing class:</b> A<br><br></code>\n" +
           "<p>constructor comment</p>",

           "class A { /** constructor comment */ A<caret>(int one, int two); }");
  }

  public void testConstructorSig2() {
    // named constructor at declaration
    doTest(
      "<code><b>test.dart</b><br>A&nbsp;A.name(int&nbsp;one,&nbsp;int&nbsp;two)<br><br><b>Containing class:</b> A<br><br></code>\n" +
      "<p>constructor comment</p>",

      "class A { /** constructor comment */ A.name<caret>(int one, int two); }");
  }

  public void testConstructorSig3() {
    // unnamed constructor at instantiation
    doTest("<code><b>test.dart</b><br>A&nbsp;A(int&nbsp;one,&nbsp;int&nbsp;two)<br><br><b>Containing class:</b> A<br><br></code>\n" +
           "<p>constructor comment</p>",

           "class A { /** constructor comment */ A(int one, int two);}" +
           "void m() { var a = new A<caret>(1,2); }");
  }

  public void testConstructorSig4() {
    // named constructor at instantiation
    doTest(
      "<code><b>test.dart</b><br>A&nbsp;A.name(int&nbsp;one,&nbsp;int&nbsp;two)<br><br><b>Containing class:</b> A<br><br></code>\n" +
      "<p>constructor comment</p>",

      "class A { /** constructor comment */ A.name(int one, int two); }" +
      "void m() { var a = new A.name<caret>(1,2); }");
  }

  public void testConstructorSig5() {
    // unnamed constructor at instantiation, implicit new
    doTest(
      "<code><b>test.dart</b><br>(new)&nbsp;A&nbsp;A(int&nbsp;one,&nbsp;int&nbsp;two)<br><br><b>Containing class:</b> A<br><br></code>\n" +
      "<p>constructor comment</p>",

      "class A { /** constructor comment */ A(int one, int two); }" +
      "void m() { var a = A<caret>(1,2); }");
  }

  public void testConstructorSig6() {
    // named constructor at instantiation, implicit new
    doTest(
      "<code><b>test.dart</b><br>(new)&nbsp;A&nbsp;A.name(int&nbsp;one,&nbsp;int&nbsp;two)<br><br><b>Containing class:</b> A<br><br></code>\n" +
      "<p>constructor comment</p>",

      "class A { /** constructor comment */ A.name(int one, int two); }" +
      "void m() { var a = A.name<caret>(1,2); }");
  }

  public void testEnumSig() {
    doTest("<code><b>test.dart</b><br>enum&nbsp;Foo<br><br></code>", "enum <caret>Foo { BAR }");
  }

  public void testFieldSig1() {
    doTest("<code><b>test.dart</b><br>int&nbsp;y<br><br><b>Containing class:</b> Z<br><br><b>Type:</b> int<br><br></code>",
           "class Z { int <caret>y = 42; }");
  }

  public void testFieldSig2() {
    doTest("<code><b>test.dart</b><br>int&nbsp;y<br><br><b>Containing class:</b> Z<br><br><b>Type:</b> int<br><br></code>",
           "class Z { int <caret>y; }");
  }

  public void testFunctionDoc1() {
    doTest("<code><b>test.dart</b><br>void&nbsp;foo(int&nbsp;x)<br><br></code>\n<p>A function on <code>x</code>s.</p>",
           "/// A function on [x]s.\nvoid <caret>foo(int x) { }");
  }

  public void testFunctionDoc2() {
    doTest("<code><b>test.dart</b><br>void&nbsp;foo(int&nbsp;x)<br><br></code>\n<p>Good for:</p>\n\n" +
           "<ul><li>this</li>\n" +
           "<li>that</li></ul>", "/** Good for:\n\n" +
                                 " * * this\n" +
                                 " * * that\n" +
                                 "*/\n" +
                                 "\nvoid <caret>foo(int x) { }");
  }

  public void testFunctionSig1() {
    doTest("<code><b>test.dart</b><br>int&nbsp;calc(int&nbsp;x)<br><br></code>", "int <caret>calc(int x) => x + 42;");
  }

  public void testFunctionSig10() {
    doTest("<code><b>test.dart</b><br>void&nbsp;x({bool&nbsp;b})<br><br></code>", "void <caret>x({bool b}){};");
  }

  public void testFunctionSig11() {
    doTest(
      "<code><b>test.dart</b><br>List<String>&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true,&nbsp;List<String>&nbsp;c})<br><br></code>",
      "List<String> <caret>x({bool b= true, List<String> c}){}");
  }

  public void testFunctionSig12() {
    doTest(
      "<code><b>test.dart</b><br>List<String>&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true,&nbsp;Map<String,&nbsp;String>&nbsp;c})<br><br></code>",
      "List<String> <caret>x({bool b= true, Map<String, String> c}){}");
  }

  public void testFunctionSig13() {
    doTest(
      "<code><b>test.dart</b><br>Map<String,&nbsp;String>&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true,&nbsp;List<String>&nbsp;c})<br><br></code>",
      "Map<String, String> <caret>x({bool b= true, List<String> c}){}");
  }

  public void testFunctionSig14() {
    doTest(
      "<code><b>test.dart</b><br>Map<String,&nbsp;String>&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true,&nbsp;Map<String,&nbsp;String>&nbsp;c})<br><br></code>",
      "Map<String, String> <caret>x({bool b= true, Map<String, String> c}){}");
  }

  public void testFunctionSig15() {
    doTest(
      "<code><b>test.dart</b><br>Map<String,&nbsp;Map<String,&nbsp;String>>&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true,&nbsp;Map<String,&nbsp;Map<String,&nbsp;String>>&nbsp;c})<br><br></code>",
      "Map<String, Map<String, String>> <caret>x({bool b= true, Map<String, Map<String, String>> c}){}");
  }

  public void testFunctionSig16() {
    doTest(
      "<code><b>test.dart</b><br>String&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true,&nbsp;dynamic&nbsp;Function(dynamic,&nbsp;dynamic,&nbsp;dynamic)&nbsp;Function})<br><br></code>",
      "String <caret>x({bool b= true, Function(String, String, String)}){}");
  }

  public void testFunctionSig2() {
    doTest("<code><b>test.dart</b><br>dynamic&nbsp;foo([int&nbsp;x&nbsp;=&nbsp;3])<br><br></code>",
           "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() {
    doTest("<code><b>test.dart</b><br>void&nbsp;foo([int&nbsp;x&nbsp;=&nbsp;3])<br><br></code>",
           "void <caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() {
    doTest(
      "<code><b>test.dart</b><br>void&nbsp;foo(<br/>&nbsp;&nbsp;int&nbsp;x,&nbsp;{<br/>&nbsp;&nbsp;int&nbsp;y,<br/>&nbsp;&nbsp;int&nbsp;z,<br/>})<br><br></code>",
      "void <caret>foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() {
    doTest("<code><b>test.dart</b><br>dynamic&nbsp;x(List<dynamic>&nbsp;e)<br><br></code>", "E <caret>x(List<E> e) { }");
  }

  public void testFunctionSig6() {
    doTest("<code><b>test.dart</b><br>int&nbsp;calc(int&nbsp;Function()&nbsp;x)<br><br></code>", "int <caret>calc(int x()) => null;");
  }

  public void testFunctionSig7() {
    doTest("<code><b>test.dart</b><br>Map<String,&nbsp;int>&nbsp;foo(Map<int,&nbsp;String>&nbsp;p)<br><br></code>",
           "Map<String, int> <caret>foo(Map<int, String> p) => null;");
  }

  public void testFunctionSig8() {
    doTest("<code><b>test.dart</b><br>dynamic&nbsp;x()<br><br></code>", "<caret>x() => null;");
  }

  public void testFunctionSig9() {
    doTest("<code><b>test.dart</b><br>dynamic&nbsp;x({bool&nbsp;b&nbsp;=&nbsp;true})<br><br></code>", "<caret>x({bool b: true}){};");
  }

  public void testGetterSig() {
    doTest("<code><b>test.dart</b><br>int&nbsp;get&nbsp;x<br><br><b>Containing class:</b> Z<br><br></code>",
           "class Z { int get <caret>x => 0; }");
  }

  public void testImplementsSig1() {
    doTest("<code><b>test.dart</b><br>abstract&nbsp;class&nbsp;Foo&nbsp;implements&nbsp;Bar<br><br></code>",
           "abstract class <caret>Foo implements Bar<T> { }\nclass Bar { }");
  }

  public void testLibraryClassDoc() {
    doTest("<code><b>test.dart</b><br>class&nbsp;A<br><br></code>", "library c.b.a;\nclass <caret>A {}");
  }

  public void testMetaClassSig1() {
    doTest("<code><b>test.dart</b><br>class&nbsp;A<br><br></code>", " @deprecated class <caret>A {}");
  }

  public void testMethodMultilineDoc() {
    doTest("<code><b>test.dart</b><br>dynamic&nbsp;foo()<br><br><b>Containing class:</b> A<br><br></code>\n" +
           "<p>doc1\n" +
           "doc2\n" +
           " doc3</p>\n" +
           "\n" +
           "<p>   doc4</p>\n" +
           "\n" +
           "<pre><code>    code</code></pre>",

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

  public void testMethodSig1() {
    doTest("<code><b>test.dart</b><br>int&nbsp;y()<br><br><b>Containing class:</b> Z<br><br></code>", "class Z { int <caret>y() => 42; }");
  }

  public void testMethodSingleLineDocs() {
    doTest("<code><b>test.dart</b><br>dynamic&nbsp;foo()<br><br><b>Containing class:</b> A<br><br></code>\n<p>doc1\n" +
           "doc2</p>", "class A{\n" +
                       "// not doc \n" +
                       "///   doc1  \n" +
                       " /* not doc */\n" +
                       "   /// doc2   \n" +
                       " // not doc \n" +
                       "<caret>foo(){}\n" +
                       "}");
  }

  public void testMixinSig1() {
    doTest("<code><b>test.dart</b><br>class&nbsp;Foo2&nbsp;extends&nbsp;Bar1&nbsp;with&nbsp;Baz1,&nbsp;Baz2<br><br></code>",
           "class Bar1 {} class Baz1{} class Baz2 {} class <caret>Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testMixinSig2() {
    doTest("<code><b>test.dart</b><br>class&nbsp;X&nbsp;extends&nbsp;Y&nbsp;with&nbsp;Z<br><br></code>",
           "class Y {} class Z {} class <caret>X extends Y with Z { }");
  }

  public void testNamedConstructorSig() {
    doTest("<code><b>test.dart</b><br>Z&nbsp;Z.z()<br><br><b>Containing class:</b> Z<br><br></code>", "class Z { <caret>Z.z(); }");
  }

  public void testParamClassSig() {
    doTest("<code><b>test.dart</b><br>class&nbsp;Foo<T><br><br></code>", "class <caret>Foo<T>{ }");
  }

  public void testParamClassSig2() {
    doTest("<code><b>test.dart</b><br>class&nbsp;Foo<T,&nbsp;Z><br><br></code>",
           "class <caret>Foo<T,Z>{ }");
  }

  public void testParamClassSig3() {
    doTest("<code><b>test.dart</b><br>class&nbsp;Foo&nbsp;implements&nbsp;Bar<br><br></code>",
           "class <caret>Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() {
    doTest("<code><b>test.dart</b><br>class&nbsp;Foo&nbsp;implements&nbsp;Bar,&nbsp;Baz<br><br></code>",
           "class <caret>Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() {
    doTest("<code><b>test.dart</b><br>class&nbsp;Foo<A,&nbsp;B>&nbsp;extends&nbsp;Bar<A,&nbsp;B><br><br></code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }

  public void testParamClassSig6() {
    doTest("<code>List<String>&nbsp;ids<br><br><b>Type:</b> List&lt;String&gt;<br><br></code>",
           "class A { foo() { List<String> <caret>ids; }}");
  }

  public void testParamClassSig7() {
    doTest(
      "<code>List<Map<String,&nbsp;int>>&nbsp;ids<br><br><b>Type:</b> List&lt;Map&lt;String, int&gt;&gt;<br><br></code>",
      "class A { foo() { List<Map<String, int>> <caret>ids; }}");
  }

  public void testParamClassSig8() {
    doTest("<code>List<List<Map<String,&nbsp;List<Object>>>>&nbsp;list<br><br>" +
           "<b>Type:</b> List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt;<br><br></code>",
           "class A { foo() { List<List<Map<String, List<Object>>>> <caret>list; }}");
  }

  public void testSetterSig() {
    doTest(
      "<code><b>test.dart</b><br>void&nbsp;set&nbsp;x(int&nbsp;x)<br><br><b>Containing class:</b> Z<br><br></code>",
      "class Z { void set <caret>x(int x) { } }");
  }

  public void testTopLevelVarDoc1() {
    doTest("<code><b>test.dart</b><br>String&nbsp;x<br><br><b>Type:</b> String<br><br></code>\n<p>docs1\ndocs2</p>",
           "library a.b.c;\n" + "/// docs1\n" + "/// docs2\n" + "@deprecated var <caret>x = 'foo';");
  }

  public void testTopLevelVarDoc2() {
    doTest("<code><b>test.dart</b><br>int&nbsp;x<br><br><b>Type:</b> int<br><br></code>",
           "library a.b.c;\nint <caret>x = 3;\n");
  }

  public void testTypedefSig() {
    doTest("<code><b>test.dart</b><br>typedef&nbsp;F&nbsp;=&nbsp;int&nbsp;Function(int&nbsp;x)<br><br></code>",
           "typedef int <caret>F(int x);");
  }

  private void doTest(String expectedDoc, String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.configureByText("test.dart", realContents);
    myFixture.doHighlighting(); // warm up
    final HoverInformation hover = DartDocumentationProvider.getSingleHover(psiFile, caretOffset);
    assertNotNull(hover);
    final String doc = DartDocumentationProvider.generateDocServer(getProject(), hover);
    assertEquals(expectedDoc, doc);
  }
}
