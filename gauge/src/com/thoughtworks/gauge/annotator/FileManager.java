/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.annotator;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.gauge.language.ConceptFileType;
import com.thoughtworks.gauge.language.SpecFileType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intellij.psi.search.GlobalSearchScope.moduleScope;
import static com.intellij.psi.search.GlobalSearchScope.projectScope;

public final class FileManager {
  public static List<PsiFile> getAllJavaFiles(Module module) {
    Collection<VirtualFile> javaVirtualFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, moduleScope(module));
    List<PsiFile> javaFiles = new ArrayList<>();

    for (VirtualFile javaVFile : javaVirtualFiles) {
      PsiFile file = PsiManager.getInstance(module.getProject()).findFile(javaVFile);
      if (file != null && PsiTreeUtil.findChildrenOfType(file, PsiClass.class).size() > 0) {
        javaFiles.add(file);
      }
    }
    javaFiles.sort((o1, o2) -> getJavaFileName(o1).compareToIgnoreCase(getJavaFileName(o2)));
    return javaFiles;
  }

  public static List<PsiFile> getAllConceptFiles(Project project) {
    Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(ConceptFileType.INSTANCE, projectScope(project));
    List<PsiFile> files = new ArrayList<>();

    for (VirtualFile ConceptVFile : virtualFiles) {
      PsiFile file = PsiManager.getInstance(project).findFile(ConceptVFile);
      if (file != null) {
        files.add(file);
      }
    }
    files.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    return files;
  }

  public static String getJavaFileName(PsiFile value) {
    PsiJavaFile javaFile = (PsiJavaFile)value;
    if (!javaFile.getPackageName().isEmpty()) {
      return javaFile.getPackageName() + "." + javaFile.getName();
    }
    return javaFile.getName();
  }

  public static List<VirtualFile> getAllSpecFiles(Project project) {
    Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(SpecFileType.INSTANCE, projectScope(project));
    return new ArrayList<>(virtualFiles);
  }

  public static List<VirtualFile> getConceptFiles(Project project) {
    Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(ConceptFileType.INSTANCE, projectScope(project));
    return new ArrayList<>(virtualFiles);
  }
}
