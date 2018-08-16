/*
 * Copyright 2015 The authors
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
package com.intellij.struts2.graph.fileEditor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.facet.ui.StrutsFileSet;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import com.intellij.util.xml.ui.PerspectiveFileEditorProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Provides "Graph"-tab for struts.xml files registered in S2 fileset.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2GraphFileEditorProvider extends PerspectiveFileEditorProvider {

  @Override
  public boolean accept(@NotNull final Project project, @NotNull final VirtualFile file) {
    if (!file.isValid()) {
      return false;
    }

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

    if (!(psiFile instanceof XmlFile)) {
      return false;
    }

    if (psiFile instanceof JspFile) {
      return false;
    }

    if (!StrutsManager.getInstance(project).isStruts2ConfigFile((XmlFile) psiFile)) {
      return false;
    }

    final Module module = ModuleUtilCore.findModuleForFile(file, project);
    if (module == null) {
      return false;
    }

    final Set<StrutsFileSet> fileSets = StrutsManager.getInstance(project).getAllConfigFileSets(module);
    for (final StrutsFileSet fileSet : fileSets) {
      if (fileSet.hasFile(file)) {
        return true;
      }
    }

    return false;
  }

  @Override
  @NotNull
  public PerspectiveFileEditor createEditor(@NotNull final Project project, @NotNull final VirtualFile file) {
    return new Struts2GraphFileEditor(project, file);
  }

  @Override
  public boolean isDumbAware() {
    return false;
  }

  @Override
  public double getWeight() {
    return 0;
  }

}
