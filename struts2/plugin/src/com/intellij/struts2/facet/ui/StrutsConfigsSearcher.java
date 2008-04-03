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

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.util.xml.NanoXmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yann CŽbron
 */
public class StrutsConfigsSearcher {

  @NonNls
  private static final String XML_EXTENSION = StdFileTypes.XML.getDefaultExtension();

  private final FacetEditorContext myContext;
  private final Map<Module, List<PsiFile>> myFiles = new LinkedHashMap<Module, List<PsiFile>>();
  private final Map<VirtualFile, List<PsiFile>> myJars = new LinkedHashMap<VirtualFile, List<PsiFile>>();

  private final List<VirtualFile> myVirtualFiles = new ArrayList<VirtualFile>();

  public StrutsConfigsSearcher(final FacetEditorContext context) {
    myContext = context;
  }

  public void search() {
    myFiles.clear();
    myJars.clear();
    final Module module = myContext.getModule();
    if (module != null) {
      searchInModule(module);
      searchInJars(module);
      final Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();
      for (final Module dep : dependencies) {
        searchInJars(dep);
      }
    } else {
      final ModuleBuilder builder = myContext.getModuleBuilder();
      if (builder instanceof SourcePathsBuilder) {
        final String entryPath = ((SourcePathsBuilder) builder).getContentEntryPath();
        if (entryPath != null) {
          final VirtualFile root = LocalFileSystem.getInstance().findFileByPath(entryPath);
          if (root != null) {
            final ContentIterator iterator = new ContentIterator() {
              public boolean processFile(final VirtualFile fileOrDir) {
                if (fileOrDir.isDirectory()) {
                  for (final VirtualFile child : fileOrDir.getChildren()) {
                    processFile(child);
                  }
                } else {
                  if (fileOrDir.getName().endsWith(XML_EXTENSION)) {
                    final String result = NanoXmlUtil.parseHeader(fileOrDir).getRootTagLocalName();
                    if (result != null && result.equals(StrutsRoot.TAG_NAME)) {
                      myVirtualFiles.add(fileOrDir);
                    }
                  }
                }
                return true;
              }
            };
            iterator.processFile(root);
          }
        }
      }
    }
  }

  private void searchInModule(@NotNull final Module module) {
    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    final Project project = module.getProject();
    final StrutsManager strutsManager = StrutsManager.getInstance(project);
    final PsiShortNamesCache namesCache = JavaPsiFacade.getInstance(project).getShortNamesCache();
    final String[] fileNames = namesCache.getAllFileNames();
    for (final String fileName : fileNames) {
      if (fileName.endsWith(XML_EXTENSION)) {
        final PsiFile[] psiFiles = namesCache.getFilesByName(fileName);
        for (final PsiFile file : psiFiles) {
          if (file instanceof XmlFile && strutsManager.isStruts2ConfigFile((XmlFile) file)) {
            final Module fileModule = ModuleUtil.findModuleForPsiElement(file);
            if (fileModule != null && (fileModule.equals(module) || rootManager.isDependsOn(fileModule))) {
              List<PsiFile> list = myFiles.get(fileModule);
              if (list == null) {
                list = new ArrayList<PsiFile>();
                myFiles.put(fileModule, list);
              }
              list.add(file);
            }
          }
        }
      }
    }
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

  public List<VirtualFile> getVirtualFiles() {
    return myVirtualFiles;
  }

}