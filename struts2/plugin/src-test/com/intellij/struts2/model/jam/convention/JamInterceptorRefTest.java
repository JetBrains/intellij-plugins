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

import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link JamInterceptorRef}.
 *
 * @author Yann C&eacute;bron
 */
public class JamInterceptorRefTest extends JamConventionTestBase<JavaModuleFixtureBuilder> {

  @NotNull
  @Override
  protected String getTestDataFolder() {
    return "interceptorRef";
  }

  @SkipStrutsLibrary
  public void testCompletionActionSingle() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    myFixture.testCompletionVariants("/src/completion/ActionSingle.java",
                                     "myCustomInterceptor", "myInterceptorStack");
  }

  @SkipStrutsLibrary
  public void testResolveActionSingle() throws Exception {
    createStrutsFileSet(STRUTS_XML);
    final JamInterceptorRef jamInterceptorRef = getClassJam("jam.ActionSingle", JamInterceptorRef.META_CLASS);
    checkResolve(jamInterceptorRef, "myCustomInterceptor");
  }

  private void checkResolve(@NotNull final JamInterceptorRef jamInterceptorRef, @NotNull final String interceptorName) {
    final InterceptorOrStackBase interceptorOrStackBase = jamInterceptorRef.getValue().getValue();
    assertNotNull(interceptorOrStackBase);
    assertEquals(interceptorName, interceptorOrStackBase.getName().getStringValue());
  }

}