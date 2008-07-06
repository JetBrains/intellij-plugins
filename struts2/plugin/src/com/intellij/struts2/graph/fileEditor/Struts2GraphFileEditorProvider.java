/*
 * Copyright 2008 The authors
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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import com.intellij.util.xml.ui.PerspectiveFileEditorProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class Struts2GraphFileEditorProvider extends PerspectiveFileEditorProvider {

  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    return psiFile instanceof XmlFile &&
           StrutsManager.getInstance(project).isStruts2ConfigFile((XmlFile) psiFile);
  }

  @NotNull
  public PerspectiveFileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new Struts2GraphFileEditor(project, file);
  }

  public double getWeight() {
    return 0;
  }

}