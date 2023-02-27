/*
 * Copyright 2000-2018 JetBrains s.r.o.
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
package org.intellij.terraform.config;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.terraform.hcl.HCLFileType;
import org.intellij.terraform.hcl.formatter.HCLCodeStyleSettings;
import org.intellij.terraform.config.TerraformFileType;
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
        {HCLFileType.INSTANCE}, {TerraformFileType.INSTANCE}
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
  public void testBasicFormatting() throws Exception {
    doSimpleTest("a=1", "a = 1");
    doSimpleTest("'a'=1", "'a' = 1");
  }

  @Test
  public void testFormatBlock() throws Exception {
    doSimpleTest("block x{}", "block x {}");
    doSimpleTest("block 'x'{}", "block 'x' {}");
    doSimpleTest("block 'x'{\n}", "block 'x' {\n}");

    doSimpleTest("block x{a=true}", "block x { a = true }");
    doSimpleTest("block x{\na=true\n}", "block x {\n  a = true\n}");
    doSimpleTest("block x{\na=true}", "block x {\n  a = true\n}");
  }

  @Test
  public void testAlignPropertiesOnEquals() throws Exception {
    CodeStyle.getSettings(getProject()).getCustomSettings(HCLCodeStyleSettings.class).PROPERTY_ALIGNMENT = HCLCodeStyleSettings.ALIGN_PROPERTY_ON_EQUALS;
    doSimpleTest("a=true\nbaz=42", "a   = true\nbaz = 42");
    doSimpleTest("a = true\nbaz=42", "a   = true\nbaz = 42");
    doSimpleTest("a = true\nbaz = 42", "a   = true\nbaz = 42");
    doSimpleTest("a=true\nbaz = 42", "a   = true\nbaz = 42");
  }

  @Test
  public void testAlignPropertiesOnValue() throws Exception {
    CodeStyle.getSettings(getProject()).getCustomSettings(HCLCodeStyleSettings.class).PROPERTY_ALIGNMENT = HCLCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE;
    doSimpleTest("a=true\nbaz=42", "a =   true\nbaz = 42");
    doSimpleTest("a = true\nbaz=42", "a =   true\nbaz = 42");
    doSimpleTest("a = true\nbaz = 42", "a =   true\nbaz = 42");
    doSimpleTest("a=true\nbaz = 42", "a =   true\nbaz = 42");
  }

  @Test
  public void testFormatHeredoc() throws Exception {
    doSimpleTest("a=<<E\nE", "a = <<E\nE");
    doSimpleTest("a=<<E\n  E", "a = <<E\n  E");
    doSimpleTest("a=<<E\n\tE", "a = <<E\n\tE");
    doSimpleTest("a=<<E\n inner\nE", "a = <<E\n inner\nE");
    doSimpleTest("a=<<E\n inner\n  E", "a = <<E\n inner\n  E");
    doSimpleTest("a=<<E\n inner\n\tE", "a = <<E\n inner\n\tE");
  }

  @Test
  public void testFormatAfterHeredoc() throws Exception {
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
  public void testAlignPropertiesOnValueAndSplitByBlocks() throws Exception {
    CodeStyle.getSettings(getProject()).getCustomSettings(HCLCodeStyleSettings.class).PROPERTY_ALIGNMENT = HCLCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE;
    doSimpleTest("a=true\n\n\nbaz=42", "a = true\n\n\nbaz = 42");
  }

  @Test
  public void testArrays() throws Exception {
    doSimpleTest("a=[]", "a = []");
    doSimpleTest("a=[1,2]", "a = [1, 2]");
    doSimpleTest("a    =    [    1    ,    2    ]", "a = [1, 2]");
  }

  @Test
  public void testObjects() throws Exception {
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
  public void testValuesInParentheses() throws Exception {
    doSimpleTest("a=x(\nb,1\n)", "a = x(\n  b, 1\n)");
    doSimpleTest("a    =    x    (\n    b    ,    1\n)", "a = x(\n  b, 1\n)");

    doSimpleTest("block {\n   a=x(\nb,1\n  )\n}", "block {\n  a = x(\n    b, 1\n  )\n}");

    doSimpleTest("a=x(\nb,\n1\n)", "a = x(\n  b,\n  1\n)");
  }

  @Test
  public void testMultilineObjectsAsValue() throws Exception {
    doSimpleTest("obj = {\nx = 1\n}\ny   = 2", "obj = {\n  x = 1\n}\ny = 2");
    doSimpleTest("test = <<EOF\n\nEOF\nb       = true", "test = <<EOF\n\nEOF\nb    = true");
  }

  @Test
  public void testNoSpacesAroundSelectExpression() throws Exception {
    doSimpleTest("x = a. b .c", "x = a.b.c");
  }

  @Test
  public void testNoSpacesBetweenMethodNameAndParen() throws Exception {
    doSimpleTest("x = foo ()", "x = foo()");
  }

  @Test
  @Ignore
  public void testNoSpaceBeforeEllipsis() throws Exception {
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
  public void testNoSpaceBeforeComma() throws Exception {
    doSimpleTest("x  =  foo  (a   ,   b)", "x = foo(a, b)");
  }

  @Test
  public void testNoSpaceAfterNot() throws Exception {
    doSimpleTest("x  =  ! true", "x = !true");
  }

  @Test
  public void testSpacesAroundBraces() throws Exception {
    doSimpleTest("foo={bar = baz}", "foo = { bar = baz }");
    doSimpleTest("foo{bar = baz}", "foo { bar = baz }");
    doSimpleTest("foo={   }", "foo = {}");
    doSimpleTest("foo={}", "foo = {}");
    doSimpleTest("foo{}", "foo {}");
    doSimpleTest("foo{    }", "foo {}");
  }


  @SuppressWarnings("WeakerAccess")
  public void doSimpleTest(String input, String expected) throws Exception {
    doSimpleTest(input, expected, null);
  }

  @SuppressWarnings("WeakerAccess")
  public void doSimpleTest(String input, String expected, @Nullable Runnable setupSettings) throws Exception {
    myFixture.configureByText(myFileType, input);
    final Project project = getProject();
    final PsiFile file = myFixture.getFile();
    if (setupSettings != null) setupSettings.run();
    CodeStyleManager.getInstance(project).reformatText(file, Collections.singleton(file.getTextRange()));
    myFixture.checkResult(expected);
  }
}
