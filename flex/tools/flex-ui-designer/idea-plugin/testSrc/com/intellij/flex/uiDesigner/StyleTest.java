package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;

@Flex(version="4.5")
public class StyleTest extends MxmlTestBase {
  @Override
  protected String getBasePath() {
    return "/css";
  }
  
  public void testLibraryCssDefaults() throws Exception {
    testFile("emptyForCheckLibrariesCssDefaults.mxml");
  }

  @Override
  protected void assertAfterInitLibrarySets(XmlFile[] unregisteredDocumentReferences) throws IOException {
    super.assertAfterInitLibrarySets(unregisteredDocumentReferences);

    if (getName().equals("testStyleTag")) {
      final ProblemsHolder problemsHolder = new ProblemsHolder();
      client.registerDocumentReferences(unregisteredDocumentReferences, myModule, problemsHolder);
      assertTrue(problemsHolder.isEmpty());
    }
  }
  
  @Flex(requireLocalStyleHolder=true, rawProjectRoot=true)
  public void testStyleTag() throws Exception {
    testFile("StyleTag.mxml", "testPackage/CustomSkinInPackage.mxml");
  }
  
  @Flex(version="4.1", requireLocalStyleHolder=true)
  // see mx.controls.ButtonBar line 528 in flex sdk 4.1
  public void testMxButtonBar41WithLocalStyleHolder() throws Exception {
    // must be tested with local style holder
    testFile("../mxml/mx/MxComponents.mxml", "StyleTagWithSource.mxml", "externalCss.css");
  }
  
  @Flex(requireLocalStyleHolder=true)
  public void testStyleTagWithSource() throws Exception {
    testFile("StyleTagWithSource.mxml", "externalCss.css");
  }

  @Flex(requireLocalStyleHolder = true)
  public void testStyleTagWithSourceAsRelativePath() throws Exception {
    testFile("StyleTagWithSourceAsRelativePath.mxml", "externalCss.css");
  }

  @Flex(requireLocalStyleHolder=true)
  public void testApplicationLevelGlobalSelector() throws Exception {
    testFile("ApplicationLevelGlobalSelector.mxml");
  }

  @Flex(rawProjectRoot=true)
  public void testComponentWithCustomSkin() throws Exception {
    testFiles(new String[]{"ComponentWithCustomSkin.mxml", "ComponentWithCustomSkinInPackage.mxml", "ComponentWithCustomSkinAsBinding.mxml"}, "CustomSkin.mxml", "AuxMyButtonSkin.mxml", "testPackage/CustomSkinInPackage.mxml");
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