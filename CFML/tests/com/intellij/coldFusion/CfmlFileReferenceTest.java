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

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 1/26/12
 */
public class CfmlFileReferenceTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  public void testRename() throws Throwable {
    doRenameFileTest("newName.test.cfml", "<cfinclude template=\"rename.test.cfml\">");
  }

  public void testRenameInScript() throws Throwable {
    doRenameFileTest("newName.test.cfml", "<cfscript>\n" +
                                          "    include \"renameInScript.test.cfml\";\n" +
                                          "\n" +
                                          "</cfscript>");
  }

  public void testMoveOldFile() throws Throwable {
    doMoveFileTest("testDir", "<cfinclude template=\"moveOldFile.test.cfml\">");
  }

  public void testMoveOldFileInScript() throws Throwable {
    doMoveFileTest("testDir", "<cfscript>\n" +
                              "    include \"testDir/moveOldFileInScript.test.cfml\";\n" +
                              "\n" +
                              "</cfscript>");
  }

  public void testFileReferencesWithErrors() throws Throwable {
    doHighlighting();
  }

  public void testFileReferencesWithErrorsInScript() throws Throwable {
    doHighlighting();
  }

  private void doRenameFileTest(final String newName, String fileTextWithReference) throws Exception {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.addFileToProject("fileWithReference.test.cfml", fileTextWithReference);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        myFixture.renameElement(myFixture.getFile(), newName);
      }
    });
    myFixture.checkResultByFile("fileWithReference.test.cfml", Util.getExpectedDataFileName(getTestName(true)), false);
  }

  public void testFileNameAttributeCompletion() throws Throwable {
    doTestCompletionVariants("rename.test.cfml", "moveOldFile.test.cfml", "fileNameAttributeCompletion.test.cfml");
  }

  public void testFilePathInScriptCompletion() throws Throwable {
    doTestCompletionVariants("rename.test.cfml", "moveOldFile.test.cfml", "filePathInScriptCompletion.test.cfml");
  }


  private void doMoveFileTest(final String newPath, String fileTextWithReference) throws Exception {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.getTempDirFixture().findOrCreateDir(newPath);
    myFixture.addFileToProject("fileWithMoveReference.test.cfml", fileTextWithReference);
    myFixture.moveFile(Util.getInputDataFileName(getTestName(true)), newPath);
    myFixture.checkResultByFile("fileWithMoveReference.test.cfml", Util.getExpectedDataFileName(getTestName(true)), false);
  }

  private void doTestCompletionVariants(@NonNls String... items) throws Throwable {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    myFixture.addFileToProject("rename.test.cfml", "");
    myFixture.addFileToProject("moveOldFile.test.cfml", "");
    myFixture.testCompletionVariants(inputDataFileName, items);
  }

  private void doHighlighting() throws Throwable {
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





