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

package com.intellij.struts2.dom.struts.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yann C&eacute;bron
 */
class StrutsModelFactory extends DomModelFactory<StrutsRoot, StrutsModel, PsiElement> {

  protected StrutsModelFactory(final Project project) {
    super(StrutsRoot.class, project, "struts2");
  }

  @Override
  protected List<StrutsModel> computeAllModels(@NotNull final Module module) {
    final PsiManager psiManager = PsiManager.getInstance(module.getProject());
    final StrutsManager strutsManager = StrutsManager.getInstance(module.getProject());
    final Set<StrutsFileSet> fileSets = strutsManager.getAllConfigFileSets(module);

    final List<StrutsModel> models = new ArrayList<>(fileSets.size());
    for (final StrutsFileSet set : fileSets) {
      if (set.isRemoved()) {
        continue;
      }

      final Set<XmlFile> files = new LinkedHashSet<>(set.getFiles().size());
      for (final VirtualFilePointer filePointer : set.getFiles()) {
        if (!filePointer.isValid()) continue;
        final VirtualFile file = filePointer.getFile();
        if (file == null) {
          continue;
        }
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile instanceof XmlFile) {
          final StrutsRoot strutsRootDom = getDom((XmlFile)psiFile);
          if (strutsRootDom != null) {
            files.add((XmlFile)psiFile);
            // TODO           addIncludes(files, strutsRootDom);
          }
        }
      }
      if (!files.isEmpty()) {
        final DomFileElement<StrutsRoot> element = createMergedModelRoot(files);
        final StrutsModel model;
        if (element != null) {
          model = new StrutsModelImpl(element, files);
          models.add(model);
        }
      }
    }

    return models;
  }

  @Override
  protected StrutsModel createCombinedModel(@NotNull final Set<XmlFile> xmlFiles,
                                            @NotNull final DomFileElement<StrutsRoot> strutsRootDomFileElement,
                                            final StrutsModel strutsModel,
                                            final Module module) {
    return new StrutsModelImpl(strutsRootDomFileElement, xmlFiles);
  }
}