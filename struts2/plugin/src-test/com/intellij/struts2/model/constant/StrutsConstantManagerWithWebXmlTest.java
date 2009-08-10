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

package com.intellij.struts2.model.constant;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} with custom constant property in
 * {@code web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerWithWebXmlTest extends StrutsConstantManagerTestCase<WebModuleFixtureBuilder> {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "model/constant/withWebXml";
  }

  @Override
  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addLibrary(moduleBuilder, "servlet-api", "servlet-api.jar");

    moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
  }

  public void testWebXml() throws Throwable {
    createStrutsFileSet(STRUTS_XML);
    myFixture.copyFileToProject("/WEB-INF/web.xml");

    final VirtualFile dummyFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(dummyFile, "struts.action.extension", "foo");
  }

}