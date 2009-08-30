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
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} with custom constant property in
 * {@code struts.properties}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerWithStrutsPropertiesTest extends StrutsConstantManagerTestCase<JavaModuleFixtureBuilder> {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "model/constant/withStrutsProperties";
  }

  @Override
  protected boolean hasJavaSources() {
    return true;
  }

  /**
   * Must override {@code "bar"} from struts.xml.
   */
  public void testStrutsProperties() throws Throwable {
    createStrutsFileSet(STRUTS_XML);

    final VirtualFile dummyFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(dummyFile, StrutsCoreConstantContributor.ACTION_EXTENSION, "foo");
  }

}