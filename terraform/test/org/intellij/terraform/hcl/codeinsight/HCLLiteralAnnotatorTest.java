/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
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
package org.intellij.terraform.hcl.codeinsight;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.intellij.terraform.TerraformTestUtils;

public class HCLLiteralAnnotatorTest extends CodeInsightFixtureTestCase {
  private String myExtension;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myExtension = "hcl";
    myFixture.setTestDataPath(getBasePath());
  }

  private void terraform() {
    myExtension = "tf";
  }

  @Override
  protected String getBasePath() {
    return TerraformTestUtils.getTestDataPath() + "/terraform/annotator/";
  }

  protected String getExtension() {
    return myExtension;
  }

  protected void doTestHighlighting(boolean checkInfo, boolean checkWeakWarning, boolean checkWarning) {
    myFixture.testHighlighting(checkWarning, checkInfo, checkWeakWarning, getTestName(false) + "." + getExtension());
  }

  public void testNumbers() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void testMethodCalls() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void testForEllipsis() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void testHCL1StringKeys() throws Exception {
    terraform();
    doTestHighlighting(false, true, true);
  }

  public void testHCL2StringKeys() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void test324() throws Exception {
    terraform();
    doTestHighlighting(false, true, true);
  }
}
