/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
