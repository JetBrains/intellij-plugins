package com.jetbrains.lang.dart.ide.runner.unittest;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
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
            if (isLabeled(expression, "test")) {
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

        private DartCallExpression getGroup(final DartCallExpression expression) {
          return (DartCallExpression)PsiTreeUtil.findFirstParent(expression, true, new Condition<PsiElement>() {
            @Override
            public boolean value(final PsiElement element) {
              return element instanceof DartCallExpression && isLabeled((DartCallExpression)element, "group");
            }
          });
        }

        private boolean nameMatches(final DartCallExpression expression, final String name) {
          final DartArguments args = expression.getArguments();
          final DartArgumentList argumentList = args.getArgumentList();
          if (argumentList != null) {
            final PsiElement firstArg = argumentList.getFirstChild();
            if (firstArg instanceof DartStringLiteralExpression) {
              // skip the quote
              final PsiElement textElement = firstArg.getFirstChild().getNextSibling();
              if (textElement != null) {
                return textElement.getText().equals(name);
              }
            }
          }
          return false;
        }

        private boolean isLabeled(final DartCallExpression element, String label) {
          final PsiElement child = element.getFirstChild();
          return child != null && child.getText().equals(label);
        }
      };

      PsiTreeUtil.processElements(psiFile, collector);
    }

    return locations;
  }
}
