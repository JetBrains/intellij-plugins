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

import com.intellij.psi.PsiPackage;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.jam.convention.JamParentPackage}.
 *
 * @author Yann C&eacute;bron
 */
public class JamParentPackageTest extends JamConventionTestBase<JavaModuleFixtureBuilder> {

  @NotNull
  @Override
  protected String getTestDataFolder() {
    return "parentPackage";
  }

  @SkipStrutsLibrary
  public void testCompletionAction() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("/src/testcompletion/Action.java",
                                     "myPackage", "myPackage2");
  }

  @SkipStrutsLibrary
  public void testCompletionPackageInfo() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("/src/testcompletion/package-info.java",
                                     "myPackage", "myPackage2");
  }

  @SkipStrutsLibrary
  public void testResolveAction() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    final JamParentPackage jamParentPackage = getClassJam("jam.Action", JamParentPackage.META_CLASS);
    checkResolve(jamParentPackage);
  }

  @SkipStrutsLibrary
  public void testResolveJamPackageInfo() throws Exception {
    createStrutsFileSet(STRUTS_XML);

    myFixture.configureByFile("/src/jam/package-info.java");
    final PsiPackage myPackage = myFixture.findPackage("jam");

    final JamParentPackage jamElement = JamParentPackage.META_PACKAGE.getJamElement(myPackage);
    assertNotNull(jamElement);
    checkResolve(jamElement);
  }

  private void checkResolve(@NotNull final JamParentPackage jamParentPackage) {
    final StrutsPackage strutsPackage = jamParentPackage.getValue().getValue();
    assertNotNull(strutsPackage);
    assertEquals("myPackage", strutsPackage.getName().getStringValue());
  }

}