package com.intellij.flex.resolver;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSResolveTestCase;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSLocalVariable;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
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

  public void testResolveInClassDynamic() {
    String fileText = """
      public class T {
        public function m() : String {
          return "m";
        }

        public function test() : String {
          function foo():String {
            return this.<caret>m();
          }
          return foo();
        }
      }
      function m() {}""";

    PsiPolyVariantReference ref = configureByFileText(fileText);
    ResolveResult[] results = ref.multiResolve(false);
    assertEquals(2, results.length);
  }

  public void testResolveIt() {
    String fileText = """
      package {

      public class Test {
        public function Test() {
          var describeType:XML = describe<caret>Type(Test);
        }
      }
      function describeType(x) {}""";

    PsiPolyVariantReference ref = configureByFileText(fileText);
    assertTrue(ref.resolve() instanceof JSLocalVariable);
  }

  public void testResolveInClass() {
    String fileText = """
      class A { function get aaa() {} function foo() {
        var bbb = a<caret>aa;
      }}""";

    PsiPolyVariantReference ref = configureByFileText(fileText);
    assertTrue(ref.resolve() instanceof JSFunction);
  }

  public void testObjectProperties() {
    String fileText = """
      package {
      public class Foo {
          function bar() {        var x:Object = {};
              x.toStri<caret>ng();
          }
      }
      }""";
    PsiPolyVariantReference ref = configureByFileText(fileText);
    final PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
  }

  public void testResolveOfGetProperty() {
    String fileText = """
      class A { public function get foo() : String { return "foo"; } }
      function zzz() {
      var a : A;
      a.f<caret>oo}""";

    PsiReference ref = configureByFileText(fileText);
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof JSFunction);
  }

  public void testResolve23_4() {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_5() {
    doMultipleResolveTestForFile(buildResolveHandler("C", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_6() {
    doMultipleResolveTestForFile(buildResolveHandler("C", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_7() {
    doMultipleResolveTestForFile(buildResolveHandler("E", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_8() {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_9() {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve23_10() {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), testName + ".as", testName + "_2.as", testName + "_3.as");
  }

  public void testResolve23_11() {
    doMultipleResolveTestForFile(resolveResults -> {
      checkResolvedToClass(resolveResults);
      return false;
    }, getTestName(false) + ".as");
  }

  public void testResolve23_12() {
    doMultipleResolveTestForFile(resolveResults -> {
      checkResolvedToClass(resolveResults);
      return false;
    }, getTestName(false) + ".as");
  }


  public void testResolve24() {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve24_2() {
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), getTestName(false) + ".as");
  }

  public void testResolve24_3() {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), testName + ".as", testName + "_2.as");

    doMultipleResolveTestForFile(buildResolveHandler("AAA", JSClass.class), testName + "_3.as");
    doMultipleResolveTestForFile(buildResolveHandler("BBB", JSClass.class), testName + "_4.as", testName + "_5.as");
    doMultipleResolveTestForFile(buildResolveHandler("foo.XXX", JSClass.class, false, true), testName + "_7.as");
    doMultipleResolveTestForFile(buildResolveHandler("foo2.ZZZ", JSClass.class, false, true), testName + "_8.as");
    doMultipleResolveTestForFile(buildResolveHandler("CCC", JSClass.class), testName + "_6.as");
    doMultipleResolveTestForFile(buildResolveHandler("foo.CCC", JSClass.class, false, true), testName + "_9.as");
  }

  //public void testResolve24_4()  {
  //  final String testName = getTestName(false);
  //  doMultipleResolveTestForFile(buildResolveHandler("Object", JSClass.class), testName +".as");
  //}

  //public void testResolve24_5()  {
  //  final String testName = getTestName(false);
  //  doMultipleResolveTestForFile(buildResolveHandler("Object", JSClass.class), testName +".as");
  //}

  public void testResolve24_6() {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest", JSFunction.class), testName + ".as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest3", JSFunction.class), testName + "_4.as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest5", JSClass.class), testName + "_5.as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest2", JSFunction.class), testName + "_2.as", testName + "_3.as");
    doMultipleResolveTestForFile(buildResolveHandler("ResolveTest4", JSClass.class), testName + "_4_2.as");
  }

  public void testResolve24_7() {
    final String testName = getTestName(false);
    doMultipleResolveTestForFile(createProcessor("BazBar"), testName + ".as");
    doMultipleResolveTestForFile(createProcessor("BazBar2"), testName + "_2.as", testName + "_3.as");
  }

  public void testResolve26() {
    String fileText = """
      dynamic class Object {}
      package {
      \timport flash.display.Sprite;
      \tpublic class DoubleInterfaceResolve extends Sprite {
      \t}
      }
      import flash.display.Sprite;
      class FooView extends Sprite {
      }

      interface ItfA {
          function f():It<caret>fB;
      }
      interface ItfB {}""";
    PsiReference ref = configureByFileText(fileText);

    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof JSClass);
  }

  public void testResolveVectorMember() {
    String fileText = """
      package {
      public class Vector$object {
          function reverse():Vector$object {}
      }
      }
      var x:Vector.<int>
      x.rev<caret>erse()""";
    PsiPolyVariantReference ref = configureByFileText(fileText);
    final PsiElement resolved = ref.resolve();
    assertNotNull(resolved);
  }
}
