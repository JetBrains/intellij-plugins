/*
 * Copyright 2013 The authors
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
package com.intellij.struts2.dom;

import com.intellij.openapi.application.PathManager;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.util.xml.stubs.DomStubTest;

import java.io.File;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsDomStubTest extends DomStubTest {

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') +
           BasicLightHighlightingTestCase.TEST_DATA_PATH + "stubs";
  }

  public void testStrutsXml() {
    doBuilderTest("struts-stubs.xml",
                  "File:struts\n" +
                  "  Element:struts\n" +
                  "    Element:struts namespace:constant\n" +
                  "      Attribute:name:name\n" +
                  "      Attribute:value:value\n" +
                  "    Element:struts namespace:package\n" +
                  "      Attribute:name:anotherPackage\n" +
                  "    Element:struts namespace:package\n" +
                  "      Attribute:name:name\n" +
                  "      Attribute:extends:anotherPackage\n" +
                  "      Attribute:namespace:namespace\n" +
                  "      Element:struts namespace:result-types\n" +
                  "        Element:result-type\n" +
                  "          Attribute:name:resultType\n" +
                  "          Attribute:class:MyClass\n" +
                  "      Element:struts namespace:action\n" +
                  "        Attribute:name:action\n" +
                  "        Attribute:class:MyActionClass\n");
  }
}
