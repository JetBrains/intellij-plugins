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
import com.intellij.psi.search.FilenameIndex;
import com.intellij.struts2.Struts2ProjectDescriptorBuilder;
import com.intellij.struts2.StrutsConstants;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.psi.search.GlobalSearchScope.moduleWithLibrariesScope;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} with custom {@link <constant>} in
 * {@code struts-plugin.xml} from struts2-spring-plugin.jar.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerWithStrutsPluginXmlTest extends StrutsConstantManagerTestCase {
  private static final LightProjectDescriptor SPRING = new Struts2ProjectDescriptorBuilder()
    .withStrutsLibrary()
    .withStrutsFacet()
    .withMavenLibrary("org.apache.struts:struts2-spring-plugin:2.3.1")
    .withMavenLibrary("org.springframework:spring-beans:3.0.5.RELEASE");

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

  public void testSpringPluginXml() {
    Collection<VirtualFile> configFiles =
      FilenameIndex.getVirtualFilesByName("struts-plugin.xml", moduleWithLibrariesScope(getModule()));
    assertSize(1, configFiles);

    VirtualFile springStrutsPluginXml = configFiles.iterator().next();

    createStrutsFileSet(STRUTS_XML, springStrutsPluginXml.getUrl());

    PsiClass springObjectFactoryClass =
      myFixture.getJavaFacade().findClass(StrutsConstants.SPRING_OBJECT_FACTORY_CLASS,
                                          moduleWithLibrariesScope(getModule()));
    assertNotNull(springObjectFactoryClass);

    final VirtualFile strutsXmlFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(strutsXmlFile, StrutsConstantKey.create("struts.objectFactory"), springObjectFactoryClass);
  }
}