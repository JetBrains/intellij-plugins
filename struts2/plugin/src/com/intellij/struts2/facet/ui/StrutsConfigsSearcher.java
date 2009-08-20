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

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConfigsSearcher {

  @NonNls
  private static final String XML_EXTENSION = StdFileTypes.XML.getDefaultExtension();

  private final Module module;
  private final Map<Module, List<PsiFile>> myFiles = new LinkedHashMap<Module, List<PsiFile>>();
  private final Map<VirtualFile, List<PsiFile>> myJars = new LinkedHashMap<VirtualFile, List<PsiFile>>();

  public StrutsConfigsSearcher(final Module module) {
    this.module = module;
  }

  public void search() {
    myFiles.clear();
    myJars.clear();

    final Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();

    searchInModule(module);
    for (final Module dependentModule : dependencies) {
      searchInModule(dependentModule);
    }

    searchInJars(module);
    for (final Module dependentModule : dependencies) {
      searchInJars(dependentModule);
    }
  }

  private void searchInModule(@NotNull final Module module) {
    final Project project = module.getProject();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final StrutsManager strutsManager = StrutsManager.getInstance(project);

    ModuleRootManager.getInstance(module).getFileIndex().iterateContent(new ContentIterator() {

      public boolean processFile(final VirtualFile virtualFile) {
        if (StringUtil.endsWith(virtualFile.getName(), XML_EXTENSION)) {
          final PsiFile psiFile = psiManager.findFile(virtualFile);
          if (psiFile instanceof XmlFile &&
              strutsManager.isStruts2ConfigFile((XmlFile) psiFile)) {
            List<PsiFile> list = myFiles.get(module);
            if (list == null) {
              list = new ArrayList<PsiFile>();
              myFiles.put(module, list);
            }

            list.add(psiFile);
          }
        }
        return true;
      }
    });
  }

  private void searchInJars(@NotNull final Module module) {
    final Project project = module.getProject();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final StrutsManager strutsManager = StrutsManager.getInstance(project);
    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    for (final VirtualFile virtualFile : rootManager.getFiles(OrderRootType.CLASSES)) {
      if (isJarFile(virtualFile)) {
        if (virtualFile.isDirectory()) {
          final List<PsiFile> list = new ArrayList<PsiFile>();
          final ContentIterator iterator = new ContentIterator() {

            public boolean processFile(final VirtualFile fileOrDir) {
              if (fileOrDir.isDirectory()) {
                for (final VirtualFile child : fileOrDir.getChildren()) {
                  processFile(child);
                }
              } else if (fileOrDir.getName().endsWith(XML_EXTENSION)) {
                final PsiFile psiFile = psiManager.findFile(fileOrDir);
                if (psiFile instanceof XmlFile && strutsManager.isStruts2ConfigFile((XmlFile) psiFile)) {
                  list.add(psiFile);
                }
              }
              return true;
            }
          };
          iterator.processFile(virtualFile);
          if (list.size() > 0) {
            myJars.put(virtualFile, list);
          }
        }
      }
    }
  }

  private static boolean isJarFile(final VirtualFile file) {
    return FileTypeManager.getInstance().getFileTypeByFile(file) == StdFileTypes.ARCHIVE;
  }

  public Map<Module, List<PsiFile>> getFilesByModules() {
    return myFiles;
  }

  public Map<VirtualFile, List<PsiFile>> getJars() {
    return myJars;
  }

}