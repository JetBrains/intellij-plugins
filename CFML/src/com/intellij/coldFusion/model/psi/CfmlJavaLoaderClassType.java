// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;

public class CfmlJavaLoaderClassType extends PsiType {
  private final GlobalSearchScope mySearchScope;
  private final Project myProject;

  private final class JarFileScope extends GlobalSearchScope {
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
    GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);//EMPTY_SCOPE;

    if (javaLoaderMatcher.matches()) {
      Collection<String> collection = CfmlPsiUtil.findBetween(text, "loadPaths=\"", "\"");
      GlobalSearchScope[] scopes = collection.stream()
        .map(str -> JarFileSystem.getInstance().findFileByPath(str + JarFileSystem.JAR_SEPARATOR))
        .filter(Objects::nonNull)
        .map(file -> new JarFileScope(file))
        .toArray(GlobalSearchScope[]::new);
      searchScope = scopes.length == 0 ? searchScope : GlobalSearchScope.union(ArrayUtil.append(scopes, searchScope));
    }
    mySearchScope = searchScope;
  }

  GlobalSearchScope getSearchScope() {
    return mySearchScope;
  }

  @Override
  public @NotNull String getPresentableText() {
    return getCanonicalText();
  }

  @Override
  public @NotNull String getCanonicalText() {
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
  public PsiType @NotNull [] getSuperTypes() {
    return EMPTY_ARRAY;
  }
}