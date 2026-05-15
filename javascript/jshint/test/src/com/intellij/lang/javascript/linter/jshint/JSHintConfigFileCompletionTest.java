package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileType;
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JSHintConfigFileCompletionTest extends BaseJSCompletionTestCase {

  @Override
  protected String getExtension() {
    return JSHintConfigFileType.INSTANCE.getDefaultExtension();
  }

  public void testStartNewOptionName() {
    doTest("");
    List<String> variants = doubleQuoteList(getAllKeyCompletionVariants());
    assertCompletionVariants(variants);
  }

  public void testStartNewOptionNameInStringLiteral() {
    doTest("");
    List<String> variants = getAllKeyCompletionVariants();
    assertCompletionVariants(variants);
  }

  public void testBooleanOptionValue() {
    doTest("");
    assertCompletionVariants(List.of("true", "false"));
  }

  public void testQuotmarkOptionValue() {
    doTest("");
    List<String> variants = ContainerUtil.map(
      JSHintUtil.QUOTMARK_TYPE.getVariants(),
      variant -> variant.getValueAsJsonStr()
    );
    assertCompletionVariants(variants);
  }

  public void testQuotmarkOptionValueInStringLiteral() {
    doTest("");
    List<String> variants = new ArrayList<>();
    for (OptionEnumVariant variant : JSHintUtil.QUOTMARK_TYPE.getVariants()) {
      if (variant.getValue() instanceof String) {
        variants.add(StringUtil.stripQuotesAroundValue(variant.getValueAsJsonStr()));
      }
    }
    assertCompletionVariants(variants);
  }

  private void assertCompletionVariants(@NotNull List<String> expectedVariants) {
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

  @Override
  protected String getTestDataPath() {
    return JSHintTestUtil.BASE_TEST_DATA_PATH + "/config/completion/";
  }

}
