package com.jetbrains.lang.dart.ide.runner.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TestModel {
  private final VirtualFile myFile;
  private final Set<String> myGroups = new THashSet<String>();
  private final Set<String> myTests = new THashSet<String>();

  public TestModel(@NotNull final Project project, @NotNull final VirtualFile file) {
    myFile = file;

    final PsiFile testFile = PsiManager.getInstance(project).findFile(file);
    if (testFile != null) {
      PsiElementProcessor<PsiElement> collector = new PsiElementProcessor<PsiElement>() {
        @Override
        public boolean execute(@NotNull final PsiElement element) {
          if (element instanceof DartCallExpression) {
            DartCallExpression expression = (DartCallExpression)element;
            if (TestUtil.isTest(expression)) {
              myTests.add(DartTestLocationProvider.getTestLabel(expression));
            }
            else if (TestUtil.isGroup(expression)) {
              myGroups.add(DartTestLocationProvider.getTestLabel(expression));
            }
          }
          return true;
        }
      };

      PsiTreeUtil.processElements(testFile, collector);
    }
  }

  public boolean includes(@NotNull final Scope scope, @NotNull final String testLabel) {
    return scope == Scope.METHOD ? myTests.contains(testLabel) : myGroups.contains(testLabel);
  }

  public boolean appliesTo(final VirtualFile file) {
    return myFile.equals(file);
  }
}
