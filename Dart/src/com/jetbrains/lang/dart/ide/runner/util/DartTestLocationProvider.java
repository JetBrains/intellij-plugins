package com.jetbrains.lang.dart.ide.runner.util;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartTestLocationProvider implements SMTestLocator, DumbAware {
  private static final List<Location> NONE = Collections.emptyList();

  public static final DartTestLocationProvider INSTANCE = new DartTestLocationProvider();

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocol,
                                    @NotNull String path,
                                    @NotNull Project project,
                                    @NotNull GlobalSearchScope scope) {
    ///Users/x/projects/foo/test/foo_test.dart,main tests/calculate_fail

    final List<String> elements = StringUtil.split(path, ",");
    if (elements.size() != 2) {
      return NONE;
    }

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(elements.get(0));
    if (file == null) {
      return NONE;
    }

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    return getLocation(project, pathToNodes(elements.get(1)), psiFile);
  }

  private static List<String> pathToNodes(final String element) {
    return StringUtil.split(element, "/");
  }

  @VisibleForTesting
  public List<Location> getLocation(@NotNull String testPath, final PsiFile psiFile) {
    return getLocation(psiFile.getProject(), pathToNodes(testPath), psiFile);
  }

  protected List<Location> getLocation(@NotNull final Project project, final List<String> nodes, final PsiFile psiFile) {

    final List<Location> locations = new ArrayList<Location>();

    if (psiFile instanceof DartFile) {

      PsiElementProcessor<PsiElement> collector = new PsiElementProcessor<PsiElement>() {

        @Override
        public boolean execute(@NotNull final PsiElement element) {
          if (element instanceof DartCallExpression) {
            DartCallExpression expression = (DartCallExpression)element;
            if (TestUtil.isTest(expression) || TestUtil.isGroup(expression)) {
              if (nodes.get(nodes.size() - 1).equals(getTestLabel(expression))) {
                boolean matches = true;
                for (int i = nodes.size() - 2; i >= 0 && matches; --i) {
                  expression = getGroup(expression);
                  if (expression == null || !nodes.get(i).equals(getTestLabel(expression))) {
                    matches = false;
                  }
                }
                if (matches) {
                  locations.add(new PsiLocation<PsiElement>(project, element));
                  return false;
                }
              }
            }
          }

          return true;
        }

        @Nullable
        private DartCallExpression getGroup(final DartCallExpression expression) {
          return (DartCallExpression)PsiTreeUtil.findFirstParent(expression, true, new Condition<PsiElement>() {
            @Override
            public boolean value(final PsiElement element) {
              return element instanceof DartCallExpression && TestUtil.isGroup((DartCallExpression)element);
            }
          });
        }
      };

      PsiTreeUtil.processElements(psiFile, collector);
    }

    return locations;
  }

  @Nullable
  public static String getTestLabel(@NotNull final DartCallExpression testCallExpression) {
    final DartArguments arguments = testCallExpression.getArguments();
    final DartArgumentList argumentList = arguments == null ? null : arguments.getArgumentList();
    final List<DartExpression> argExpressions = argumentList == null ? null : argumentList.getExpressionList();
    return argExpressions != null && !argExpressions.isEmpty() && argExpressions.get(0) instanceof DartStringLiteralExpression ? StringUtil
      .unquoteString(argExpressions.get(0).getText()) : null;
  }
}
