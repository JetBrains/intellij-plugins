package com.intellij.flex.resolver;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSResolveTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSLocalVariable;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;

public class ActionScriptResolveTest extends BaseJSResolveTestCase {

  @Override
  protected String getExtension() {
    return "as";
  }

  @Override
  protected String getSampleName() {
    return "__sample.as";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("resolve/");
  }

  private static void checkResolvedToClass(final ResolveResult[] resolveResults) {
    assertEquals(1, resolveResults.length);
    final PsiElement realElement = getElement(resolveResults[0]);
    assertTrue(realElement instanceof JSClass);
    assertEquals("B", ((JSClass)realElement).getName());
  }

  private static Processor<ResolveResult[]> createProcessor(final String expected) {
    return resolveResults -> {
      assertEquals(1, resolveResults.length);
      PsiElement element = getElement(resolveResults[0]);
      assertTrue(element instanceof JSClass);
      assertEquals(expected, ((JSNamedElement)element).getName());
      return false;
    };
  }

  public void testResolveInClassDynamic() throws Exception {
    String fileText = "public class T {\n" +
                      "  public function m() : String {\n" +
                      "    return \"m\";\n" +
                      "  }\n" +
                      "\n" +
                      "  public function test() : String {\n" +
                      "    function foo():String {\n" +
                      "      return this.<ref>m();\n" +
                      "    }\n" +
                      "    return foo();\n" +
                      "  }\n" +
                      "}\n" +
                      "function m() {}";

    JSReferenceExpression ref = (JSReferenceExpression)configureByFileText(fileText, "sample.js2");
    ResolveResult[] results = ref.multiResolve(false);
    assertEquals(2, results.length);
  }

  public void testResolveIt() throws Exception {
    String fileText = "package {\n" +
                      "\n" +
                      "public class Test {\n" +
                      "  public function Test() {\n" +
                      "    var describeType:XML = describe<ref>Type(Test);\n" +
                      "  }\n" +
                      "}\n" +
                      "function describeType(x) {}";

    JSReferenceExpression ref = (JSReferenceExpression)configureByFileText(fileText, "sample.js2");
    assertTrue(ref.resolve() instanceof JSLocalVariable);
  }

  public void testResolveInClass() throws Exception {
    String fileText = "class A { function get aaa() {} function foo() {\n" +
                      "  var aaa = a<ref>aa;\n}" +
                      "}";

    JSReferenceExpression ref = (JSReferenceExpression)configureByFileText(fileText, "sample.js2");
    assertTrue(ref.resolve() instanceof JSFunction);
  }

  public void testObjectProperties() throws Exception {
    String fileText = "package {\n" +
                      "public class Foo {\n" +
                      "    function bar() {" +
                      "        var x:Object = {};\n" +
                      "        x.toStri<ref>ng();\n" +
                      "    }\n" +
                      "}\n" +
                      "}";
    JSReferenceExpression ref = (JSReferenceExpression)configureByFileText(fileText, "sample.as");
    final PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
  }

  public void testResolveOfGetProperty() throws Exception {
    String fileText = "class A { public function get foo() : String { return \"foo\"; } }\n" +
                      "function zzz() {\n" +
                      "var a : A;\n" +
                      "a.f<ref>oo" +
                      "}";

    PsiReference ref = configureByFileText(fileText, "sample.js2");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof JSFunction);
  }

  public void testResolve23_4() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_5() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("C", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_6() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("C", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_7() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("E", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_8() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_9() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_10() throws Exception {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), testName + ".as", testName + "_2.as", testName + "_3.as");
  }

  public void testResolve23_11() throws Exception {
    doMultipleResolveTestForFile(resolveResults -> {
      checkResolvedToClass(resolveResults);
      return false;
    }, getTestName(false) + ".as");
  }

  public void testResolve23_12() throws Exception {
    doMultipleResolveTestForFile(resolveResults -> {
      checkResolvedToClass(resolveResults);
      return false;
    }, getTestName(false) + ".as");
  }


  public void testResolve24() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve24_2() throws Exception {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve24_3() throws Exception {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), testName + ".as", testName + "_2.as");

    doMultipleResolveTestForFile(buildResolveHandler("AAA", JSClass.class), testName + "_3.as");
    doMultipleResolveTestForFile(buildResolveHandler("BBB", JSClass.class), testName + "_4.as", testName + "_5.as");
    doMultipleResolveTestForFile(buildResolveHandler("foo.XXX", JSClass.class, false, true), testName + "_7.as");
    doMultipleResolveTestForFile(buildResolveHandler("foo2.ZZZ", JSClass.class, false, true), testName + "_8.as");
    doMultipleResolveTestForFile(buildResolveHandler("CCC", JSClass.class), testName + "_6.as");
    doMultipleResolveTestForFile(buildResolveHandler("foo.CCC", JSClass.class, false, true), testName + "_9.as");
  }

  //public void testResolve24_4() throws Exception  {
  //  final String testName = getTestName(false);
  //  doMultipleResolveTestForFile(buildResolveHandler("Object", JSClass.class), testName +".as");
  //}

  //public void testResolve24_5() throws Exception  {
  //  final String testName = getTestName(false);
  //  doMultipleResolveTestForFile(buildResolveHandler("Object", JSClass.class), testName +".as");
  //}

  public void testResolve24_6() throws Exception {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest", JSFunction.class), testName + ".as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest3", JSFunction.class), testName + "_4.as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest5", JSClass.class), testName + "_5.as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest2", JSFunction.class), testName + "_2.as", testName + "_3.as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest4", JSClass.class), testName + "_4_2.as");
  }

  public void testResolve24_7() throws Exception {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(createProcessor("BazBar"), testName + ".as");
    doMultipleResolveTestForFile(createProcessor("BazBar2"), testName + "_2.as", testName + "_3.as");
  }

  @JSTestOptions({JSTestOption.WithIndex})
  public void testResolve26() throws Exception {
    String fileText = "dynamic class Object {}\n" +
                      "package {\n" +
                      "\timport flash.display.Sprite;\n" +
                      "\tpublic class DoubleInterfaceResolve extends Sprite {\n" +
                      "\t}\n" +
                      "}\n" +
                      "import flash.display.Sprite;\n" +
                      "class FooView extends Sprite {\n" +
                      "}\n" +
                      "\n" +
                      "interface ItfA {\n" +
                      "    function f():It<ref>fB;\n" +
                      "}\n" +
                      "interface ItfB {}";
    PsiReference ref = configureByFileText(fileText, "sample.js2");

    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof JSClass);
  }

  public void testResolveVectorMember() throws Exception {
    String fileText = "package {\n" +
                      "public class Vector$object {\n" +
                      "    function reverse():Vector$object {}\n" +
                      "}\n" +
                      "}\n" +
                      "var x:Vector.<int>\n" +
                      "x.rev<ref>erse()";
    JSReferenceExpression ref = (JSReferenceExpression)configureByFileText(fileText, "sample.as");
    final PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
  }
}
