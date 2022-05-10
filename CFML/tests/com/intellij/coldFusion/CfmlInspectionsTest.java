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

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.coldFusion.UI.config.CfmlMappingsConfig;
import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.UI.inspections.CfmlFileReferenceInspection;
import com.intellij.coldFusion.UI.inspections.CfmlReferenceInspection;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vnikolaenko
 */
public class CfmlInspectionsTest extends CfmlCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/inspections";
  }

  @SafeVarargs
  private void doInspectionTest(boolean infos, Class<? extends LocalInspectionTool>... inspectionClasses) {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    myFixture.configureByFiles(inputDataFileName);
    myFixture.enableInspections(inspectionClasses);
    myFixture.testHighlighting(true, infos, true);
  }

  static PsiFile addScriptComponentsTo(CodeInsightTestFixture fixture) {
    return fixture.addFileToProject("folder/subfolder/ComponentName.cfc",
                                    "component {\n" +
                                    "    function func1(){}\n" +
                                    "\n" +
                                    "    function func2() {}\n" +
                                    "}");
  }

  public void testSpellCheck() {
    doInspectionTest(false, SpellCheckingInspection.class);
  }

  public void testUnResolveIncludeWithMappings() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    addScriptComponentsTo(myFixture);
    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      String directoryName = root.getPresentableUrl() + "/folder";
      VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(directoryName);
      if (fileByUrl != null) {
        mappings.put("/myf", directoryName);
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      myFixture.enableInspections(CfmlFileReferenceInspection.class);
      myFixture.testHighlighting(true, false, true);
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }


  public void testResolveIncludeWithBaclSlashMappings() {
    myFixture.configureByFile(getTestName(true) + ".test.cfml");
    addScriptComponentsTo(myFixture);
    Map<String, String> mappings = new HashMap<>();
    for (VirtualFile root : ProjectRootManager.getInstance(getProject()).getContentRoots()) {
      VirtualFile directory = root.findChild("folder");
      if (directory != null && directory.isDirectory()) {
        mappings.put("\\myf", directory.getUrl());
      }
    }

    CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State state = new CfmlProjectConfiguration.State(new CfmlMappingsConfig(mappings));
    try {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(state);
      myFixture.enableInspections(CfmlFileReferenceInspection.class);
      myFixture.testHighlighting(true, false, true);
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
  }



  public void testUnresolvedScopeVariable() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    myFixture.enableInspections(CfmlReferenceInspection.class);
    myFixture.testHighlighting(true, false, true);
  }

  public void testComponentSpellCheck() {
    doInspectionTest(false, SpellCheckingInspection.class);
  }

  public void testArgumentsStandartVariable() {
    doInspectionTest(false, CfmlReferenceInspection.class);
  }

  public void testArgumentNamesOfStandardFunction() {
    doInspectionTest(false, CfmlReferenceInspection.class);
  }

  public void testPredefinedFunctionsNoInspection() {
    doInspectionTest(false, CfmlReferenceInspection.class);
  }
}
