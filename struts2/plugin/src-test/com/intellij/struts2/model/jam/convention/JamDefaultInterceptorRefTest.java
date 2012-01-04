/*
 * Copyright 2012 The authors
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
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link JamDefaultInterceptorRef}.
 *
 * @author Yann C&eacute;bron
 */
public class JamDefaultInterceptorRefTest extends JamConventionTestBase<JavaModuleFixtureBuilder> {

  @NotNull
  @Override
  protected String getTestDataFolder() {
    return "defaultInterceptorRef";
  }

/* TODO test does not work
  @HasJavaSources
  @SkipStrutsLibrary
  public void testCompletion() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("/src/testcompletion/package-info.java",
                                     "myCustomInterceptor", "myInterceptorStack");
  }
*/

  @HasJavaSources
  @SkipStrutsLibrary
  public void testResolve() throws Exception {
    createStrutsFileSet(STRUTS_XML);

    myFixture.configureByFile("/src/jam/package-info.java");
    final PsiPackage myPackage = myFixture.findPackage("jam");

    final JamDefaultInterceptorRef jamElement = JamDefaultInterceptorRef.META_PACKAGE.getJamElement(myPackage);
    assertNotNull(jamElement);
    checkResolve(jamElement, "myCustomInterceptor");
  }


  private static void checkResolve(@NotNull final JamDefaultInterceptorRef jamDefaultInterceptorRef,
                                   @NotNull final String interceptorName) {
    final InterceptorOrStackBase interceptorOrStackBase = jamDefaultInterceptorRef.getValue().getValue();
    assertNotNull(interceptorOrStackBase);
    assertEquals(interceptorName, interceptorOrStackBase.getName().getStringValue());
  }

}