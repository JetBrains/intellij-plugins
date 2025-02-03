// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.structuralsearch;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ActionScriptStructuralSearchTest extends JSStructuralSearchTestBase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("structuralsearch/");
  }

  @Override
  protected void doTest(String source, String pattern, int expectedOccurrences, boolean wrapAsSourceWithFunction) {
    if (wrapAsSourceWithFunction) {
      source = "class A { function f() {" + source + "} }";
    }
    doActionScriptTest(source, pattern, expectedOccurrences);
  }

  private void doActionScriptTest(@Language("ECMA Script Level 4") String source, String pattern, int expectedOccurrences) {
    doTest(source, pattern, expectedOccurrences, ActionScriptFileType.INSTANCE);
  }

  public void testActionScriptVarInJSFile() {
    String s = """
      var x, y, z;
      var a;
      var b = 1;
      """;
    // should not be able to find ActionScript in regular JS file
    doTest(s, "var '_a+;", 0, ActionScriptFileType.INSTANCE, JavaScriptSupportLoader.JAVASCRIPT);
  }

  public void testAsInterface() {
    doActionScriptTest("interface A { function aba(); }", "aba", 1);
  }

  public void testMalformedPatterns() {
    doMalformedPatternTest("class var");
    doMalformedPatternTest("{");
    doMalformedPatternTest("(");
    doMalformedPatternTest("function f()");
    doMalformedPatternTest("""
                             $FunctionAssigment$ $FunctionEqual$ function() {
                                 $Statements$
                             }"""); // see IDEA-154183
  }

  private void doMalformedPatternTest(String pattern) {
    try {
      doActionScriptTest("{}", pattern, 0);
      fail();
    } catch (MalformedPatternException ignore) {}
  }

  public void testCustomAttributes() {
    final String source = """
      package {
      public class C {
      [Embed(source='pic.png')] public static const n: int;
      }
      }""";
    doActionScriptTest(source, "\\[Embed(source=\"pic.png\")]", 1);
    doActionScriptTest(source, "\\[Exbed(source=\"pic.png\")]", 0);
    doActionScriptTest(source, "\\[Embed(nope=\"pic.png\")]", 0);
    doActionScriptTest(source, "\\[Embed(source=\"bla\")]", 0);
    doActionScriptTest(source, "\\[Embed(source=\"$a$\")]", 1);
    doActionScriptTest(source, " [Embed(source=\"$a$\")]", 1);
  }

  public void testInMxml() throws IOException {
    doTestByFile("Script.mxml", "var $i$ = $val$", 2, ActionScriptFileType.INSTANCE);
    doTestByFile("Script.mxml", "for (var i = 0; i < n; i++) $exp$;", 1, ActionScriptFileType.INSTANCE);
    doTestByFile("Script.mxml", "for (var $i$ = 0; $i$ < n; $i$++) $exp$;", 2, ActionScriptFileType.INSTANCE);
    doTestByFile("Script.mxml", "$func$();", 1, ActionScriptFileType.INSTANCE);

    // todo: test AS in XML attribute values
  }

  public void testAsFunc() throws IOException {
    doTestByFile("class.as", "$a$+$b$", 1);
    doTestByFile("class.as", "function $name$('_param*) { '_st*; }", 2, ActionScriptFileType.INSTANCE);
    doTestByFile("class.as", "$a$+$b$", 1, ActionScriptFileType.INSTANCE);
    try {
      doTestByFile("class.as", "public static function sum('_param*) { '_st*; }", 0);
      fail();
    } catch (MalformedPatternException ignore) {}
    doTestByFile("class.as", "public static function sum('_param*) { '_st*; }", 1, ActionScriptFileType.INSTANCE);
    doTestByFile("class.as", "function sum('_param*) { '_st*; }", 1, ActionScriptFileType.INSTANCE);
    doTestByFile("class.as", "private static function sum('_param*) { '_st*; }", 0, ActionScriptFileType.INSTANCE);
  }

  public void testStringLiteralInActionScript() {
    doActionScriptTest("""
                         package {
                         public class MyClass {
                             private var s:String = "hello";
                         }
                         }""", "\"$str$\"", 1);
  }

  public void testClasses() {
    String pattern = "class $name$ {}";
    doActionScriptTest("""
                         package {
                         public class MyClass implements mx.messaging.messages.IMessage {
                         }
                         }""", pattern, 1);
    doActionScriptTest("""
                         package {
                         class MyClass implements mx.messaging.messages.IMessage {
                         }
                         }""", pattern, 1);

    String c = """
      package {
      public class MyAsClass extends SomeClass {
          function MyAsClass() {}
          function f() {
            var a = 1;    }
          function g() {
          }
      }
      }""";
    doActionScriptTest(c, "class $name$ { function g() {} }", 1);
    doActionScriptTest(c, "class $name$ { function f() { '_st*; } }", 1);
    doActionScriptTest(c, "class $name$ { function f() {} }", 0);
    doActionScriptTest(c, "class $name$ { function f() {var a = 1;} }", 1);
    doActionScriptTest(c, "class $name$ { function g() { '_st1*; } function f() { '_st2*; } }", 1);
    doActionScriptTest(c, "class $name$ { function $name$() { '_st*; } }", 1);
    doActionScriptTest(c, "'_statement;", 1);

    String c1 = """
      package {
      class C1 implements I1, I2 {}
      }""";
    doActionScriptTest(c1, "class $name$ implements $i1$, $i2$ {}", 1);
    doActionScriptTest(c1, "class $name$ implements $i1$, $i2$, $i3$ {}", 0);
    doActionScriptTest(c1, "class $name$ implements I2, I1 {}", 1);
    doActionScriptTest(c1, "class $name$ implements $i$ {}", 1);
  }

  public void testTypes() {
    String as = """
      class Foo {
          function m() {
          }
      }
      class Bar {
          function m() {
          }
      }
      class Baz extends Foo {

      }

      var x = new Foo();
      var y = new Bar();
      var z = new Baz();
      x.m();
      y.m();
      z.m();""";
    doActionScriptTest(as, "'_ref.m()", 3);
    doActionScriptTest(as, "'_ref:[exprtype( Foo )].m()", 1);
    doActionScriptTest(as, "'_ref:[exprtype( *Foo )].m()", 2);
  }
}
