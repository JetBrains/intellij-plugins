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

package com.intellij.struts2.dom.struts.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsManagerImpl extends StrutsManager {

  private final StrutsModelFactory myStrutsModelFactory;

  public StrutsManagerImpl(final Project project) {
    myStrutsModelFactory = new StrutsModelFactory(project);
  }

  @Override
  public boolean isStruts2ConfigFile(@NotNull final XmlFile file) {
    return DomManager.getDomManager(file.getProject()).getFileElement(file, StrutsRoot.class) != null;
  }

  @Override
  @Nullable
  public StrutsModel getModelByFile(@NotNull final XmlFile file) {
    return myStrutsModelFactory.getModelByConfigFile(file);
  }

  @Override
  @NotNull
  public List<StrutsModel> getAllModels(@NotNull final Module module) {
    return myStrutsModelFactory.getAllModels(module);
  }

  @Override
  @Nullable
  public StrutsModel getCombinedModel(@Nullable final Module module) {
    return myStrutsModelFactory.getCombinedModel(module);
  }

  @Nullable
  @Override
  public StrutsModel getCombinedModel(@NotNull PsiElement psiElement) {
    return getCombinedModel(ModuleUtilCore.findModuleForPsiElement(psiElement));
  }

  @Override
  @NotNull
  public Set<StrutsFileSet> getAllConfigFileSets(@NotNull final Module module) {
    if (module.isDisposed()) {
      return Collections.emptySet();
    }

    final StrutsFacet facet = StrutsFacet.getInstance(module);
    if (facet != null) {
      return facet.getConfiguration().getFileSets();
    }
    return Collections.emptySet();
  }
}
