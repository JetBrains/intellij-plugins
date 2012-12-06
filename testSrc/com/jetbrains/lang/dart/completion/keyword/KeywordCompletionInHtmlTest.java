package com.jetbrains.lang.dart.completion.keyword;

import com.jetbrains.lang.dart.util.DartHtmlUtil;

import java.io.IOException;

/**
 * @author: Fedor.Korotkov
 */
public class KeywordCompletionInHtmlTest extends KeywordCompletionTest {
  @Override
  protected void configure(String... files) throws IOException {
    DartHtmlUtil.createHtmlAndConfigureFixture(myFixture, files);
  }
}
