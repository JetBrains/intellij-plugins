package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NonNls;

public class StyleTest extends MxmlWriterTestBase {
  @Override
  protected String getBasePath() {
    return "/css";
  }
  
  protected boolean useRawProjectRoot() {
    return getName().equals("testComponentWithCustomSkinInPackage") || getName().equals("testComponentWithCustomSkinAsBinding");
  }
  
    @Flex(version="4.5")
  public void testLibraryCssDefaults() throws Exception {
    testFile("empty.mxml");
  }
  
    @Flex(version="4.5", requireLocalStyleHolder=true)
  public void testStyleTag() throws Exception {
    testFile("StyleTag.mxml");
  }
  
    @Flex(version="4.1", requireLocalStyleHolder=true)
  // see mx.controls.ButtonBar line 528 in flex sdk 4.1
  public void testMxButtonBar41WithLocalStyleHolder() throws Exception {
    testFile("../mxml/Form.mxml", "StyleTag.mxml");
  }
  
  @Flex(version="4.5", requireLocalStyleHolder=true)
  public void testStyleTagWithSource() throws Exception {
    testFile("StyleTagWithSource.mxml", "externalCss.css");
  }
  
  @Flex(version="4.5")
  public void testComponentWithCustomSkin() throws Exception {
    testFile("ComponentWithCustomSkin.mxml", "CustomSkin.mxml");
  }
  
  @Flex(version="4.5")
  public void testComponentWithCustomSkinInPackage() throws Exception {
    testFile("ComponentWithCustomSkinInPackage.mxml", "testPackage/CustomSkinInPackage.mxml");
  }
  
  @Flex(version="4.5")
  public void testComponentWithCustomSkinAsBinding() throws Exception {
    testFile("ComponentWithCustomSkinAsBinding.mxml", "testPackage/CustomSkinInPackage.mxml");
  }
  
  @Flex(version="4.5", requireLocalStyleHolder=true)
  public void testLibraryWithDefaultsCss() throws Exception {
    testFile("LibraryWithDefaultsCss.mxml", "defaults.css");
  }
  
  @Override
  protected Module createModule(@NonNls final String moduleName) {
    Module module = super.createModule(moduleName);
    if (getName().equals("testLibraryWithDefaultsCss")) {
      FlexBuildConfiguration.getInstance(module).OUTPUT_TYPE = FlexBuildConfiguration.LIBRARY;
    }
    return module;
  }
}