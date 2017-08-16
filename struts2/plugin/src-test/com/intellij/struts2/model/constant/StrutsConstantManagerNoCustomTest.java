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

package com.intellij.struts2.model.constant;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts2.model.constant.contributor.StrutsCoreConstantContributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} w/o any custom set property values (--> S2 default.properties).
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerNoCustomTest extends StrutsConstantManagerTestCase {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "model/constant/noCustom";
  }

  /**
   * Context = struts.xml
   */
  public void testNoCustomConfiguration() {
    createStrutsFileSet(STRUTS_XML);

    final VirtualFile strutsXmlFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(strutsXmlFile, StrutsCoreConstantContributor.ACTION_EXTENSION, Arrays.asList("action"));
  }

  /**
   * Context = dummy.xml
   */
  public void testNoCustomConfigurationFromDummyXml() {
    createStrutsFileSet(STRUTS_XML);

    final VirtualFile dummyFile = myFixture.copyFileToProject("dummy.xml");
    performResolveTest(dummyFile, StrutsCoreConstantContributor.ACTION_EXTENSION, Arrays.asList("action"));
  }

  /**
   * Non-existent configuration property.
   */
  public void testNoCustomNonExistentConfiguration() {
    createStrutsFileSet(STRUTS_XML);

    final VirtualFile strutsXmlFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(strutsXmlFile, StrutsConstantKey.create("XXX_NON_EXISTENT_XXX"), null);
  }
}