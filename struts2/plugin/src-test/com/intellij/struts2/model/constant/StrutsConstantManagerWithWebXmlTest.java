/*
 * Copyright 2017 The authors
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
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} with custom constant property in
 * {@code web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerWithWebXmlTest extends StrutsConstantManagerTestCase {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "model/constant/withWebXml";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return WEB;
  }

  public void testWebXml() {
    myFixture.copyDirectoryToProject("WEB-INF", "WEB-INF");
    createStrutsFileSet(STRUTS_XML);

    final VirtualFile strutsXmlFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(strutsXmlFile, StrutsCoreConstantContributor.ACTION_EXTENSION, Arrays.asList("foo"));
  }
}