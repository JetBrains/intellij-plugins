package com.jetbrains.lang.dart.completion.reference;

import com.jetbrains.lang.dart.util.DartHtmlUtil;

import java.io.IOException;

/**
 * @author: Fedor.Korotkov
 */
public class ReferenceCompletionInHtmlTest extends ReferenceCompletionInLibraryRootTest {
  @Override
  protected void configure(String... files) throws IOException {
    DartHtmlUtil.createHtmlAndConfigureFixture(myFixture, files);
  }
}
