// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.intellij.terraform.hcl.psi.HCLExpression;
import org.intellij.terraform.hcl.psi.HCLObject;
import org.intellij.terraform.hcl.psi.HCLProperty;
import org.intellij.terraform.hcl.psi.HCLStringLiteral;
import org.intellij.terraform.config.TerraformFileType;
import org.intellij.terraform.hil.inspection.HILConvertToHCLInspection;

public class ConvertHILToHCLTest extends LightJavaCodeInsightFixtureTestCase {

  protected void doTest(String input, String expected) {
    PsiFile file = myFixture.configureByText(TerraformFileType.INSTANCE, "a=\"" + input + "\"");
    HCLProperty property = (HCLProperty) file.getFirstChild();
    assertNotNull(property);
    HCLStringLiteral literal = (HCLStringLiteral) property.getValue();
    assertNotNull(literal);
    HCLExpression value = HILConvertToHCLInspection.ConvertToHCLFix.getReplacementValue(getProject(), literal);
    assertEquals(expected, value.getText());
  }

  public void testSimpleSum() {
    doTest("${a+b}", "a+b");
  }

  public void testSelectNumberFromList() {
    doTest("${outer.list.0.inner}", "outer.list[0].inner");
  }

  public void testSelectSplatFromList() {
    doTest("${google_compute_instance.main.*.name}", "google_compute_instance.main.*.name");
  }

  public void testListIndexing() {
    doTest("${google_compute_instance.main.name[count.index]}", "google_compute_instance.main.name[count.index]");
  }

  public void testTemplate() {
    doTest("Hello, %{ if var.name != \"\"}${var.name}%{else}unnamed%{endif}!", "\"Hello, %{ if var.name != \"\"}${var.name}%{else}unnamed%{endif}!\"");
  }

  public void testNotTemplate() {
    doTest("Hello, %%{ if var.name != \"\"}${var.name}", "\"Hello, %%{ if var.name != \"\"}\" + var.name");
  }

  public void testPropertyKeyWithReference() {
    String input = "a = { \"${var.foo}\": \"bar\" }";
    PsiFile file = myFixture.configureByText(TerraformFileType.INSTANCE, input);
    HCLProperty p1 = (HCLProperty)file.getFirstChild();
    assertNotNull(p1);
    HCLObject object = (HCLObject)p1.getValue();
    assertNotNull(object);
    HCLProperty property = object.getPropertyList().iterator().next();
    assertNotNull(property);
    HCLStringLiteral literal = (HCLStringLiteral)property.getNameElement();
    assertNotNull(literal);
    HCLExpression value = HILConvertToHCLInspection.ConvertToHCLFix.getReplacementValue(getProject(), literal);
    assertEquals("(var.foo)", value.getText());
  }
}
