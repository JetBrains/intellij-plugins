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
 *
 */

package com.intellij.struts2.facet.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link com.intellij.struts2.facet.ui.StrutsConfigsSearcher}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsConfigsSearcherTest extends BasicLightHighlightingTestCase {

  @Override
  @NotNull
  protected String getTestDataLocation() {
    return "configsSearcher";
  }

  public void testSearch() {
    myFixture.copyFileToProject("struts.xml");

    final StrutsConfigsSearcher configsSearcher = new StrutsConfigsSearcher(getModule());
    configsSearcher.search();

    final MultiMap<Module, PsiFile> map = configsSearcher.getFilesByModules();
    assertEquals(1, map.size());
    assertEquals(1, map.get(getModule()).size()); // /src/struts.xml
    assertEquals("struts.xml", map.get(getModule()).iterator().next().getName());

    final MultiMap<VirtualFile, PsiFile> configsInJars = configsSearcher.getJars();
    assertEquals(1, configsInJars.size()); // default-xxx.xml in struts2-core.jar
  }
}