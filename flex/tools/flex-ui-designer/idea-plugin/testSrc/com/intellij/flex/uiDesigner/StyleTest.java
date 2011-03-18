package com.intellij.flex.uiDesigner;

import js.JSTestOptions;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class StyleTest extends MxmlWriterTestBase {
  @Override
  protected String getBasePath() {
    return "/css";
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testLibraryCssDefaults() throws Exception {
    testFile("empty.mxml");
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5", requireLocalStyleHolder=true)
  public void testStyleTag() throws Exception {
    testFile("StyleTag.mxml");
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5", requireLocalStyleHolder=true)
  public void testStyleTagWithSource() throws Exception {
    testFile("StyleTagWithSource.mxml", "externalCss.css");
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testComponentWithCustomSkin() throws Exception {
    testFile("ComponentWithCustomSkin.mxml", "CustomSkin.mxml");
  }
}