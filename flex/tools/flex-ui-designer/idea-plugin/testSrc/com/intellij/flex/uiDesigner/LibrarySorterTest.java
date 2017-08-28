package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import org.jetbrains.annotations.NotNull;

@Flex(version="4.5")
public class LibrarySorterTest extends MxmlTestBase {
  @Override
  protected String generateSdkName(String version) {
    return getName();
  }

  @NotNull
  @Override
  protected Disposable getSdkParentDisposable() {
    return myModule;
  }

  @Override
  protected void modifySdk(final Sdk sdk, SdkModificator sdkModificator) {
    // must be added before super (i. e. before framework.swc)
    if (getName().equals("testDelete")) {
      addLibrary(sdkModificator, "flash-integration_4.1.swc");
    }
    else if (getName().equals("testIgnoreSwcWithoutLibraryFile")) {
      addLibrary(sdkModificator, "swcWithoutLibrarySwf.swc");
    }

    //noinspection ConstantConditions
    sdkModificator.removeRoot(sdk.getHomeDirectory().findFileByRelativePath("frameworks/libs/framework.swc"), OrderRootType.CLASSES);

    super.modifySdk(sdk, sdkModificator);

    if (getName().equals("testResolveToClassWithBiggestTimestamp")) {
      final String path = DesignerTests.getTestDataPath() + "/ResolveToClassWithBiggestTimestamp/bin/";
      addLibrary(sdkModificator, path + "lib_1.swc");
      addLibrary(sdkModificator, path + "lib_2.swc");
    }
  }

  @Flex(version="4.1")
  public void testDelete() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  public void testMoveFlexSdkLibToSdkLibsIfNot() throws Exception {
    moduleInitializer = (model, file, libs1) -> {
      libs1.add(flexSdkRootPath + "/frameworks/libs/framework.swc");
      libs1.add("MinimalComps_0_9_10.swc");
      libs1.add(DebugPathManager.resolveTestArtifactPath("test-data-helper.swc"));
      return null;
    };

    testFile("GenericMxmlSupport.mxml");
  }

  public void testResolveToClassWithBiggestTimestamp() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  public void testIgnoreSwcWithoutLibraryFile() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  // AS-235
  public void testOverlappingContent() throws Exception {
    moduleInitializer = (model, file, libs1) -> {
      libs1.add("flexunit-4.1.0-8-flex_4.1.0.16076.swc");
      libs1.add("FlexUnit1Lib.swc");
      return null;
    };

    testFile(SPARK_COMPONENTS_FILE);
  }
}