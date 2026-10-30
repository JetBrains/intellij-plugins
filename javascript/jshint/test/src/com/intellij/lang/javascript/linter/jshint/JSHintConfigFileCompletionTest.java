package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileType;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.JSCompletionFixture.checkWeHaveInCompletion;
import static com.intellij.lang.javascript.JSCompletionFixture.jsCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergey Simonchik
 */
@TestApplication
public class JSHintConfigFileCompletionTest {
  private final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSCompletionFixture> js =
    jsCompletionFixture(projectFixture, new JSFixtureOptions(JSHintTestUtil.BASE_TEST_DATA_PATH + "/config/completion/", JSHintConfigFileType.INSTANCE.getDefaultExtension()));

  @Test
  public void testStartNewOptionName() {
    js.get().doTest("");
    List<String> variants = doubleQuoteList(getAllKeyCompletionVariants());
    assertCompletionVariants(variants);
  }

  @Test
  public void testStartNewOptionNameInStringLiteral() {
    js.get().doTest("");
    List<String> variants = getAllKeyCompletionVariants();
    assertCompletionVariants(variants);
  }

  @Test
  public void testBooleanOptionValue() {
    js.get().doTest("");
    assertCompletionVariants(List.of("true", "false"));
  }

  @Test
  public void testQuotmarkOptionValue() {
    js.get().doTest("");
    List<String> variants = ContainerUtil.map(
      JSHintUtil.QUOTMARK_TYPE.getVariants(),
      variant -> variant.getValueAsJsonStr()
    );
    assertCompletionVariants(variants);
  }

  @Test
  public void testQuotmarkOptionValueInStringLiteral() {
    js.get().doTest("");
    List<String> variants = new ArrayList<>();
    for (OptionEnumVariant variant : JSHintUtil.QUOTMARK_TYPE.getVariants()) {
      if (variant.getValue() instanceof String) {
        variants.add(StringUtil.stripQuotesAroundValue(variant.getValueAsJsonStr()));
      }
    }
    assertCompletionVariants(variants);
  }

  private void assertCompletionVariants(@NotNull List<String> expectedVariants) {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    String[] expectedVariantsArray = ArrayUtilRt.toStringArray(expectedVariants);
    checkWeHaveInCompletion(myFixture.getLookupElements(), expectedVariantsArray);
    assertEquals(expectedVariantsArray.length, myFixture.getLookupElements().length);
  }

  private static List<String> getAllKeyCompletionVariants() {
    List<String> list = new ArrayList<>();
    for (JSHintOption option : JSHintOption.values()) {
      list.add(option.getKey());
      if (option.getKeyAlias() != null) {
        list.add(option.getKeyAlias());
      }
    }
    list.add(JSHintConfigFileUtil.EXTENDS_KEY);
    return list;
  }

  private static List<String> doubleQuoteList(@NotNull List<String> items) {
    return ContainerUtil.map(items, s -> StringUtil.wrapWithDoubleQuote(s));
  }
}
