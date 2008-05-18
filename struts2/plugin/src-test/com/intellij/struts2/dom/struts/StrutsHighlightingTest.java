/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.struts;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Various basic and complex struts.xml highlighting tests.
 *
 * @author Yann CŽbron
 */
public class StrutsHighlightingTest extends BasicStrutsHighlightingTestCase<JavaModuleFixtureBuilder> {

  @NotNull
  protected String getTestDataLocation() {
    return "strutsXmlHighlighting";
  }

  public void testSimpleStruts() throws Throwable {
    performHighlightingTest("struts-simple.xml");
  }

  public void testParam() throws Throwable {
    performHighlightingTest("struts-param.xml");
  }

  public void testConstants() throws Throwable {
    performHighlightingTest("struts-constants.xml");
  }

  public void testExceptionMapping() throws Throwable {
    performHighlightingTest("struts-exceptionmapping.xml");
  }

  public void testStrutsDefault() throws Throwable {
    performHighlightingTest("struts-default.xml");
  }

  public void testXWorkDefault() throws Throwable {
    performHighlightingTest("xwork-default.xml");
  }

  public void testStrutsWithErrorsNotInFilesetNoHighlighting() throws Throwable {
    createStrutsFileSet("struts-default.xml");
    myFixture.testHighlighting(false, false, true, "struts-errors.xml");
  }

}