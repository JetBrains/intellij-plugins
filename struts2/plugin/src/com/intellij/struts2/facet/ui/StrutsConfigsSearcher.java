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
package com.intellij.struts2.facet.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import com.intellij.xml.config.ConfigFileSearcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConfigsSearcher extends ConfigFileSearcher {

  public StrutsConfigsSearcher(@NotNull Module module) {
    super(module, module.getProject());
  }

  @Override
  public Set<PsiFile> search(@Nullable Module module, @NotNull Project project) {
    assert module != null;

    final List<DomFileElement<StrutsRoot>> elements =
      DomService.getInstance().getFileElements(StrutsRoot.class, project,
                                               GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));

    return ContainerUtil.map2Set(elements, element -> element.getFile());
  }
}