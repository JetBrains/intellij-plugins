/*
 * Copyright 2007 The authors
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
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConfigsSearcher {

  private final Module module;
  private final MultiMap<Module, PsiFile> myFiles = new MultiMap<Module, PsiFile>();
  private final MultiMap<VirtualFile, PsiFile> myJars = new MultiMap<VirtualFile, PsiFile>();

  public StrutsConfigsSearcher(final Module module) {
    this.module = module;
  }

  public void search() {
    myFiles.clear();
    myJars.clear();

    final List<DomFileElement<StrutsRoot>> elements =
      DomService.getInstance().getFileElements(StrutsRoot.class, module.getProject(),
                                               GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));

    for (final DomFileElement<StrutsRoot> element : elements) {
      final XmlFile file = element.getFile();
      final VirtualFile jar = JarFileSystem.getInstance().getVirtualFileForJar(file.getVirtualFile());
      if (jar != null) {
        myJars.putValue(jar, file);
      } else {
        final Module module = ModuleUtil.findModuleForPsiElement(file);
        if (module != null) {
          myFiles.putValue(module, file);
        }
      }
    }
  }

  public MultiMap<Module, PsiFile> getFilesByModules() {
    return myFiles;
  }

  public MultiMap<VirtualFile, PsiFile> getJars() {
    return myJars;
  }

}