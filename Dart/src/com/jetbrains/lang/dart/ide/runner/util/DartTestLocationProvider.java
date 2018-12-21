// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartTestLocationProvider implements SMTestLocator, DumbAware {
  private static final List<Location> NONE = Collections.emptyList();
  private static final Gson GSON = new Gson();

  public static final DartTestLocationProvider INSTANCE = new DartTestLocationProvider();
  public static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocol,
                                    @NotNull String path,
                                    @NotNull Project project,
                                    @NotNull GlobalSearchScope scope) {
    // see DartTestEventsConverter.addLocationHint()
    // path is like /Users/x/projects/foo/test/foo_test.dart,35,12,["main tests","calculate_fail"]

    int commaIdx1 = path.indexOf(',');
    int commaIdx2 = path.indexOf(',', commaIdx1 + 1);
    int commaIdx3 = path.indexOf(',', commaIdx2 + 1);
    if (commaIdx3 < 0) return NONE;

    final String filePath = path.substring(0, commaIdx1);
    final int line = Integer.parseInt(path.substring(commaIdx1 + 1, commaIdx2));
    final int column = Integer.parseInt(path.substring(commaIdx2 + 1, commaIdx3));
    final String names = path.substring(commaIdx3 + 1);

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    final PsiFile psiFile = file == null ? null : PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof DartFile)) return NONE;

    if (line >= 0 && column >= 0) {
      final Location<PsiElement> location = getLocationByLineAndColumn(psiFile, line, column);
      if (location != null) {
        return Collections.singletonList(location);
      }
    }

    final List<String> nodes = pathToNodes(names);
    if (nodes.isEmpty()) {
      return Collections.singletonList(new PsiLocation<PsiElement>(psiFile));
    }

    return getLocationByGroupAndTestNames(psiFile, nodes);
  }

  @Nullable
  private static Location<PsiElement> getLocationByLineAndColumn(@NotNull final PsiFile file, final int line, final int column) {
    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    if (document == null) return null;
    if (line >= document.getLineCount()) return null;

    final int offset = document.getLineStartOffset(line) + column;
    final PsiElement element = file.findElementAt(offset);
    final PsiElement parent1 = element == null ? null : element.getParent();
    final PsiElement parent2 = parent1 instanceof DartId ? parent1.getParent() : null;
    final PsiElement parent3 = parent2 instanceof DartReferenceExpression ? parent2.getParent() : null;
    if (parent3 instanceof DartCallExpression) {
      if (TestUtil.isTest((DartCallExpression)parent3) || TestUtil.isGroup((DartCallExpression)parent3)) {
        return new PsiLocation<>(parent3);
      }
    }
    return null;
  }

  private static List<String> pathToNodes(final String element) {
    return GSON.fromJson(element, STRING_LIST_TYPE);
  }

  @VisibleForTesting
  public List<Location> getLocationForTest(@NotNull final PsiFile psiFile, @NotNull final String testPath) {
    return getLocationByGroupAndTestNames(psiFile, pathToNodes(testPath));
  }

  protected List<Location> getLocationByGroupAndTestNames(final PsiFile psiFile, final List<String> nodes) {
    final List<Location> locations = new ArrayList<>();

    if (psiFile instanceof DartFile && !nodes.isEmpty()) {
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
                  locations.add(new PsiLocation<>(element));
                  return false;
                }
              }
            }
          }

          return true;
        }

        @Nullable
        private DartCallExpression getGroup(final DartCallExpression expression) {
          return (DartCallExpression)PsiTreeUtil.findFirstParent(expression, true,
                                                                 element -> element instanceof DartCallExpression &&
                                                                            TestUtil.isGroup((DartCallExpression)element));
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
    return argExpressions != null && !argExpressions.isEmpty() && argExpressions.get(0) instanceof DartStringLiteralExpression
           ? StringUtil.unquoteString(argExpressions.get(0).getText())
           : null;
  }
}
