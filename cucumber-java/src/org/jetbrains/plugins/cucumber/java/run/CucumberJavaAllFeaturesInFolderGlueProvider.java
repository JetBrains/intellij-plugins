// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.function.Consumer;

public class CucumberJavaAllFeaturesInFolderGlueProvider implements CucumberGlueProvider {
  private final PsiDirectory myDirectory;

  public CucumberJavaAllFeaturesInFolderGlueProvider(@NotNull PsiDirectory directory) {
    myDirectory = directory;
  }

  @Override
  public void calculateGlue(@NotNull Consumer<String> consumer) {
    myDirectory.accept(new PsiElementVisitor() {
      @Override
      public void visitFile(@NotNull final PsiFile file) {
        if (file instanceof GherkinFile) {
          CucumberJavaUtil.calculateGlueFromGherkinFile((GherkinFile)file, consumer);
        }
      }

      @Override
      public void visitDirectory(@NotNull PsiDirectory dir) {
        ProgressManager.checkCanceled();
        for (PsiDirectory subDir : dir.getSubdirectories()) {
          subDir.accept(this);
        }

        for (PsiFile file : dir.getFiles()) {
          file.accept(this);
        }
      }
    });
    CucumberJavaUtil.calculateGlueFromHooksAndTypes(myDirectory, consumer);
  }
}
