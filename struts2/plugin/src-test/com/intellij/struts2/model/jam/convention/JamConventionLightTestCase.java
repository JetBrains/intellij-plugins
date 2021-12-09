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

import com.intellij.jam.JamElement;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.jam.reflect.JamPackageMeta;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.struts2.Struts2ProjectDescriptorBuilder;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Convention JAM tests.
 *
 * @author Yann C&eacute;bron
 */
abstract class JamConventionLightTestCase extends BasicLightHighlightingTestCase {

  private static final LightProjectDescriptor CONVENTION = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withStrutsConvention();

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "model/jam/convention/" + getTestDataFolder();
  }

  /**
   * Returns the test data folder name located relative to {@code model/jam/convention}.
   *
   * @return Folder name.
   */
  @NonNls
  @NotNull
  protected abstract String getTestDataFolder();

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CONVENTION;
  }

  /**
   * Gets the JAM-element for the given class.
   *
   * @param clazzName FQN of class.
   * @param meta      JAM-Meta.
   * @param <Jam>     JAM-Type.
   * @return JAM.
   */
  @NotNull
  protected <Jam extends JamElement> Jam getClassJam(final String clazzName,
                                                     final JamClassMeta<Jam> meta) {
    final PsiClass myClass = myFixture.findClass(clazzName);
    assertNotNull(clazzName, myClass);

    final Jam jam = meta.getJamElement(myClass);
    assertNotNull("JAM was null for " + meta + " in '" + clazzName + "'", jam);
    return jam;
  }

  /**
   * Gets the JAM-element for the given package.
   *
   * @param packageName FQN of package.
   * @param meta        JAM-Meta.
   * @param <Jam>       JAM-Type.
   * @return JAM.
   */
  @NotNull
  protected <Jam extends JamElement> Jam getPackageJam(final String packageName,
                                                       final JamPackageMeta<Jam> meta) {
    final PsiPackage myPackage = myFixture.findPackage(packageName);
    assertNotNull(packageName, myPackage);

    final Jam jam = meta.getJamElement(myPackage);
    assertNotNull("JAM was null for " + meta + " in '" + packageName + "'", jam);
    return jam;
  }
}