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

package com.intellij.struts2.dom.validator;

import org.jetbrains.annotations.NotNull;

/**
 * Various basic and complex validation.xml highlighting tests.
 *
 * @author Yann C&eacute;bron
 */
public class ValidationHighlightingTest extends ValidationLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "validationXml/highlighting";
  }

  public void testValidationSimple() {
    myFixture.copyFileToProject("com/MyAction.java");
    performHighlightingTest("com/MyAction-validation.xml");
  }
}