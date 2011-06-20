/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package com.intellij.flexIde;

import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiBundle;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class PsiPackageFactoryImpl extends PsiDirectoryFactory {
  private final PsiManagerImpl myManager;

  public PsiPackageFactoryImpl(final PsiManagerImpl manager) {
    myManager = manager;
  }

  public PsiDirectory createDirectory(final VirtualFile file) {
    return new PsiDirectoryImpl(myManager, file);
  }

  @NotNull
  public String getQualifiedName(@NotNull final PsiDirectory directory, final boolean presentable) {
    String packageName = DirectoryIndex.getInstance(directory.getProject()).getPackageName(directory.getVirtualFile());
    if (packageName != null) {
      if (packageName.length() > 0) return packageName;
      if (presentable) {
        return PsiBundle.message("default.package.presentation") + " (" + directory.getVirtualFile().getPresentableUrl() + ")";
      }
      return "";
    }
    return presentable ? ProjectUtil.getLocationRelativeToUserHome(directory.getVirtualFile().getPresentableUrl()) : "";
  }

  @Override
  public boolean isPackage(PsiDirectory directory) {
    return ProjectRootManager.getInstance(myManager.getProject()).getFileIndex().getPackageNameByDirectory(directory.getVirtualFile()) !=
           null;
  }

  public boolean isValidPackageName(String name) {
    return !StringUtil.isEmptyOrSpaces(name);
  }
}
