// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.javascript.web.symbols.WebSymbol;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static org.angular2.modules.Angular2TestModule.*;

@TestDataPath("$R#_COMPLETION_TEST_ROOT/Angular2")
public class Angular2CodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  private static final Set<String> TESTS_TO_SKIP = ContainerUtil.newHashSet(
    "test004", // improve ngFor completions
    "external/test004"
  );

  private static final Set<String> HIGH_PRIORITY_ONLY = ContainerUtil.newHashSet(
    "external/test003"
  );

  private static final Set<String> CAMEL_CASE_MATCH_ONLY = ContainerUtil.newHashSet(
    "external/test002",
    "external/test004",
    "external/test006"
  );

  @Override
  protected boolean shouldSkipItem(@NotNull LookupElement element) {
    if (element.getLookupString().startsWith("[(")) {
      return true;
    }
    if (HIGH_PRIORITY_ONLY.contains(getName())) {
      return !(element instanceof PrioritizedLookupElement)
             || ((PrioritizedLookupElement<?>)element).getPriority() < WebSymbol.Priority.HIGH.getValue();
    }
    if (CAMEL_CASE_MATCH_ONLY.contains(getName())) {
      PsiElement el = myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1);
      String prefix = el.getText();
      if (new CamelHumpMatcher(prefix).matchingDegree(element.getLookupString()) < 800) {
        return true;
      }
    }
    return super.shouldSkipItem(element);
  }

  @Override
  protected boolean isExcluded() {
    return TESTS_TO_SKIP.contains(getName());
  }

  @Override
  protected void doSingleTest(@NotNull String testFile, @NotNull String path) throws Exception {
    if (getName().startsWith("external")) {
      configureLink(
        myFixture,
        ANGULAR_COMMON_4_0_0,
        ANGULAR_CORE_4_0_0,
        ANGULAR_PLATFORM_BROWSER_4_0_0,
        ANGULAR_ROUTER_4_0_0,
        ANGULAR_FORMS_4_0_0,
        IONIC_ANGULAR_3_0_1);
    } else {
      configureLink(myFixture);
    }
    super.doSingleTest(testFile, path);
  }

  @Override
  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = super.doGetExtraFiles();
    if (getName().startsWith("external")) {
      extraFiles.add("external/module.ts");
    }
    return extraFiles;
  }
}
