package com.intellij.flex.resolver;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSResolveFixture;
import com.intellij.lang.javascript.JSTestFixtures;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSLocalVariable;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSResolveFixture.buildResolveHandler;
import static com.intellij.lang.javascript.JSResolveFixture.getElement;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class ActionScriptResolveTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSResolveFixture> js = JSTestFixtures.jsFixture(
    "jsResolveFixture", projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("resolve/"), "as"),
    (fixture, testClass, testMethod, options) -> new JSResolveFixture(fixture, testClass, testMethod, options) {
      @Override
      public @NotNull String getSampleName() {
        return "__sample.as";
      }
    });

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

  @Test
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

    PsiPolyVariantReference ref = js.get().configureByFileText(fileText);
    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      ResolveResult[] results = ref.multiResolve(false);
      assertEquals(2, results.length);
    });
  }

  @Test
  public void testResolveIt() {
    String fileText = """
      package {

      public class Test {
        public function Test() {
          var describeType:XML = describe<caret>Type(Test);
        }
      }
      function describeType(x) {}""";

    PsiPolyVariantReference ref = js.get().configureByFileText(fileText);
    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      assertTrue(ref.resolve() instanceof JSLocalVariable);
    });
  }

  @Test
  public void testResolveInClass() {
    String fileText = """
      class A { function get aaa() {} function foo() {
        var bbb = a<caret>aa;
      }}""";

    PsiPolyVariantReference ref = js.get().configureByFileText(fileText);
    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      assertTrue(ref.resolve() instanceof JSFunction);
    });
  }

  @Test
  public void testObjectProperties() {
    String fileText = """
      package {
      public class Foo {
          function bar() {        var x:Object = {};
              x.toStri<caret>ng();
          }
      }
      }""";
    PsiPolyVariantReference ref = js.get().configureByFileText(fileText);
    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      final PsiElement resolved = ref.resolve();
      assertNotNull(resolved);
    });
  }

  @Test
  public void testResolveOfGetProperty() {
    String fileText = """
      class A { public function get foo() : String { return "foo"; } }
      function zzz() {
      var a : A;
      a.f<caret>oo}""";

    PsiReference ref = js.get().configureByFileText(fileText);
    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      PsiElement resolved = ref.resolve();
      assertTrue(resolved instanceof JSFunction);
    });
  }

  @Test
  public void testResolve23_4() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_5() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("C", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_6() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("C", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_7() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("E", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_8() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_9() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_10() {
    final String testName = js.get().getTestName(false);
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), testName + ".as", testName + "_2.as", testName + "_3.as");
  }

  @Test
  public void testResolve23_11() {
    js.get().doMultipleResolveTestForFile(resolveResults -> {
      checkResolvedToClass(resolveResults);
      return false;
    }, js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve23_12() {
    js.get().doMultipleResolveTestForFile(resolveResults -> {
      checkResolvedToClass(resolveResults);
      return false;
    }, js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve24() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve24_2() {
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), js.get().getTestName(false) + ".as");
  }

  @Test
  public void testResolve24_3() {
    final String testName = js.get().getTestName(false);
    js.get().doMultipleResolveTestForFile(buildResolveHandler("A", JSClass.class), testName + ".as", testName + "_2.as");

    js.get().doMultipleResolveTestForFile(buildResolveHandler("AAA", JSClass.class), testName + "_3.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("BBB", JSClass.class), testName + "_4.as", testName + "_5.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("foo.XXX", JSClass.class, false, true), testName + "_7.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("foo2.ZZZ", JSClass.class, false, true), testName + "_8.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("CCC", JSClass.class), testName + "_6.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("foo.CCC", JSClass.class, false, true), testName + "_9.as");
  }

  //public void testResolve24_4()  {
  //  final String testName = getTestName(false);
  //  doMultipleResolveTestForFile(buildResolveHandler("Object", JSClass.class), testName +".as");
  //}

  //public void testResolve24_5()  {
  //  final String testName = getTestName(false);
  //  doMultipleResolveTestForFile(buildResolveHandler("Object", JSClass.class), testName +".as");
  //}

  @Test
  public void testResolve24_6() {
    final String testName = js.get().getTestName(false);
    js.get().doMultipleResolveTestForFile(buildResolveHandler("ResolveTest", JSFunction.class), testName + ".as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("ResolveTest3", JSFunction.class), testName + "_4.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("ResolveTest5", JSClass.class), testName + "_5.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("ResolveTest2", JSFunction.class), testName + "_2.as", testName + "_3.as");
    js.get().doMultipleResolveTestForFile(buildResolveHandler("ResolveTest4", JSClass.class), testName + "_4_2.as");
  }

  @Test
  public void testResolve24_7() {
    final String testName = js.get().getTestName(false);
    js.get().doMultipleResolveTestForFile(createProcessor("BazBar"), testName + ".as");
    js.get().doMultipleResolveTestForFile(createProcessor("BazBar2"), testName + "_2.as", testName + "_3.as");
  }

  @Test
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
    PsiReference ref = js.get().configureByFileText(fileText);

    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      PsiElement resolved = ref.resolve();
      assertTrue(resolved instanceof JSClass);
    });
  }

  @Test
  public void testResolveVectorMember() {
    String fileText = """
      package {
      public class Vector$object {
          function reverse():Vector$object {}
      }
      }
      var x:Vector.<int>
      x.rev<caret>erse()""";
    PsiPolyVariantReference ref = js.get().configureByFileText(fileText);
    JSTestUtils.runInBackgroundReadActionAndWaitVoid(() -> {
      final PsiElement resolved = ref.resolve();
      assertNotNull(resolved);
    });
  }
}
