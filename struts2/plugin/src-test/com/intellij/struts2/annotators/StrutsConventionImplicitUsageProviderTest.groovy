/*
 * Copyright 2013 The authors
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
package com.intellij.struts2.annotators

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.deadCode.UnusedDeclarationInspectionBase
import com.intellij.struts2.BasicLightHighlightingTestCase
import com.intellij.struts2.Struts2ProjectDescriptorBuilder
import com.intellij.struts2.StrutsConstants
import com.intellij.struts2.model.jam.convention.StrutsConventionConstants
import com.intellij.testFramework.LightProjectDescriptor
import org.jetbrains.annotations.NotNull

/**
 * @author Yann C&eacute;bron
 */
class StrutsConventionImplicitUsageProviderTest extends BasicLightHighlightingTestCase {

  private static final LightProjectDescriptor CONVENTION = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withLibrary("struts2-convention-plugin", "struts2-convention-plugin-" + STRUTS2_VERSION + ".jar");

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return ""
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return getTestName(false).contains("Convention") ? CONVENTION : super.getProjectDescriptor()
  }

  @Override
  protected InspectionProfileEntry[] getHighlightingInspections() {
    return [new UnusedDeclarationInspectionBase(true)]
  }

  void testUnusedPublicClass() {
    doTest("NotAction.java", """
      public class <warning descr="Class 'NotAction' is never used">NotAction</warning> {}
    """)
  }

  void testUnusedPublicMethod() {
    doTest("NotAction.java", """
      public class <warning descr="Class 'NotAction' is never used">NotAction</warning> {
        public void <warning descr="Method 'method()' is never used">method</warning>() {}
      }
    """)
  }

  void testConventionUnused() {
    doTest("Unused.java", """
      public class <warning descr="Class 'Unused' is never used">Unused</warning> {
        public void <warning descr="Method 'unusedMethod()' is never used">unusedMethod</warning>() {}
      }
    """)
  }

  void testConventionUnusedInterface() {
    doTest("Unused.java", """
      public interface <warning descr="Class 'Unused' is never used">Unused</warning> {
      }
    """)
  }

  void testConventionEndsWithAction() {
    doTest("EndsWithAction.java", """
      public class EndsWithAction {
        public void isActionClassMethod() {}
        protected void <warning descr="Method 'checkProtected()' is never used">checkProtected</warning>() {}
        private void <warning descr="Private method 'checkPrivate()' is never used">checkPrivate</warning>() {}
      }
    """)
  }

  void testConventionAnnotatedWithAction() {
    doTest("AnnotatedAction.java", """
      @$StrutsConventionConstants.ACTION
      public class AnnotatedAction {
        public String execute() throws Exception { return null; }
        public void isActionMethod() {}
      }
    """)
  }

  void testConventionAnnotatedWithActions() {
    doTest("AnnotatedAction.java", """
      @$StrutsConventionConstants.ACTIONS
      public class AnnotatedAction {
        public String execute() throws Exception { return null; }
        public void isActionMethod() {}
      }
    """)
  }

  void testConventionInheritsFromAction() {
    doTest("Inherit.java", """
      public class Inherit implements $StrutsConstants.XWORK_ACTION_CLASS {
        public String execute() throws Exception { return null; }
        public void isActionMethod() {}
      }
    """)
  }

  private void doTest(String classFqn, String classText) {
    myFixture.configureByText(classFqn, classText)
    myFixture.checkHighlighting()
  }
}
