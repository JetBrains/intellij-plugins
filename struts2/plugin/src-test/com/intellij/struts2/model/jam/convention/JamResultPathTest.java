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
package com.intellij.struts2.model.jam.convention;

import com.intellij.javaee.web.WebDirectoryElement;
import com.intellij.lang.properties.IProperty;
import com.intellij.struts2.Struts2ProjectDescriptorBuilder;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.jam.convention.JamResultPath}.
 *
 * @author Yann C&eacute;bron
 */
public class JamResultPathTest extends JamConventionLightTestCase {
  private final LightProjectDescriptor CONVENTION_WEB = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withStrutsConvention()
    .withWebModuleType();

  @NotNull
  @Override
  protected String getTestDataFolder() {
    return "resultPath";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CONVENTION_WEB;
  }

  /**
   * "property" variants from struts.properties.
   */
  public void testCompletionActionProperty() {
    myFixture.copyFileToProject("struts.properties");
    myFixture.testCompletionVariants("testcompletion/ActionProperty.java",
                                     "myProperty1", "myProperty2");
  }

  /**
   * "property" resolving to key in struts.properties.
   */
  public void testResolveActionProperty() {
    myFixture.copyFileToProject("jam/ActionProperty.java");
    myFixture.copyFileToProject("struts.properties");

    final JamResultPath jamResultPath = getClassJam("jam.ActionProperty", JamResultPath.META_CLASS);
    final IProperty property = jamResultPath.getProperty().getValue();
    assertNotNull(property);
    assertEquals("myProperty1", property.getName());
  }

  /**
   * "value" resolving to web-dir.
   */
  public void testResolveActionValue() {
    myFixture.copyFileToProject("WEB-INF/customContent/test.jsp");
    myFixture.copyFileToProject("jam/ActionValue.java");

    final JamResultPath jamResultPath = getClassJam("jam.ActionValue", JamResultPath.META_CLASS);
    final WebDirectoryElement webDirectoryElement = jamResultPath.getValue().getValue();
    assertNotNull(webDirectoryElement);
    assertEquals("/WEB-INF/customContent", webDirectoryElement.getPath());
  }
}