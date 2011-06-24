package com.intellij.javascript.flex.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveDirectoryWithClassesHelper;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FlexMoveDirectoryWithClassesHelper extends MoveDirectoryWithClassesHelper.Default {

  @Override
  public void findUsages(Collection<PsiFile> filesToMove,
                         PsiDirectory[] directoriesToMove,
                         Collection<UsageInfo> result,
                         boolean searchInComments,
                         boolean searchInNonJavaFiles,
                         Project project) {
    for (PsiFile file : filesToMove) {
      for (PsiReference reference : ReferencesSearch.search(file)) {
        result.add(new MyUsageInfo(reference, file));
      }
    }
  }

  @Override
  public void postProcessUsages(UsageInfo[] usages) {
    for (UsageInfo usage : usages) {
      if (usage instanceof MyUsageInfo) {
        PsiReference reference = usage.getReference();
        if (reference != null) {
          reference.bindToElement(((MyUsageInfo)usage).myFile);
        }
      }
    }
  }

  private static class MyUsageInfo extends UsageInfo {
    private final PsiFile myFile;

    public MyUsageInfo(@NotNull PsiReference reference, PsiFile file) {
      super(reference);
      myFile = file;
    }
  }
}
