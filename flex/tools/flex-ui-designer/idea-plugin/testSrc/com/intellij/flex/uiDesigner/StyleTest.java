package com.intellij.flex.uiDesigner;

import js.JSTestOptions;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class StyleTest extends MxmlWriterTestBase {
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testLibraryCssDefaults() throws Exception {
    testFile("css/empty.mxml");
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5", requireLocalStyleHolder=true)
  public void testStyleTag() throws Exception {
    testFile("css/StyleTag.mxml");
  }
}