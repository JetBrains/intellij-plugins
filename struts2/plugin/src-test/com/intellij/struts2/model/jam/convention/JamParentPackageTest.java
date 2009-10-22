/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.model.jam.convention;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.jam.convention.JamParentPackage}.
 *
 * @author Yann C&eacute;bron
 */
public class JamParentPackageTest extends BasicHighlightingTestCase<JavaModuleFixtureBuilder> {

  @NotNull
  protected String getTestDataLocation() {
    return "model/jam/convention/parentPackage";
  }

  @Override
  protected boolean hasJavaSources() {
    return true;
  }

  @Override
  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addLibrary(moduleBuilder, "struts2-convention-plugin", "struts2-convention-plugin-2.1.8.jar");
  }

  public void testCompletionAction() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("/src/testcompletion/Action.java",
                                     "myPackage", "myPackage2");
  }

/* TODO not working yet
  public void testCompletionPackageInfo() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("/src/testcompletion/package-info.java",
                                     "myPackage", "myPackage2");
  }
*/

  public void testJamPackageInfo() throws Exception {
    createStrutsFileSet(STRUTS_XML);

    myFixture.configureByFile("/src/jam/package-info.java");
    final PsiPackage myPackage = JavaPsiFacade.getInstance(myProject).findPackage("jam");
    assertNotNull(myPackage);

    final JamParentPackage jamElement = JamParentPackage.META_PACKAGE.getJamElement(myPackage);
    assertNotNull(jamElement);
    assertTrue(jamElement.getPsiElement() instanceof PsiPackage);
  }

}