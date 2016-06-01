package com.jetbrains.lang.dart.completion.handler;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.Nullable;

public abstract class CompletionHandlerTestBase extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  protected void doTest() {
    doTest(CompletionType.BASIC, 1, null);
  }

  protected void doTest(CompletionType type, int invocationCount, @Nullable final String toComplete) {
    configure();
    myFixture.complete(type, invocationCount);
    final LookupEx lookup = LookupManager.getActiveLookup(myFixture.getEditor());
    assertFalse(toComplete != null && lookup == null);
    if (lookup != null && toComplete != null) {
      final LookupElement[] elements = myFixture.getLookupElements();
      assertNotNull("no lookups", elements);
      final LookupElement element = ContainerUtil.find(elements, element1 -> StringUtil.equals(element1.getLookupString(), toComplete));
      assertNotNull("Can't find '" + toComplete + "' variant", element);
      lookup.setCurrentItem(element);
    }
    if (lookup != null) {
      myFixture.type('\n');
    }
    myFixture.checkResultByFile(getTestName(true) + ".after" + getTestFileExtension());
  }

  protected void configure() {
    myFixture.configureByFile(getTestName(true) + getTestFileExtension());
  }

  protected abstract String getTestFileExtension();
}
