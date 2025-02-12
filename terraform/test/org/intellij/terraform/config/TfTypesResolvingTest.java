// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.config.model.Type;
import org.intellij.terraform.hcl.psi.HCLExpression;
import org.intellij.terraform.hcl.psi.HCLProperty;
import org.intellij.terraform.hcl.psi.common.BaseExpression;
import org.intellij.terraform.hil.psi.TypeCachedValueProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TfTypesResolvingTest extends LightPlatformTestCase {

  public void testSimpleLiterals() throws Exception {
    doTypeResolveTest("1", "number");
    doTypeResolveTest("1.0", "number");
    doTypeResolveTest("true", "bool");
    doTypeResolveTest("false", "bool");
    doTypeResolveTest("\"\"", "string");
    doTypeResolveTest("\"string\"", "string");
    doTypeResolveTest("<<EOF\ntext\nEOF", "string");
  }

  public void testUnaryExpression() throws Exception {
    doTypeResolveTest("!true", "bool");
    doTypeResolveTest("!false", "bool");
    doTypeResolveTest("!!true", "bool");
    doTypeResolveTest("!!false", "bool");

    doTypeResolveTest("-1", "number");
    doTypeResolveTest("--1", "number");
    // Unary plus not allowed in HCL2
    //doTypeResolveTest("+1", "number");
    //doTypeResolveTest("++1", "number");
  }

  public void testBinaryLogicalExpression() throws Exception {
    doTypeResolveTest("true || false", "bool");
    doTypeResolveTest("true && true", "bool");

    // Unfinished
    doTypeResolveTest("true || ", "bool");
  }

  public void testBinaryArithmeticalExpression() throws Exception {
    doTypeResolveTest("1 + 1", "number");
    doTypeResolveTest("1 - 1", "number");
    doTypeResolveTest("5 * 2", "number");
    doTypeResolveTest("10 / 2", "number");
    doTypeResolveTest("10 % 5", "number");
    doTypeResolveTest("1 + 1.0", "number");
    doTypeResolveTest("1 - 1.0", "number");
    doTypeResolveTest("5 * 2.0", "number");
    doTypeResolveTest("10 / 2.0", "number");

    doTypeResolveTest("1 + 1 * 2", "number");

    // Unfinished
    doTypeResolveTest("1 + ", "number");
  }

  public void testBinaryNumericalCompareExpression() throws Exception {
    doTypeResolveTest("1 < 2", "bool");
    doTypeResolveTest("1 > 2", "bool");
    doTypeResolveTest("1 <= 2", "bool");
    doTypeResolveTest("1 >= 2", "bool");

    // Unfinished
    doTypeResolveTest("1 >= ", "bool");
  }

  public void testBinaryEqualsExpression() throws Exception {
    doTypeResolveTest("1 == 1", "bool");
    doTypeResolveTest("1 != 2", "bool");
    doTypeResolveTest("true == false", "bool");
    doTypeResolveTest("true != false", "bool");
    doTypeResolveTest("1 == \"1\"", "bool");
    doTypeResolveTest("1 != \"1\"", "bool");
    doTypeResolveTest("\"1\" == \"1\"", "bool");
    doTypeResolveTest("\"1\" != \"2\"", "bool");

    // Unfinished
    doTypeResolveTest("1 == ", "bool");
  }

  public void testConditionalExpression() throws Exception {
    doTypeResolveTest("true?true:false", "bool");
    doTypeResolveTest("true?1:2", "number");
    doTypeResolveTest("true?\"1\":\"2\"", "string");

    // Weird type conversion:
    doTypeResolveTest("true?1:\"2\"", "number"); // False converted to type of true
    doTypeResolveTest("true?\"1\":2", "number"); // True converted to type of false since true type is string

    // Unfinished
    doTypeResolveTest("true?:", null);
    doTypeResolveTest("true?1:", "number");
    doTypeResolveTest("true?:1", "number");
  }

  public void testParenthesesAroundSimpleExpressions() throws Exception {
    doTypeResolveTest("()", "any");
    doTypeResolveTest("(\"\")", "string");
    doTypeResolveTest("(1)", "number");
    doTypeResolveTest("(true)", "bool");
  }

  public void testParenthesesAroundComplexExpressions() throws Exception {
    doTypeResolveTest("(1 + 1)", "number");
    doTypeResolveTest("(1 == 1)", "bool");
    doTypeResolveTest("(true || false)", "bool");

    doTypeResolveTest("()", "any");
  }

  public void testListExpression() throws Exception {
    doTypeResolveTest("[]", "list");

    doTypeResolveTest("[42]", "list(number)");
    doTypeResolveTest("[\"text\"]", "list(string)");
    doTypeResolveTest("[true]", "list(bool)");

    doTypeResolveTest("[1, 2, 3]", "list(number)");
    doTypeResolveTest("[\"a\", \"b\"]", "list(string)");
    doTypeResolveTest("[true, false]", "list(bool)");

    doTypeResolveTest("[[]]", "list(list)");
    doTypeResolveTest("[[1]]", "list(list(number))");
    doTypeResolveTest("[[true]]", "list(list(bool))");

    doTypeResolveTest("[1, \"2\"]", "list(number)");
    doTypeResolveTest("[\"false\", true, \"true\"]", "list(bool)");
    doTypeResolveTest("[[true],[false]]", "list(list(bool))");
    doTypeResolveTest("[[true],[\"false\"]]", "list(list(bool))");
  }

  public void testObjectExpression() throws Exception {
    doTypeResolveTest("{}", "object");
    doTypeResolveTest("{a=1}", "object({a=number})");
  }

  public void testForExpressions() throws Exception {
    doTypeResolveTest("[for i in []: i]", "list(any)");
    doTypeResolveTest("{for i in []: i=>i}", "map(any)");
  }

  public void testSelectIndexExpression() {
    doTypeResolveTest("[true].0", "bool");
    doTypeResolveTest("[42].1", "number");
    doTypeResolveTest("[].2", "any");
    doTypeResolveTest("[true][0]", "bool");
    doTypeResolveTest("[42][1]", "number");
    doTypeResolveTest("[][2]", "any");

    // splat select, keeps list as is
    doTypeResolveTest("[true].*", "list(bool)");
    doTypeResolveTest("[42].*", "list(number)");
    doTypeResolveTest("[].*", "list");
    doTypeResolveTest("[true][*]", "list(bool)");
    doTypeResolveTest("[42][*]", "list(number)");
    doTypeResolveTest("[][*]", "list");

    // converts single element into list
    doTypeResolveTest("(true).*", "list(bool)");
    doTypeResolveTest("(42).*", "list(number)");
  }

  public void testTerraformFunctions() {
    // simple functions
    doTypeResolveTest("abs(-10)", "number");
    doTypeResolveTest("signum(-10)", "number");
  }

  public void testTerraformFunctionsSpecial() {
    doTypeResolveTest("totuple()", "tuple([])");
    doTypeResolveTest("totuple(1)", "tuple([number])");
    doTypeResolveTest("totuple(1, 2)", "tuple([number, number])");
    doTypeResolveTest("totuple(1, \"2\")", "tuple([number, string])");


    doTypeResolveTest("coalesce()", "invalid");
    doTypeResolveTest("coalesce(1)", "number");
    doTypeResolveTest("coalesce(1, 2)", "number");
    doTypeResolveTest("coalesce(\"\", 1)", "number");

    doTypeResolveTest("coalescelist()", "invalid");
    doTypeResolveTest("coalescelist([])", "list");
    doTypeResolveTest("coalescelist([1])", "list(number)");
    doTypeResolveTest("coalescelist([1], [2])", "list(number)");
//    doTypeResolveTest("coalescelist([], [1])", "list(number)");
//    doTypeResolveTest("coalescelist([\"x\"], [1])", "list(string)");

    doTypeResolveTest("concat()", "invalid");
    doTypeResolveTest("concat([])", "list");
    doTypeResolveTest("concat([1])", "list(number)");
    doTypeResolveTest("concat([1], [2])", "list(number)");
    doTypeResolveTest("concat([1], [\"2\"])", "list(number)");


    doTypeResolveTest("element()", "any");
    doTypeResolveTest("element([1], 0)", "number");
    doTypeResolveTest("element([1], 1)", "number");
    doTypeResolveTest("element(totuple(1), 1)", "number");
    doTypeResolveTest("element(totuple(1, 2), 1)", "number");
    doTypeResolveTest("element(totuple(\"1\", 2), 1)", "number");
//    doTypeResolveTest("element(totuple(\"1\", 2), 0)", "string");

//    doTypeResolveTest("", "");
  }

  private void doTypeResolveTest(@NotNull String text, @Nullable final String expected) {
    text = "x=" + text;
    final PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText("a.tf", TerraformFileType.INSTANCE, text);
    assertEquals(TerraformLanguage.INSTANCE, psiFile.getLanguage());
    PsiElement root = psiFile.getFirstChild();
    assertNotNull(root);
    assertInstanceOf(root, HCLProperty.class);
    HCLExpression value = ((HCLProperty) root).getValue();
    assertNotNull(value);
    assertInstanceOf(value, BaseExpression.class);
    Type type = TypeCachedValueProvider.Companion.getType(value);
    if (expected == null) {
      assertNull(type);
    } else {
      assertNotNull("Expected: " + expected + ", got null", type);
      assertEquals(expected, type.getPresentableText());
    }
  }
}
