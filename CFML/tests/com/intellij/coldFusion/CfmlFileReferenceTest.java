/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion;

import org.jetbrains.annotations.NonNls;

public class CfmlFileReferenceTest extends CfmlCodeInsightFixtureTestCase {

  public void testRename() {
    doRenameFileTest("newName.test.cfml", "<cfinclude template=\"rename.test.cfml\">");
  }

  public void testRenameInScript() {
    doRenameFileTest("newName.test.cfml", """
      <cfscript>
          include "renameInScript.test.cfml";

      </cfscript>""");
  }

  public void testMoveOldFile() throws Throwable {
    doMoveFileTest("testDir", "<cfinclude template=\"moveOldFile.test.cfml\">");
  }

  public void testMoveOldFileInScript() throws Throwable {
    doMoveFileTest("testDir", """
      <cfscript>
          include "testDir/moveOldFileInScript.test.cfml";

      </cfscript>""");
  }

  public void testFileReferencesWithErrors() {
    doHighlighting();
  }

  public void testFileReferencesWithErrorsInScript() {
    doHighlighting();
  }

  private void doRenameFileTest(final String newName, String fileTextWithReference) {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.addFileToProject("fileWithReference.test.cfml", fileTextWithReference);
    myFixture.renameElement(myFixture.getFile(), newName);
    myFixture.checkResultByFile("fileWithReference.test.cfml", Util.getExpectedDataFileName(getTestName(true)), false);
  }

  public void testFileNameAttributeCompletion() {
    doTestCompletionVariants("rename.test.cfml", "moveOldFile.test.cfml", "fileNameAttributeCompletion.test.cfml");
  }

  public void testFilePathInScriptCompletion() {
    doTestCompletionVariants("rename.test.cfml", "moveOldFile.test.cfml", "filePathInScriptCompletion.test.cfml");
  }


  private void doMoveFileTest(final String newPath, String fileTextWithReference) throws Exception {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.getTempDirFixture().findOrCreateDir(newPath);
    myFixture.addFileToProject("fileWithMoveReference.test.cfml", fileTextWithReference);
    myFixture.moveFile(Util.getInputDataFileName(getTestName(true)), newPath);
    myFixture.checkResultByFile("fileWithMoveReference.test.cfml", Util.getExpectedDataFileName(getTestName(true)), false);
  }

  private void doTestCompletionVariants(@NonNls String... items) {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    myFixture.addFileToProject("rename.test.cfml", "");
    myFixture.addFileToProject("moveOldFile.test.cfml", "");
    myFixture.testCompletionVariants(inputDataFileName, items);
  }

  private void doHighlighting() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.addFileToProject("rename.test.cfml", "");
    myFixture.addFileToProject("moveOldFile.test.cfml", "");
    myFixture.testHighlighting(true, false, true);
  }

  @Override
  protected String getBasePath() {
    return "/manipulator";
  }
}





