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

import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.util.xml.stubs.DomStubTest;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsDomStubTest extends DomStubTest {

  @Override
  protected String getBasePath() {
    return BasicLightHighlightingTestCase.TEST_DATA_PATH + "stubs";
  }

  public void testStrutsXml() {
    doBuilderTest("struts-stubs.xml",
                  """
                    File:struts
                      Element:struts
                        Element:struts namespace:constant
                          Attribute:name:name
                          Attribute:value:value
                        Element:struts namespace:package
                          Attribute:name:anotherPackage
                        Element:struts namespace:package
                          Attribute:name:name
                          Attribute:extends:anotherPackage
                          Attribute:namespace:namespace
                          Element:struts namespace:result-types
                            Element:result-type
                              Attribute:name:resultType
                              Attribute:class:MyClass
                          Element:struts namespace:action
                            Attribute:name:action
                            Attribute:class:MyActionClass
                    """);
  }
}
