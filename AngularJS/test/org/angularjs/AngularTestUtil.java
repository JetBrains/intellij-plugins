// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.intellij.lang.javascript.completion.JSCompletionUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webSymbols.WebTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.intellij.lang.javascript.completion.JSLookupPriority.*;
import static com.intellij.testFramework.UsefulTestCase.assertInstanceOf;
import static junit.framework.TestCase.assertEquals;
import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.PROP_ERROR_SYMBOL;

public final class AngularTestUtil {

  public static String getBaseTestDataPath(Class<?> clazz) {
    String contribPath = getContribPath();
    return contribPath + "/AngularJS/test/" + clazz.getPackage().getName().replace('.', '/') + "/data/";
  }

  public static String getBaseTestExDataPath(Class<?> clazz) {
    String contribPath = getContribPath();
    return contribPath + "/AngularJS/test-ex/" + clazz.getPackage().getName().replace('.', '/') + "/data/";
  }

  private static String getContribPath() {
    final String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (new File(homePath, "contrib/.gitignore").isFile()) {
      return homePath + File.separatorChar + "contrib";
    }
    return homePath;
  }

  public static String getDirectiveDefinitionText(PsiElement resolve) {
    return ObjectUtils.notNull(PsiTreeUtil.getParentOfType(resolve, ES6Decorator.class), resolve.getParent()).getText();
  }

  @SuppressWarnings("unchecked")
  public static <T extends JSElement> T checkVariableResolve(final String signature,
                                                             final String varName,
                                                             final Class<T> varClass,
                                                             @NotNull CodeInsightTestFixture fixture) {
    PsiElement resolve = resolveReference(signature, fixture);
    assertInstanceOf(resolve, varClass);
    assertEquals(varName, varClass.cast(resolve).getName());
    return (T)resolve;
  }

  public static void enableAstLoadingFilter(@NotNull UsefulTestCase testCase) {
    WebTestUtil.enableAstLoadingFilter(testCase);
  }

  public static void moveToOffsetBySignature(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    WebTestUtil.moveToOffsetBySignature(fixture, signature);
  }

  public static int findOffsetBySignature(String signature, final PsiFile psiFile) {
    return WebTestUtil.findOffsetBySignature(psiFile, signature);
  }

  @NotNull
  public static PsiElement resolveReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    return WebTestUtil.resolveReference(fixture, signature);
  }

  public static void assertUnresolvedReference(@NotNull String signature, @NotNull CodeInsightTestFixture fixture) {
    var symbols = WebTestUtil.multiResolveWebSymbolReference(fixture, signature);
    if (!symbols.isEmpty() && ContainerUtil.and(symbols, s -> s.getProperties().get(PROP_ERROR_SYMBOL) == Boolean.TRUE)) {
      return;
    }
    WebTestUtil.assertUnresolvedReference(fixture, signature);
  }

  public static List<String> renderLookupItems(@NotNull CodeInsightTestFixture fixture,
                                               boolean renderPriority,
                                               boolean renderTypeText) {
    return renderLookupItems(fixture, renderPriority, renderTypeText, false);
  }

  public static List<String> renderLookupItems(@NotNull CodeInsightTestFixture fixture,
                                               boolean renderPriority,
                                               boolean renderTypeText,
                                               boolean filterOutGlobalSymbols) {
    return renderLookupItems(fixture, renderPriority, renderTypeText, false, filterOutGlobalSymbols);
  }

  public static List<String> renderLookupItems(@NotNull CodeInsightTestFixture fixture,
                                               boolean renderPriority,
                                               boolean renderTypeText,
                                               boolean renderTailText,
                                               boolean filterOutGlobalSymbols) {
    return WebTestUtil.renderLookupItems(fixture, renderPriority,
                                         renderTypeText, renderTailText, false, false, lookupElement -> {
        if (!filterOutGlobalSymbols || "$any".equals(lookupElement.getLookupString())) {
          return true;
        }
        var priority = (int)JSCompletionUtil.getLookupElementPriority(lookupElement);
        return priority != NON_CONTEXT_KEYWORDS_PRIORITY.getPriorityValue()
               && priority != KEYWORDS_PRIORITY.getPriorityValue()
               && priority != TOP_LEVEL_SYMBOLS_FROM_OTHER_FILES.getPriorityValue()
               && priority != 0;
      });
  }
}
