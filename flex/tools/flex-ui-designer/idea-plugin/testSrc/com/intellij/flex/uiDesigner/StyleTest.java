package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NonNls;

@Flex(version="4.5")
public class StyleTest extends MxmlWriterTestBase {
  @Override
  protected String getBasePath() {
    return "/css";
  }
  
  protected boolean useRawProjectRoot() {
    return getName().equals("testComponentWithCustomSkin");
  }
  
  public void testLibraryCssDefaults() throws Exception {
    testFile("emptyForCheckLibrariesCssDefaults.mxml");
  }
  
  @Flex(requireLocalStyleHolder=true)
  public void testStyleTag() throws Exception {
    testFile("StyleTag.mxml");
  }
  
  @Flex(version="4.1", requireLocalStyleHolder=true)
  // see mx.controls.ButtonBar line 528 in flex sdk 4.1
  public void testMxButtonBar41WithLocalStyleHolder() throws Exception {
    testFile("../mxml/mx/MxComponents.mxml", "StyleTag.mxml");
  }
  
  @Flex(requireLocalStyleHolder=true)
  public void testStyleTagWithSource() throws Exception {
    testFile("StyleTagWithSource.mxml", "externalCss.css");
  }
  
  public void testComponentWithCustomSkin() throws Exception {
    testFiles(new String[]{"ComponentWithCustomSkin.mxml", "ComponentWithCustomSkinInPackage.mxml", "ComponentWithCustomSkinAsBinding.mxml"}, "CustomSkin.mxml", "testPackage/CustomSkinInPackage.mxml");
  }
  
  @Flex(requireLocalStyleHolder=true)
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