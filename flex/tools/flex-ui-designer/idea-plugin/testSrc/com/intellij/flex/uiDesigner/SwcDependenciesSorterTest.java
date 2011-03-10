package com.intellij.flex.uiDesigner;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import js.JSTestOptions;

import java.io.File;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class SwcDependenciesSorterTest extends MxmlWriterTestBase {
  @Override
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    super.modifySdk(sdk, sdkModificator);

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      addLibrary(sdkModificator, getTestDataPath() + "/spark_dmv_4.5.swc");
    }
    else {
      final String path = getTestDataPath() + "/ResolveToClassWithBiggestTimestamp/bin/";
      addLibrary(sdkModificator, path + "lib_1.swc", false);
      addLibrary(sdkModificator, path + "lib_2.swc", false);
    }
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testDeleteIfAllDefitionsHaveUnresolvedDependencies() throws Exception {
    assertEquals(-1, libs.size() - libraries.size());

    for (Library library : libraries) {
      if (library instanceof FilteredLibrary) {
        assertFalse(((FilteredLibrary) library).getOrigin().getPath().contains("spark_dmv"));
      }
      else if (library instanceof OriginalLibrary) {
        assertFalse(((OriginalLibrary) library).getPath().contains("spark_dmv"));
      }
    }
    
    testFile("Form.mxml");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    
    if (appRootDir != null && appRootDir.exists()) {
      for (File file : appRootDir.listFiles()) {
        if (!file.isHidden() && file.getPath().endsWith(".swf")) {
          //noinspection ResultOfMethodCallIgnored
          file.delete(); 
        }
      }
    }
  }

  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testResolveToClassWithBiggestTimestamp() throws Exception {
    testFile("Form.mxml");
  }
}