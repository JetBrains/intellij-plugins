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
package com.intellij.struts2.model.jam.convention;

import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.jam.convention.JamParentPackage}.
 *
 * @author Yann C&eacute;bron
 */
public class JamParentPackageTest extends JamConventionLightTestCase {
  @NotNull
  @Override
  protected String getTestDataFolder() {
    return "parentPackage";
  }

  public void testCompletionAction() {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("testcompletion/Action.java",
                                     "myPackage", "myPackage2");
  }

  public void testCompletionPackageInfo() {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("testcompletion/package-info.java",
                                     "myPackage", "myPackage2");
  }

  public void testResolveAction() {
    createStrutsFileSet(STRUTS_XML);
    myFixture.copyFileToProject("jam/Action.java");

    final JamParentPackage jamParentPackage = getClassJam("jam.Action", JamParentPackage.META_CLASS);
    checkResolve(jamParentPackage);
  }

  public void testResolveJamPackageInfo() {
    createStrutsFileSet(STRUTS_XML);

    myFixture.copyFileToProject("jam/package-info.java");
    final JamParentPackage jamElement = getPackageJam("jam", JamParentPackage.META_PACKAGE);
    checkResolve(jamElement);
  }

  private static void checkResolve(@NotNull final JamParentPackage jamParentPackage) {
    final StrutsPackage strutsPackage = jamParentPackage.getValue().getValue();
    assertNotNull(strutsPackage);
    assertEquals("myPackage", strutsPackage.getName().getStringValue());
  }
}