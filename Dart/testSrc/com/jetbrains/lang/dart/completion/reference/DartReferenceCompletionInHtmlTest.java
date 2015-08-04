package com.jetbrains.lang.dart.completion.reference;

import com.jetbrains.lang.dart.util.DartHtmlUtil;

import java.io.IOException;

public abstract class DartReferenceCompletionInHtmlTest extends DartReferenceCompletionInLibraryRootTest {
  @Override
  protected void configure(String... files) throws IOException {
    DartHtmlUtil.createHtmlAndConfigureFixture(myFixture, files);
  }
}
