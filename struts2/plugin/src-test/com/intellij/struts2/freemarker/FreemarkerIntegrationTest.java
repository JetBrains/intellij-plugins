/*
 * Copyright 2014 The authors
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

package com.intellij.struts2.freemarker;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.freemarker.inspections.FtlInspectionToolProvider;
import com.intellij.freemarker.inspections.FtlReferencesInspection;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import org.jetbrains.annotations.NotNull;

public class FreemarkerIntegrationTest extends BasicLightHighlightingTestCase {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "freemarker";
  }

  @Override
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{new FtlReferencesInspection()};
  }

  public void testMacroParametersResolveOnlyToParameters() {
    myFixture.enableInspections(new FtlInspectionToolProvider());
    myFixture.testHighlighting(true, false, false, "MacroParametersResolveOnlyToParameters.ftl");
  }

  public void testStrutsCommonVariables() {
    myFixture.testHighlighting(true, false, false, "StrutsCommonVariables.ftl");
  }

  public void testStrutsActionToplevel() {
    createStrutsFileSet("StrutsActionToplevel-struts.xml");
    myFixture.testHighlighting(true, false, false, "StrutsActionToplevel.ftl",
                               "MyTestAction.java");
  }
}
