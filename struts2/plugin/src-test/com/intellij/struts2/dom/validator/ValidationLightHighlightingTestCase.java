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

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.struts2.dom.inspection.ValidatorConfigModelInspection;
import com.intellij.struts2.dom.inspection.ValidatorModelInspection;
import org.jetbrains.annotations.NonNls;

/**
 * Base class for validation.xml highlighting tests.
 *
 * @author Yann C&eacute;bron
 */
abstract class ValidationLightHighlightingTestCase extends BasicLightHighlightingTestCase {

  @Override
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{new ValidatorModelInspection(), new ValidatorConfigModelInspection()};
  }

  /**
   * Performs highlighting test for the given validation.xml file.
   *
   * @param validationXmlFileName Filename of validation.xml to check.
   */
  protected void performHighlightingTest(@NonNls final String validationXmlFileName) {
    myFixture.testHighlighting(true, false, false, validationXmlFileName);
  }

  /**
   * Performs completion variants test for the given validation.xml file.
   *
   * @param validationXmlFileName Filename of validation.xml to check.
   * @param expectedItems         Expected completion variants.
   */
  protected void performCompletionVariantTest(@NonNls final String validationXmlFileName,
                                              @NonNls final String... expectedItems) {
    myFixture.testCompletionVariants(validationXmlFileName, expectedItems);
  }
}