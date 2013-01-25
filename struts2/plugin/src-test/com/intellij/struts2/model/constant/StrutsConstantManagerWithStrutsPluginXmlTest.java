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

package com.intellij.struts2.model.constant;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.struts2.Struts2ProjectDescriptorBuilder;
import com.intellij.struts2.StrutsConstants;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} with custom {@link <constant>} in
 * {@code struts-plugin.xml} from struts2-spring-plugin.jar.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerWithStrutsPluginXmlTest extends StrutsConstantManagerTestCase {

  static final String SPRING_JAR = "struts2-spring-plugin-" + STRUTS2_VERSION + ".jar";

  private static final LightProjectDescriptor SPRING = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withLibrary("spring", "spring.jar")
    .withLibrary("struts2-spring-plugin", SPRING_JAR);

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "model/constant/withPluginXml";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return SPRING;
  }

  public void testSpringPluginXml() throws Throwable {
    createStrutsFileSet(STRUTS_XML,
                        "lib/" + SPRING_JAR + "!/struts-plugin.xml");

    final PsiClass springObjectFactoryClass =
      myFixture.getJavaFacade().findClass(StrutsConstants.SPRING_OBJECT_FACTORY_CLASS,
                                          GlobalSearchScope.moduleWithLibrariesScope(myModule));
    assertNotNull(springObjectFactoryClass);

    final VirtualFile strutsXmlFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(strutsXmlFile, StrutsConstantKey.create("struts.objectFactory"), springObjectFactoryClass);
  }
}