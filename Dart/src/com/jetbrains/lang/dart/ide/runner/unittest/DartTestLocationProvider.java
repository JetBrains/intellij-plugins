package com.jetbrains.lang.dart.ide.runner.unittest;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.TestLocationProvider;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartTestLocationProvider implements TestLocationProvider {

  private static final List<Location> NONE = new ArrayList<Location>();

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {

    if (project == null) {
      return NONE;
    }

    ///Users/x/projs/foo/test/foo_test.dart,main tests/calculate_fail

    final String[] elements = locationData.split(",");
    if (elements.length != 2) {
      return NONE;
    }

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(elements[0]);
    if (file == null) {
      return NONE;
    }

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    final String[] pathNodes = pathToNodes(elements[1]);
    return getLocation(project, pathNodes, psiFile);
  }

  private static String[] pathToNodes(final String element) {
    return element.split("/");
  }

  @VisibleForTesting
  public List<Location> getLocation(@NotNull String testPath, final PsiFile psiFile) {
    return getLocation(psiFile.getProject(), pathToNodes(testPath), psiFile);
  }

  protected List<Location> getLocation(@NotNull final Project project, final String[] nodes, final PsiFile psiFile) {

    final List<Location> locations = new ArrayList<Location>();

    if (psiFile instanceof DartFile) {

      PsiElementProcessor<PsiElement> collector = new PsiElementProcessor<PsiElement>() {

        @Override
        public boolean execute(@NotNull final PsiElement element) {

          if (element instanceof DartCallExpression) {
            DartCallExpression expression = (DartCallExpression)element;
            if (DartUnitRunConfigurationProducer.isTest(expression)) {
              if (nameMatches(expression, nodes[nodes.length - 1])) {
                boolean matches = true;
                for (int i = nodes.length - 2; i >= 0 && matches; --i) {
                  expression = getGroup(expression);
                  if (expression == null || !nameMatches(expression, nodes[i])) {
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
              return element instanceof DartCallExpression && DartUnitRunConfigurationProducer.isGroup((DartCallExpression)element);
            }
          });
        }

        private boolean nameMatches(final DartCallExpression expression, final String name) {
          final DartArgumentList argumentList = expression.getArguments().getArgumentList();
          final List<DartExpression> argExpressions = argumentList == null ? null : argumentList.getExpressionList();
          return argExpressions != null &&
                 !argExpressions.isEmpty() &&
                 argExpressions.get(0) instanceof DartStringLiteralExpression &&
                 name.equals(StringUtil.unquoteString(argExpressions.get(0).getText()));
        }
      };

      PsiTreeUtil.processElements(psiFile, collector);
    }

    return locations;
  }
}
