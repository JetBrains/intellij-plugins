// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.testFramework.TestDataPath;
import org.intellij.terraform.hcl.HCLParserDefinition;
import org.intellij.terraform.config.TerraformParserDefinition;

import java.io.IOException;

@TestDataPath("$CONTENT_ROOT/test-data/psi/")
public class TerraformConfigParserTest extends HCLParserTest {
  public TerraformConfigParserTest() {
    super("psi", "hcl", false, new TerraformParserDefinition(), new HCLParserDefinition());
  }

  private void setTerraformExtension() {
    myFileExt = "tf";
  }

  public void testTerraform_With_String_In_IL() throws Exception {
    setTerraformExtension();
    doTest();
  }

  public void testTerraform_With_Extra_Quote() throws Exception {
    setTerraformExtension();
    doTest();
  }

  public void testUnfinished_Interpolation() throws Exception {
    setTerraformExtension();
    doTest();
  }

  public void testBackslash_Escaping_In_Interpolation() throws Exception {
    setTerraformExtension();
    doTest();
  }

  public void testMultiline_Interpolation() throws Exception {
    setTerraformExtension();
    doTest();
  }

  public void testClosingBraceInInterpolationStringLiteral() throws Exception {
    setTerraformExtension();
    doTest();
  }

  public void testEscapedQuotesInInterpolation() {
    setTerraformExtension();
    doTest();
  }

  public void testForArray() throws IOException {
    doCodeTest("a = [for k, v in foo: v if true]");
  }

  public void testForArray2() throws IOException {
    doCodeTest("""
                 cidr_blocks = [
                   for num in var.subnet_numbers:
                   cidrsubnet(data.aws_vpc.example.cidr_block, 8, num)
                 ]""");
  }

  public void testForObjectIf() throws IOException {
    doCodeTest("""
                 value = {
                   for instance in aws_instance.example:
                   instance.id => instance.public
                   if instance.associate_public_ip_address
                 }""");
  }

  public void testForObjectIf2() throws IOException {
    doCodeTest("""
                 value = {
                   for k in v:
                   k => k
                   if !a(b)
                 }""");
  }

  public void testForArrayIncomplete1() throws IOException {
    doCodeTest("a = [for ]");
  }

  public void testForArrayIncomplete2() throws IOException {
    doCodeTest("a = [for k, in foo: v]");
  }

  public void testForArrayIncomplete3() throws IOException {
    doCodeTest("a = [for a in ]");
  }

  public void testForObject() throws IOException {
    doCodeTest("""
                 value = {
                   for instance in aws_instance.example:
                   instance.id => instance.private_ip
                 }""");
  }

  public void testForObjectGrouping() throws IOException {
    doCodeTest("""
                 value = {
                   for instance in aws_instance.example:
                   instance.availability_zone => instance.id...
                 }""");
  }

  public void testSelectExpression() throws IOException {
    doCodeTest("a = foo.bar.baz");
  }

  public void testIndexSelectExpression() throws IOException {
    doCodeTest("a = foo[5].baz");
  }

  public void testSplatExpression() throws IOException {
    doCodeTest("a = foo.*.baz");
  }

  public void testFullSplatExpression() throws IOException {
    doCodeTest("a = foo[*].baz");
  }

  public void testComplexSplat() throws IOException {
    doCodeTest("a = tuple.*.foo.bar[0]");
  }

  public void testComplexFullSplat() throws IOException {
    doCodeTest("a = tuple[*].foo.bar[0]");
  }

  public void testSelectWithNumberExpression() throws IOException {
    doCodeTest("a = foo.0.baz");
  }

  public void testPropsInObject() throws IOException {
    doCodeTest("a = {a=1, b:2, c=3\nd=4\ne:null}");
  }

  public void testPropertyNameIsSelectExpression() throws IOException {
    doCodeTest("""
                 providers = {
                   aws.target = aws.iam
                 }""");
  }

  public void testPropertyNameIsParenthesizedSelectExpression() throws IOException {
    doCodeTest("""
                 providers = {
                   a="x"
                   (aws.target) = aws.iam
                 }""");
  }

  public void testPropertyNameIsQuotedSelectExpression() throws IOException {
    doCodeTest("""
                 providers = {
                   "aws.target" = aws.iam
                 }""");
  }

  public void testObjectAsMethodCallArgument() throws IOException {
    doCodeTest("x=foo({a = 1, b = [true, false]})");
  }

  public void testMethodCallEllipsis1() throws IOException {
    doCodeTest("x=foo(1...)");
  }

  public void testMethodCallEllipsis2() throws IOException {
    doCodeTest("x=foo(1, 2...)");
  }

  public void testMethodCallCommaEllipsis() throws IOException {
    doCodeTest("x=foo(1, 2,...)");
  }

  public void testMethodCallTrailingComma() throws IOException {
    doCodeTest("x=foo(1, 2,)");
  }
}
