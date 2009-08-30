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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.struts2.StrutsConstants;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.model.constant.StrutsConstantManager} with custom {@link <constant>} in
 * {@code struts-plugin.xml} from struts2-spring-plugin.jar.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerWithStrutsPluginXmlTest extends StrutsConstantManagerTestCase<JavaModuleFixtureBuilder> {

  @NotNull
  @Override
  protected String getTestDataLocation() {
    return "model/constant/withPluginXml";
  }

  @Override
  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addLibrary(moduleBuilder, "struts2-spring-plugin", STRUTS2_SPRING_PLUGIN_JAR);
  }

  public void testSpringPluginXml() throws Throwable {
    createStrutsFileSet(STRUTS_XML);
    addStrutsXmlFromJar(STRUTS2_SPRING_PLUGIN_JAR + "!/struts-plugin.xml");

    final PsiClass springObjectFactoryClass = JavaPsiFacade.getInstance(myProject)
        .findClass(StrutsConstants.SPRING_OBJECT_FACTORY_CLASS,
                   GlobalSearchScope.moduleWithLibrariesScope(myModule));
    assertNotNull(springObjectFactoryClass);

    final VirtualFile dummyFile = myFixture.findFileInTempDir(STRUTS_XML);
    performResolveTest(dummyFile, StrutsConstantKey.create("struts.objectFactory"), springObjectFactoryClass);
  }

}