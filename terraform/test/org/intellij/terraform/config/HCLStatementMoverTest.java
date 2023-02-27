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
package org.intellij.terraform.config;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.intellij.terraform.TerraformTestUtils;

import java.io.File;
import java.io.IOException;

public class HCLStatementMoverTest extends LightJavaCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath() + "/terraform/mover";
  }

  private void both() throws IOException {
    doTest(true, true);
  }

  private void up() throws IOException {
    doTest(true, false);
  }

  private void down() throws IOException {
    doTest(false, true);
  }

  private void doTest(boolean up, boolean down) throws IOException {
    final String testName = getTestName(false);

    File source = new File(getTestDataPath() + "/" + testName + ".tf");
    if (!source.exists()) {
      assertTrue(source.createNewFile());
      fail("No source file exist, created");
    }

    if (up) {
      String expectedFile = testName + ".after_up.tf";
      File check = new File(getTestDataPath() + "/" + expectedFile);
      if (!check.exists()) {
        FileUtil.copy(source, check);
        fail("No check file exist, copied from base one");
      }

      myFixture.configureByFile(testName + ".tf");
      myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_UP_ACTION);
      myFixture.checkResultByFile(expectedFile, true);
    }

    if (down) {
      String expectedFile = testName + ".after_down.tf";
      File check = new File(getTestDataPath() + "/" + expectedFile);
      if (!check.exists()) {
        FileUtil.copy(source, check);
        fail("No check file exist, copied from base one");
      }
      if (up) {
        FileDocumentManager.getInstance().reloadFromDisk(myFixture.getDocument(myFixture.getFile()));
      }
      myFixture.configureByFile(testName + ".tf");
      myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_DOWN_ACTION);
      myFixture.checkResultByFile(expectedFile, true);
    }
  }


  public void testSimpleProperty() throws Exception {
    both();
  }

  public void testSimpleBlock() throws Exception {
    both();
  }

  public void testPropertyIntoBlock() throws Exception {
    both();
  }

  public void testPropertyFromBlock() throws Exception {
    both();
  }

  public void testArrayElement() throws Exception {
    both();
  }

  public void testNoEscapeFromArray() throws Exception {
    both();
  }

  public void testBlockClosingBrace() throws Exception {
    both();
  }

  public void testEmptyBlockClosingBrace() throws Exception {
    both();
  }
}