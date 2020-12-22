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

import com.intellij.ide.IdeView;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GaugeDataContext implements DataContext {
  private final DataContext dataContext;

  public GaugeDataContext(DataContext dataContext) {
    this.dataContext = dataContext;
  }

  @Nullable
  @Override
  public Object getData(@NonNls String dataId) {
    if (LangDataKeys.IDE_VIEW.is(dataId)) {
      return new MyIdeView(CommonDataKeys.PROJECT.getData(dataContext));
    }
    else {
      return dataContext.getData(dataId);
    }
  }

  private static class MyIdeView implements IdeView {
    private final Project project;

    private MyIdeView(Project project) {
      this.project = project;
    }

    @Override
    public void selectElement(PsiElement element) {
    }

    @Override
    public PsiDirectory[] getDirectories() {
      List<PsiDirectory> psiDirectories = new ArrayList<>();
      PsiManager psiManager = PsiManager.getInstance(project);
      for (VirtualFile root : ProjectRootManager.getInstance(psiManager.getProject()).getContentSourceRoots()) {
        PsiDirectory directory = psiManager.findDirectory(root);
        if (directory != null) {
          psiDirectories.add(directory);
        }
      }
      return psiDirectories.toArray(PsiDirectory.EMPTY_ARRAY);
    }

    @Nullable
    @Override
    public PsiDirectory getOrChooseDirectory() {
      return DirectoryChooserUtil.getOrChooseDirectory(this);
    }
  }
}
