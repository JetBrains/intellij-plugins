// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.terraform.hcl.HclFileType;
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings;
import org.intellij.terraform.hcl.formatter.PropertyAlignment;
import org.jetbrains.annotations.Nullable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
public class HCLFormatterTest extends BasePlatformTestCase {

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {HclFileType.INSTANCE}, {TerraformFileType.INSTANCE}
    });
  }

  @Parameterized.Parameter
  public LanguageFileType myFileType = TerraformFileType.INSTANCE;

  @Override
  protected boolean isWriteActionRequired() {
    return true;
  }

  @Override
  protected String getTestDataPath() {
    return "tests/data";
  }

  @Test
  public void testBasicFormatting() {
    doSimpleTest("a=1", "a = 1");
    doSimpleTest("'a'=1", "'a' = 1");
  }

  @Test
  public void testFormatBlock() {
    doSimpleTest("block x{}", "block x {}");
    doSimpleTest("block 'x'{}", "block 'x' {}");
    doSimpleTest("block 'x'{\n}", "block 'x' {\n}");

    doSimpleTest("block x{a=true}", "block x { a = true }");
    doSimpleTest("block x{\na=true\n}", "block x {\n  a = true\n}");
    doSimpleTest("block x{\na=true}", "block x {\n  a = true\n}");
  }

  @Test
  public void testAlignPropertiesOnEquals() {
    CodeStyle.getSettings(getProject()).getCustomSettings(HclCodeStyleSettings.class).PROPERTY_ALIGNMENT = PropertyAlignment.ON_EQUALS;
    doSimpleTest("a=true\nbaz=42", "a   = true\nbaz = 42");
    doSimpleTest("a = true\nbaz=42", "a   = true\nbaz = 42");
    doSimpleTest("a = true\nbaz = 42", "a   = true\nbaz = 42");
    doSimpleTest("a=true\nbaz = 42", "a   = true\nbaz = 42");

    doSimpleTest(
      """
        resource "kafka_topic" "blah" {
          provider=kafka.some_provider
          name="blah"
          replication_factor=3
          partitions=1
          config={
            "retention.ms"=100000
            "retention.bytes"=-1
          }
        }""",
      """
        resource "kafka_topic" "blah" {
          provider           = kafka.some_provider
          name               = "blah"
          replication_factor = 3
          partitions         = 1
          config = {
            "retention.ms"    = 100000
            "retention.bytes" = -1
          }
        }""");

    doSimpleTest(
      """
        resource "azurerm_role_assignment" "test" {
          # some docs for principal id
          principal_id="test-principal-id"
          # some docs for scope
          scope="test-scope"
          nested_object {
            # doc
            property_1="val1"
            # doc
            prop_2=val2
            another_long_property=val3
          }
        }""",
      """
        resource "azurerm_role_assignment" "test" {
          # some docs for principal id
          principal_id = "test-principal-id"
          # some docs for scope
          scope = "test-scope"
          nested_object {
            # doc
            property_1 = "val1"
            # doc
            prop_2                = val2
            another_long_property = val3
          }
        }""");

    doSimpleTest(
      """
        locals {
          a = true
          b = false
          c = (
          a ?
          "foo" :
          (
          b ?
          "bar" :
          "baz"
          )
          )
        }
        """,
      """
        locals {
          a = true
          b = false
          c = (
            a ?
            "foo" :
            (
              b ?
              "bar" :
              "baz"
            )
          )
        }
        """);

    doSimpleTest(
      """
        module "a_module" {
          source        = "./modules/a_module"
          some_variable = "foo"
          another_variable = {
            "foo" : "bar"
          }
          another_var     = "bar"
          some_mapped_var = tomap({
            "a" = "123"
            "b" = "456"
          })
          still_another_var = "baz"
          some_list         = [
            "a",
            "b",
            "c"
          ]
        }
        """,
      """
        module "a_module" {
          source        = "./modules/a_module"
          some_variable = "foo"
          another_variable = {
            "foo" : "bar"
          }
          another_var = "bar"
          some_mapped_var = tomap({
            "a" = "123"
            "b" = "456"
          })
          still_another_var = "baz"
          some_list = [
            "a",
            "b",
            "c"
          ]
        }
        """);
  }

  @Test
  public void testAlignPropertiesOnValue() {
    CodeStyle.getSettings(getProject()).getCustomSettings(HclCodeStyleSettings.class).PROPERTY_ALIGNMENT = PropertyAlignment.ON_VALUE;
    doSimpleTest("a=true\nbaz=42", "a =   true\nbaz = 42");
    doSimpleTest("a = true\nbaz=42", "a =   true\nbaz = 42");
    doSimpleTest("a = true\nbaz = 42", "a =   true\nbaz = 42");
    doSimpleTest("a=true\nbaz = 42", "a =   true\nbaz = 42");
  }

  @Test
  public void testFormatHeredoc() {
    doSimpleTest("a=<<E\nE", "a = <<E\nE");
    doSimpleTest("a=<<E\n  E", "a = <<E\n  E");
    doSimpleTest("a=<<E\n\tE", "a = <<E\n\tE");
    doSimpleTest("a=<<E\n inner\nE", "a = <<E\n inner\nE");
    doSimpleTest("a=<<E\n inner\n  E", "a = <<E\n inner\n  E");
    doSimpleTest("a=<<E\n inner\n\tE", "a = <<E\n inner\n\tE");
  }

  @Test
  public void testFormatAfterHeredoc() {
    doSimpleTest("""
                   a_local = [
                     <<DATA
                   This is some data string
                   DATA
                   ,
                     "some other data",
                   ]""", """
                   a_local = [
                     <<DATA
                   This is some data string
                   DATA
                   ,
                     "some other data",
                   ]""");
    doSimpleTest("""
                   a_local = [
                     <<DATA
                   This is some data string
                   DATA
                   ,  "some other data",
                   ]""", """
                   a_local = [
                     <<DATA
                   This is some data string
                   DATA
                   , "some other data",
                   ]""");
  }

  @Test
  public void testAlignPropertiesOnValueAndSplitByBlocks() {
    CodeStyle.getSettings(getProject()).getCustomSettings(HclCodeStyleSettings.class).PROPERTY_ALIGNMENT = PropertyAlignment.ON_VALUE;
    doSimpleTest("a=true\n\n\nbaz=42", "a = true\n\n\nbaz = 42");
  }

  @Test
  public void testArrays() {
    doSimpleTest("a=[]", "a = []");
    doSimpleTest("a=[1,2]", "a = [1, 2]");
    doSimpleTest("a    =    [    1    ,    2    ]", "a = [1, 2]");
  }

  @Test
  public void testObjects() {
    doSimpleTest("a={}", "a = {}");
    doSimpleTest("a    =    {    }", "a = {}");

    doSimpleTest("a={x:1}", "a = { x : 1 }");
    doSimpleTest("a    =    {    x    :    1    }", "a = { x : 1 }");
    doSimpleTest("a={x=1}", "a = { x = 1 }");
    doSimpleTest("a    =    {    x    =    1    }", "a = { x = 1 }");

    doSimpleTest("a={x:1,y:2}", "a = { x : 1, y : 2 }");
    doSimpleTest("a    =    {    x    :    1    ,    y    :    2    }", "a = { x : 1, y : 2 }");
    doSimpleTest("a={x=1,y=2}", "a = { x = 1, y = 2 }");
    doSimpleTest("a    =    {    x    =    1    ,    y    =    2    }", "a = { x = 1, y = 2 }");
  }

  @Test
  public void testValuesInParentheses() {
    doSimpleTest("a=x(\nb,1\n)", "a = x(\n  b, 1\n)");
    doSimpleTest("a    =    x    (\n    b    ,    1\n)", "a = x(\n  b, 1\n)");

    doSimpleTest("block {\n   a=x(\nb,1\n  )\n}", "block {\n  a = x(\n    b, 1\n  )\n}");

    doSimpleTest("a=x(\nb,\n1\n)", "a = x(\n  b,\n  1\n)");
  }

  @Test
  public void testMultilineObjectsAsValue() {
    doSimpleTest("obj = {\nx = 1\n}\ny   = 2", "obj = {\n  x = 1\n}\ny = 2");
    doSimpleTest("test = <<EOF\n\nEOF\nb       = true", "test = <<EOF\n\nEOF\nb    = true");
  }

  @Test
  public void testNoSpacesAroundSelectExpression() {
    doSimpleTest("x = a. b .c", "x = a.b.c");
  }

  @Test
  public void testNoSpacesBetweenMethodNameAndParen() {
    doSimpleTest("x = foo ()", "x = foo()");
  }

  @Test
  @Ignore
  public void testNoSpaceBeforeEllipsis() {
    // TODO: Requires remapping 'for' and 'in' as keywords, not identifiers
    doSimpleTest("a =  { for  i  in  var.x : i => i   ...}", "a = { for i in var.x : i => i... }");
    doSimpleTest("""
                   setting = {
                     for k, v in local.input :
                     dirname(k) => {   basename(k)  =  merge(  v  ...  ) }   ...
                   }""",
                 """
                   setting = {
                     for k, v in local.input :
                     dirname(k) => { basename(k) = merge(v...) }...
                   }""");
  }

  @Test
  public void testNoSpaceBeforeComma() {
    doSimpleTest("x  =  foo  (a   ,   b)", "x = foo(a, b)");
  }

  @Test
  public void testNoSpaceAfterNot() {
    doSimpleTest("x  =  ! true", "x = !true");
  }

  @Test
  public void testSpacesAroundBraces() {
    doSimpleTest("foo={bar = baz}", "foo = { bar = baz }");
    doSimpleTest("foo{bar = baz}", "foo { bar = baz }");
    doSimpleTest("foo={   }", "foo = {}");
    doSimpleTest("foo={}", "foo = {}");
    doSimpleTest("foo{}", "foo {}");
    doSimpleTest("foo{    }", "foo {}");
  }


  @SuppressWarnings("WeakerAccess")
  public void doSimpleTest(String input, String expected) {
    doSimpleTest(input, expected, null);
  }

  @SuppressWarnings("WeakerAccess")
  public void doSimpleTest(String input, String expected, @Nullable Runnable setupSettings) {
    myFixture.configureByText(myFileType, input);
    final Project project = getProject();
    final PsiFile file = myFixture.getFile();
    if (setupSettings != null) setupSettings.run();
    CodeStyleManager.getInstance(project).reformatText(file, Collections.singleton(file.getTextRange()));
    myFixture.checkResult(expected);
  }
}
