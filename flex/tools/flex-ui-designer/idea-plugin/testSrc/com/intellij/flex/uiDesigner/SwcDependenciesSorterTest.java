package com.intellij.flex.uiDesigner;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;

import java.io.File;

public class SwcDependenciesSorterTest extends MxmlWriterTestBase {
  @Override
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    // must be added before super (i. e. before framework.swc)
    if (getName().equals("testDelete")) {
      addLibrary(sdkModificator, getTestDataPath() + "/flash-integration_4.1.swc");
    }
    
    super.modifySdk(sdk, sdkModificator);

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      addLibrary(sdkModificator, getTestDataPath() + "/spark_dmv_4.5.swc");
    }
    else if (getName().equals("testResolveToClassWithBiggestTimestamp")) {
      final String path = getTestDataPath() + "/ResolveToClassWithBiggestTimestamp/bin/";
      addLibrary(sdkModificator, path + "lib_1.swc", false);
      addLibrary(sdkModificator, path + "lib_2.swc", false);
    }
  }
  
  @Flex(version="4.5")
  public void testDeleteIfAllDefitionsHaveUnresolvedDependencies() throws Exception {
    runAdl();

    assertEquals(-1, libs.size() - libraries.size());

    for (Library library : libraries) {
      if (library instanceof OriginalLibrary) {
        assertFalse(((OriginalLibrary) library).getPath().contains("spark_dmv"));
      }
    }
    
    testFile("Form.mxml");
  }
  
  @Flex(version="4.1")
  public void testDelete() throws Exception {
//    assertEquals(-1, libs.size() - libraries.size());
    
    testFile("Form.mxml");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    
    if (appRootDir != null && appRootDir.exists()) {
      for (File file : appRootDir.listFiles()) {
        if (!file.isHidden() && file.getPath().endsWith(".swf") && !file.getPath().endsWith("/designer.swf")) {
          //noinspection ResultOfMethodCallIgnored
          file.delete(); 
        }
      }
    }
  }

  @Flex(version="4.5")
  public void testResolveToClassWithBiggestTimestamp() throws Exception {
    testFile("Form.mxml");
  }
}