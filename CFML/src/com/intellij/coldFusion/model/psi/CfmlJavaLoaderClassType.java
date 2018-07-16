/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;

/**
 * @author vnikolaenko
 */
public class CfmlJavaLoaderClassType extends PsiType {
  private GlobalSearchScope mySearchScope;
  private final Project myProject;

  private class JarFileScope extends GlobalSearchScope {
    private final VirtualFile myVirtualFile;
    private final Module myModule;

    private JarFileScope(@NotNull VirtualFile file) {
      myVirtualFile = file;
      ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
      myModule = fileIndex.getModuleForFile(myVirtualFile);
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
      return VfsUtilCore.isAncestor(myVirtualFile, file, true);
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull Module aModule) {
      return aModule == myModule;
    }

    @Override
    public boolean isSearchInLibraries() {
      return myModule == null;
    }
  }

  CfmlJavaLoaderClassType(PsiComment comment, Project project) {
    super(PsiAnnotation.EMPTY_ARRAY);
    final String text = comment.getText();

    myProject = project;
    Matcher javaLoaderMatcher = CfmlFile.LOADER_DECL_PATTERN_TEMP.matcher(text);
    mySearchScope = GlobalSearchScope.allScope(project);//EMPTY_SCOPE;

    if (javaLoaderMatcher.matches()) {
      Collection<String> collection = CfmlPsiUtil.findBetween(text, "loadPaths=\"", "\"");
      if (collection != null) {
        for (String str : collection) {
          VirtualFile file = JarFileSystem.getInstance().findFileByPath(str + JarFileSystem.JAR_SEPARATOR);
          if (file != null) {
            mySearchScope = mySearchScope.uniteWith(new JarFileScope(file));
          }
        }
      }
    }
  }

  GlobalSearchScope getSearchScope() {
    return mySearchScope;
  }

  @Override
  @NotNull
  public String getPresentableText() {
    return getCanonicalText();
  }

  @Override
  @NotNull
  public String getCanonicalText() {
    return "JavaLoader";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean equalsToText(@NotNull String text) {
    return false;
  }

  @Override
  public <A> A accept(@NotNull PsiTypeVisitor<A> visitor) {
    return visitor.visitType(this);
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return null;
  }

  @Override
  @NotNull
  public PsiType[] getSuperTypes() {
    return EMPTY_ARRAY;
  }
}