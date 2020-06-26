/*
 * Copyright 2014 The authors
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

package com.intellij.struts2.annotators;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.testFramework.UsefulTestCase.assertSameElements;
import static junit.framework.Assert.assertNotNull;

/**
 * Utils for gutter icon navigation tests.
 *
 * @author Yann C&eacute;bron
 */
final class AnnotatorTestUtils {

  private AnnotatorTestUtils() {
  }

  /**
   * Verifies the navigation targets' names of the gutter icon match.
   *
   * @param renderer            Gutter icon.
   * @param resultValueFunction Function to transform target to expected name.
   * @param expectedValues      Expected names.
   */
  static void checkGutterTargets(@NotNull final GutterMark renderer,
                                 @NotNull final Function<? super PsiElement, String> resultValueFunction,
                                 final String... expectedValues) {
    final LineMarkerInfo lineMarkerInfo = ((LineMarkerInfo.LineMarkerGutterIconRenderer) renderer).getLineMarkerInfo();
    final NavigationGutterIconRenderer navigationHandler = (NavigationGutterIconRenderer) lineMarkerInfo.getNavigationHandler();
    assertNotNull(navigationHandler);

    final List<PsiElement> targetElements = navigationHandler.getTargetElements();

    final Set<String> foundValues = new HashSet<>();
    for (final PsiElement psiElement : targetElements) {
      foundValues.add(resultValueFunction.fun(psiElement));
    }

    assertSameElements(foundValues, expectedValues);
  }

}