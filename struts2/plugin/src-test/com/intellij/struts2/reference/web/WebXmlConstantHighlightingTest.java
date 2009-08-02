/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.reference.web;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.j2ee.web.highlighting.WebWarningInspection;
import com.intellij.j2ee.web.highlighting.WebXmlInspection;
import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * {@code <param-name>/<param-value>} in {@code web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class WebXmlConstantHighlightingTest extends BasicHighlightingTestCase<WebModuleFixtureBuilder> {

  protected LocalInspectionTool[] getHighlightingInspections() {
    return new LocalInspectionTool[]{new WebXmlInspection(), new WebWarningInspection()};
  }

  @NotNull
  protected String getTestDataLocation() {
    return "/reference/web/constant/";
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addLibrary(moduleBuilder, "servlet-api", "servlet-api.jar");

    moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
  }

  public void testHighlighting() throws Throwable {
    myFixture.copyFileToProject("/WEB-INF/web.xml");
    myFixture.testHighlighting(true, false, false, "/WEB-INF/web.xml");
  }

}
