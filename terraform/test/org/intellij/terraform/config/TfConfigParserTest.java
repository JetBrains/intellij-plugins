// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.lang.ParserDefinition;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.TestApplication;
import org.intellij.terraform.hcl.HCLParserDefinition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

@TestApplication
@TestDataPath("$CONTENT_ROOT/../test-data/psi/")
public class TfConfigParserTest extends HCLParserTest {
  @Override
  protected ParserDefinition @NotNull [] createParserDefinitions() {
    return new ParserDefinition[]{new TfParserDefinition(), new HCLParserDefinition()};
  }

  private void setTerraformExtension() {
    parsingFixture.get().setFileExt("tf");
  }

  @Test
  public void testTerraform_With_String_In_IL() throws Exception {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testTerraform_With_Extra_Quote() throws Exception {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testUnfinished_Interpolation() throws Exception {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testBackslash_Escaping_In_Interpolation() throws Exception {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testMultiline_Interpolation() throws Exception {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testClosingBraceInInterpolationStringLiteral() throws Exception {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testEscapedQuotesInInterpolation() {
    setTerraformExtension();
    doTest();
  }

  @Test
  public void testForArray() {
    doCodeTest("a = [for k, v in foo: v if true]");
  }

  @Test
  public void testForArray2() {
    doCodeTest("""
                 cidr_blocks = [
                   for num in var.subnet_numbers:
                   cidrsubnet(data.aws_vpc.example.cidr_block, 8, num)
                 ]""");
  }

  @Test
  public void testForObjectIf() {
    doCodeTest("""
                 value = {
                   for instance in aws_instance.example:
                   instance.id => instance.public
                   if instance.associate_public_ip_address
                 }""");
  }

  @Test
  public void testForObjectIf2() {
    doCodeTest("""
                 value = {
                   for k in v:
                   k => k
                   if !a(b)
                 }""");
  }

  @Test
  public void testForArrayIncomplete1() {
    doCodeTest("a = [for ]");
  }

  @Test
  public void testForArrayIncomplete2() {
    doCodeTest("a = [for k, in foo: v]");
  }

  @Test
  public void testForArrayIncomplete3() {
    doCodeTest("a = [for a in ]");
  }

  @Test
  public void testForObject() {
    doCodeTest("""
                 value = {
                   for instance in aws_instance.example:
                   instance.id => instance.private_ip
                 }""");
  }

  @Test
  public void testForObjectGrouping() {
    doCodeTest("""
                 value = {
                   for instance in aws_instance.example:
                   instance.availability_zone => instance.id...
                 }""");
  }

  @Test
  public void testSelectExpression() {
    doCodeTest("a = foo.bar.baz");
  }

  @Test
  public void testIndexSelectExpression() {
    doCodeTest("a = foo[5].baz");
  }

  @Test
  public void testSplatExpression() {
    doCodeTest("a = foo.*.baz");
  }

  @Test
  public void testFullSplatExpression() {
    doCodeTest("a = foo[*].baz");
  }

  @Test
  public void testComplexSplat() {
    doCodeTest("a = tuple.*.foo.bar[0]");
  }

  @Test
  public void testComplexFullSplat() {
    doCodeTest("a = tuple[*].foo.bar[0]");
  }

  @Test
  public void testSelectWithNumberExpression() {
    doCodeTest("a = foo.0.baz");
  }

  @Test
  public void testPropsInObject() {
    doCodeTest("a = {a=1, b:2, c=3\nd=4\ne:null}");
  }

  @Test
  public void testPropertyNameIsSelectExpression() {
    doCodeTest("""
                 providers = {
                   aws.target = aws.iam
                 }""");
  }

  @Test
  public void testPropertyNameIsParenthesizedSelectExpression() {
    doCodeTest("""
                 providers = {
                   a="x"
                   (aws.target) = aws.iam
                 }""");
  }

  @Test
  public void testPropertyNameIsQuotedSelectExpression() {
    doCodeTest("""
                 providers = {
                   "aws.target" = aws.iam
                 }""");
  }

  @Test
  public void testObjectAsMethodCallArgument() {
    doCodeTest("x=foo({a = 1, b = [true, false]})");
  }

  @Test
  public void testMethodCallEllipsis1() {
    doCodeTest("x=foo(1...)");
  }

  @Test
  public void testMethodCallEllipsis2() {
    doCodeTest("x=foo(1, 2...)");
  }

  @Test
  public void testMethodCallCommaEllipsis() {
    doCodeTest("x=foo(1, 2,...)");
  }

  @Test
  public void testMethodCallTrailingComma() {
    doCodeTest("x=foo(1, 2,)");
  }
}
