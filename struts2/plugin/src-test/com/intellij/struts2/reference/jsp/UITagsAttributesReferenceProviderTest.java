/*
 * Copyright 2011 The authors
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
package com.intellij.struts2.reference.jsp;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.xml.util.XmlDuplicatedIdInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for various attributes in S2 UI tags.
 *
 * @author Yann C&eacute;bron
 */
public class UITagsAttributesReferenceProviderTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{new XmlDuplicatedIdInspection()};
  }

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "reference/jsp/uitags/";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/jsp", "/");
  }

  public void testPathAttributes() throws Throwable {
    myFixture.testHighlighting(true, false, false, "/jsp/paths.jsp");
  }

  public void testCommonAttributes() throws Throwable {
    myFixture.testHighlighting(true, false, false, "/jsp/common.jsp");
  }

  public void testSpecificAttributes() throws Throwable {
    myFixture.testHighlighting(true, false, false, "/jsp/specific.jsp");
  }

}