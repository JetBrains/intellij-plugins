/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  public void testAbstractClassSig() throws Exception {
    doTest("<code><b>Signature:</b> abstract class Foo extends Bar<br></code>", "abstract class <caret>Foo extends Bar { }\nclass Bar { }");
  }

  public void testClassMultilineDoc1() throws Exception {
    doTest("<code><b>Signature:</b> class A<br></code><br><pre><code>     doc1\n" +
           "</code></pre>\n"                                                      +
           "\n"                                                                   +
           "<p>doc2\n"                                                            +
           " doc3</p>\n"                                                          +
           "\n"                                                                   +
           "<p>   doc4</p>\n"                                                     +
           "\n"                                                                   +
           "<pre><code>    code\n"                                                +
           "</code></pre>\n",

           "/** 1 */\n"     +
           "/**\n"          +
           " *      doc1\n" +
           " * doc2\n"      +
           " *  doc3\n"     +
           " *\n"           +
           " *    doc4\n"   +
           " * \n"          +
           " *     code\n"  +
           " */\n"          +
           "// non-doc\n"   +
           "class <caret>A{}");
  }

  public void testClassMultilineDoc2() throws Exception {
    doTest("<code><b>Signature:</b> abstract class A<br></code><br><p>doc1\n" +
           "doc2\n"                                                           +
           " doc3\n"                                                          +
           "doc4\n"                                                           +
           "doc5\n"                                                           +
           " doc6</p>\n",

           "@deprecated\n"  +
           "/**\n"          +
           "*doc1\n"        +
           "* doc2\n"       +
           "*  doc3\n"      +
           "     *doc4\n"   +
           "     * doc5\n"  +
           "     *  doc6\n" +
           " */\n"          +
           "abstract class <caret>A{}");
  }

  public void testClassSingleLineDocs1() throws Exception {
    doTest("<code><b>Signature:</b> class A<br></code><br><p>  doc1\n" +
           "doc2</p>\n",

           "// not doc \n"    +
           "///   doc1  \n"   +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n"   +
           "class <caret>A{}");
  }

  public void testClassSingleLineDocs2() throws Exception {
    doTest("<code><b>Signature:</b> class A<br></code><br><p>  doc1\n" +
           "doc2</p>\n",

           "@deprecated"      +
           "// not doc \n"    +
           "///   doc1  \n"   +
           " /* not doc */\n" +
           "   /// doc2   \n" +
           " // not doc \n"   +
           "class <caret>A{}");
  }

  public void testConstructorSig() throws Exception {
    doTest("<code><b>Signature:</b> Z() → Z<br><br><b>Containing class:</b> Z<br></code>", "class Z { <caret>Z(); }");
  }

  public void testEnumSig() throws Exception {
    doTest("<code><b>Signature:</b> enum Foo<br></code>", "enum <caret>Foo { BAR }");
  }

  public void testFieldSig1() throws Exception {
    doTest("<code><b>Signature:</b> int y<br><br><b>Containing class:</b> Z<br><br><b>Static type:</b> int<br></code>",
           "class Z { int <caret>y = 42; }");
  }

  public void testFieldSig2() throws Exception {
    doTest("<code><b>Signature:</b> int y<br><br><b>Containing class:</b> Z<br><br><b>Static type:</b> int<br></code>",
           "class Z { int <caret>y; }");
  }

  public void testFunctionDoc1() throws Exception {
    doTest("<code><b>Signature:</b> foo(int x) → void<br></code><br><p>A function on [x]s.</p>\n",
           "/// A function on [x]s.\nvoid <caret>foo(int x) { }");
  }

  public void testFunctionDoc2() throws Exception {
    doTest("<code><b>Signature:</b> foo(int x) → void<br></code><br><p>Good for:</p>\n" +
           "\n" +
           "<ul>\n" +
           "<li>this</li>\n" +
           "<li>that</li>\n" +
           "</ul>\n", "/** Good for:\n\n" +
                      " * * this\n" +
                      " * * that\n" +
                      "*/\n" +
                      "\nvoid <caret>foo(int x) { }");
  }

  //public void testMetaClassSig2() throws Exception {
  //  doTest("<code><b>Signature:</b> class <b>A</b><br><br><b>Containing library:</b> test.dart<br></code>",
  //         "@Meta(\'foo\') class <caret>A {};\n" +
  //         "class Meta {\n" +
  //         "  final String name;\n" +
  //         "  const Meta([this.name]);\n" +
  //         "}");
  //}

  public void testFunctionSig1() throws Exception {
    doTest("<code><b>Signature:</b> calc(int x) → int<br></code>", "int <caret>calc(int x) => x + 42;");
  }

  public void testFunctionSig10() throws Exception {
    doTest("<code><b>Signature:</b> x({bool b}) → void<br></code>", "void <caret>x({bool b}){};");
  }

  public void testFunctionSig2() throws Exception {
    doTest("<code><b>Signature:</b> foo([int x = 3]) → dynamic<br></code>", "<caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig3() throws Exception {
    doTest("<code><b>Signature:</b> foo([int x = 3]) → void<br></code>", "void <caret>foo([int x = 3]) { print(x); }");
  }

  public void testFunctionSig4() throws Exception {
    doTest("<code><b>Signature:</b> foo(int x, {int y, int z}) → void<br></code>", "void <caret>foo(int x, {int y, int z}) { }");
  }

  public void testFunctionSig5() throws Exception {
    doTest("<code><b>Signature:</b> x(List&lt;dynamic&gt; e) → dynamic<br></code>", "E <caret>x(List<E> e) { }");
  }

  public void testFunctionSig6() throws Exception {
    doTest("<code><b>Signature:</b> calc(() → int x) → int<br></code>", "int <caret>calc(int x()) => null;");
  }

  public void testFunctionSig7() throws Exception {
    doTest("<code><b>Signature:</b> foo(Map&lt;int, String&gt; p) → Map&lt;String, int&gt;<br></code>",
           "Map<String, int> <caret>foo(Map<int, String> p) => null;");
  }

  public void testFunctionSig8() throws Exception {
    doTest("<code><b>Signature:</b> x() → dynamic<br></code>", "<caret>x() => null;");
  }

  public void testFunctionSig9() throws Exception {
    doTest("<code><b>Signature:</b> x({bool b: true}) → dynamic<br></code>", "<caret>x({bool b: true}){};");
  }

  public void testGetterSig() throws Exception {
    doTest("<code><b>Signature:</b> get x → int<br><br><b>Containing class:</b> Z<br></code>", "class Z { int get <caret>x => 0; }");
  }

  public void testImplementsSig1() throws Exception {
    doTest("<code><b>Signature:</b> abstract class Foo implements Bar<br></code>",
           "abstract class <caret>Foo implements Bar<T> { }\nclass Bar { }");
  }

  public void testLibraryClassDoc() throws Exception {
    doTest("<code><b>Signature:</b> class A<br><br><b>Containing library:</b> c.b.a<br></code>", "library c.b.a;\nclass <caret>A {}");
  }

  public void testMetaClassSig1() throws Exception {
    doTest("<code><b>Signature:</b> class A<br></code>", " @deprecated class <caret>A {}");
  }

  public void testMethodMultilineDoc() throws Exception {
    doTest("<code><b>Signature:</b> foo() → dynamic<br><br><b>Containing class:</b> A<br></code><br><pre><code>     doc1\n" +
           "</code></pre>\n"                                                                                                +
           "\n"                                                                                                             +
           "<p>doc2\n"                                                                                                      +
           " doc3</p>\n"                                                                                                    +
           "\n"                                                                                                             +
           "<p>   doc4</p>\n"                                                                                               +
           "\n"                                                                                                             +
           "<pre><code>    code\n"                                                                                          +
           "</code></pre>\n",

           "class A{\n"       +
           "/** 1 */\n"       +
           "/**\n"            +
           " *      doc1\n"   +
           " * doc2\n"        +
           " *  doc3\n"       +
           " *\n"             +
           " *    doc4\n"     +
           " * \n"            +
           " *     code\n"    +
           " */\n"            +
           "// non-doc\n"     +
           "<caret>foo(){}\n" +
           "}");
  }

  public void testMethodSig1() throws Exception {
    doTest("<code><b>Signature:</b> y() → int<br><br><b>Containing class:</b> Z<br></code>", "class Z { int <caret>y() => 42; }");
  }

  public void testMethodSingleLineDocs() throws Exception {
    doTest("<code><b>Signature:</b> foo() → dynamic<br><br><b>Containing class:</b> A<br></code><br><p>  doc1\n" +
           "doc2</p>\n", "class A{\n" +
                         "// not doc \n" +
                         "///   doc1  \n" +
                         " /* not doc */\n" +
                         "   /// doc2   \n" +
                         " // not doc \n" +
                         "<caret>foo(){}\n" +
                         "}");
  }

  public void testMixinSig1() throws Exception {
    doTest("<code><b>Signature:</b> class Foo2 extends Bar1 with Baz1, Baz2<br></code>",
           "class Bar1 {} class Baz1{} class Baz2 {} class <caret>Foo2 = Bar1<E> with Baz1<K>, Baz2");
  }

  public void testMixinSig2() throws Exception {
    doTest("<code><b>Signature:</b> class X extends Y with Z<br></code>", "class Y {} class Z {} class <caret>X extends Y with Z { }");
  }

  public void testNamedConstructorSig() throws Exception {
    doTest("<code><b>Signature:</b> Z.z() → Z<br><br><b>Containing class:</b> Z<br></code>", "class Z { <caret>Z.z(); }");
  }

  public void testParamClassSig() throws Exception {
    doTest("<code><b>Signature:</b> class Foo&lt;T&gt;<br></code>", "class <caret>Foo<T>{ }");
  }

  public void testParamClassSig2() throws Exception {
    doTest("<code><b>Signature:</b> class Foo&lt;T, Z&gt;<br></code>", "class <caret>Foo<T,Z>{ }");
  }

  public void testParamClassSig3() throws Exception {
    doTest("<code><b>Signature:</b> class Foo implements Bar<br></code>", "class <caret>Foo implements Bar { }<br/>class Bar { }");
  }

  public void testParamClassSig4() throws Exception {
    doTest("<code><b>Signature:</b> class Foo implements Bar, Baz<br></code>",
           "class <caret>Foo implements Bar, Baz { }<br/>class Bar { }<br/>class Baz { }");
  }

  public void testParamClassSig5() throws Exception {
    doTest("<code><b>Signature:</b> class Foo&lt;A, B&gt; extends Bar&lt;A, B&gt;<br></code>",
           "class <caret>Foo<A,B> extends Bar<A,B> { }<br/>class Bar<A,B> { }");
  }

  public void testParamClassSig6() throws Exception {
    doTest("<code><b>Signature:</b> List&lt;String&gt; ids<br><br><b>Static type:</b> List&lt;String&gt;<br></code>",
           "class A { foo() { List<String> <caret>ids; }}");
  }

  public void testParamClassSig7() throws Exception {
    doTest(
      "<code><b>Signature:</b> List&lt;Map&lt;String, int&gt;&gt; ids<br><br><b>Static type:</b> List&lt;Map&lt;String, int&gt;&gt;<br></code>",
      "class A { foo() { List<Map<String, int>> <caret>ids; }}");
  }

  public void testParamClassSig8() throws Exception {
    doTest("<code><b>Signature:</b> List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt; list<br><br>" +
           "<b>Static type:</b> List&lt;List&lt;Map&lt;String, List&lt;Object&gt;&gt;&gt;&gt;<br></code>",
           "class A { foo() { List<List<Map<String, List<Object>>>> <caret>list; }}");
  }

  public void testSetterSig() throws Exception {
    doTest("<code><b>Signature:</b> set x(int x) → void<br><br><b>Containing class:</b> Z<br></code>",
           "class Z { void set <caret>x(int x) { } }");
  }

  public void testTopLevelVarDoc1() throws Exception {
    doTest("<code><b>Signature:</b> dynamic x<br><br><b>Containing library:</b> a.b.c<br><br>" +
           "<b>Static type:</b> dynamic<br><b>Propagated type:</b> String<br></code><br><p>docs1\n" +
           "docs2</p>\n", "library a.b.c;\n" +
                          "/// docs1\n" +
                          "/// docs2\n" +
                          "@deprecated var <caret>x = 'foo';");
  }

  public void testTopLevelVarDoc2() throws Exception {
    doTest("<code><b>Signature:</b> int x<br><br><b>Containing library:</b> a.b.c<br><br><b>Static type:</b> int<br></code>",
           "library a.b.c;\nint <caret>x = 3;\n");
  }

  public void testTypedefSig() throws Exception {
    doTest("<code><b>Signature:</b> typedef F(int x) → int<br></code>", "typedef int <caret>F(int x);");
  }

  private void doTest(String expectedDoc, String fileContents) {
    final int caretOffset = fileContents.indexOf("<caret>");
    assertTrue(caretOffset != -1);
    final String realContents = fileContents.substring(0, caretOffset) + fileContents.substring(caretOffset + "<caret>".length());
    final PsiFile psiFile = myFixture.configureByText("test.dart", realContents);
    //System.out.println(psiFile.getText());
    myFixture.doHighlighting(); // warm up
    //final DartComponent dartComponent = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent.class);
    //assertNotNull("target element not found at offset " + caretOffset, dartComponent);
    final HoverInformation hover = DartDocumentationProvider.getSingleHover(psiFile, caretOffset);
    assertNotNull(hover);
    //System.out.println(hover);
    final String doc = DartDocumentationProvider.generateDocServer(hover);
    assertEquals(expectedDoc, doc);
    //for (int i = 0; i < 10; i++) {
    //  final HoverInformation hover = DartDocumentationProvider.getSingleHover(psiFile, caretOffset);
    //  System.out.println(hover);
    //  if (hover != null) {
    //    final String doc = DartDocumentationProvider.generateDocServer(hover);
    //    assertEquals(expectedDoc, doc);
    //    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    //    break;
    //  }
    //  Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
    //}
  }
}
