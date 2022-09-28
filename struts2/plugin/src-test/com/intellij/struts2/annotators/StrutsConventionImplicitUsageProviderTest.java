/*
 * Copyright 2014 The authors
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
package com.intellij.struts2.annotators;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.deadCode.UnusedDeclarationInspectionBase;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.struts2.Struts2ProjectDescriptorBuilder;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.model.jam.convention.StrutsConventionConstants;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConventionImplicitUsageProviderTest extends BasicLightHighlightingTestCase {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return getTestName(false).contains("Convention") ? CONVENTION : super.getProjectDescriptor();
  }

  @Override
  protected InspectionProfileEntry[] getHighlightingInspections() {
    return new InspectionProfileEntry[]{new UnusedDeclarationInspectionBase(true)};
  }

  public void testUnusedPublicClass() {
    doTest("NotAction.java",
           "public class <warning descr=\"Class 'NotAction' is never used\">NotAction</warning> {}");
  }

  public void testUnusedPublicMethod() {
    doTest("NotAction.java",
           "public class <warning descr=\"Class 'NotAction' is never used\">NotAction</warning>{\n" +
           "  public void <warning descr=\"Method 'method()' is never used\">method</warning>() {}" +
           "}");
  }

  public void testConventionUnused() {
    doTest("Unused.java",
           "public class <warning descr=\"Class 'Unused' is never used\">Unused</warning> {\n" +
           " public void <warning descr=\"Method 'unusedMethod()' is never used\">unusedMethod</warning>() {}" +
           "}");
  }

  public void testConventionUnusedInterface() {
    doTest("Unused.java",
           "public interface <warning descr=\"Interface 'Unused' is never used\">Unused</warning> {}");
  }

  public void testConventionEndsWithAction() {
    doTest("EndsWithAction.java",
           """
             public class EndsWithAction {  public void isActionClassMethod() {}
               protected void <warning descr="Method 'checkProtected()' is never used">checkProtected</warning>() {}
               private void <warning descr="Private method 'checkPrivate()' is never used">checkPrivate</warning>() {}
             }""");
  }

  public void testConventionAnnotatedWithAction() {
    doTest("AnnotatedAction.java",
           "@" + StrutsConventionConstants.ACTION + "\n" +
           "public class AnnotatedAction {\n" +
           "  public String execute() throws Exception { return null; }\n" +
           "  public void isActionMethod() {}\n" +
           "}");
  }

  public void testConventionAnnotatedWithActions() {
    doTest("AnnotatedAction.java",
           "@" + StrutsConventionConstants.ACTIONS + "\n" +
           "public class AnnotatedAction {\n" +
           "  public String execute() throws Exception { return null; }\n" +
           "  public void isActionMethod() {}\n" +
           "}");
  }

  public void testConventionInheritsFromAction() {
    doTest("Inherit.java",
           "public class Inherit implements " + StrutsConstants.XWORK_ACTION_CLASS + " {\n" +
           "  public String execute() throws Exception { return null; }\n" +
           "  public void isActionMethod() {}\n" +
           "}");
  }

  private void doTest(String classFqn, String classText) {
    myFixture.configureByText(classFqn, classText);
    myFixture.checkHighlighting();
  }

  private static final LightProjectDescriptor CONVENTION = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withStrutsConvention();
}
