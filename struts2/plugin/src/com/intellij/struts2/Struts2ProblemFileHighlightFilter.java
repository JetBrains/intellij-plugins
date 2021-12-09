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

package com.intellij.struts2;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.facet.StrutsFacet;

/**
 * Include {@code struts.xml}-files configured in filesets and all {@code *-validation.xml}-files
 * for project view error highlighting.
 * <p/>
 * TODO include errors provided by DOM-inspections
 *
 * @author Yann C&eacute;bron
 */
public class Struts2ProblemFileHighlightFilter implements Condition<VirtualFile> {

  private final Project project;

  public Struts2ProblemFileHighlightFilter(final Project project) {
    this.project = project;
  }

  @Override
  public boolean value(final VirtualFile virtualFile) {
    if (!FileTypeRegistry.getInstance().isFileOfType(virtualFile, XmlFileType.INSTANCE)) {
      return false;
    }

    final Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
    if (module == null) {
      return false;
    }

    if (StrutsFacet.getInstance(module) == null) {
      return false;
    }

    final boolean isStrutsXml = ReadAction.compute(() -> {
      final StrutsManager strutsManager = StrutsManager.getInstance(project);

      final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      return psiFile instanceof XmlFile &&
             strutsManager.isStruts2ConfigFile((XmlFile)psiFile) &&
             strutsManager.getModelByFile((XmlFile)psiFile) != null;
    });
    if (isStrutsXml) {
      return true;
    }

    return ReadAction.compute(() -> {
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      return psiFile instanceof XmlFile
             && ValidatorManager.getInstance(project).isValidatorsFile((XmlFile)psiFile);
    });
  }
}