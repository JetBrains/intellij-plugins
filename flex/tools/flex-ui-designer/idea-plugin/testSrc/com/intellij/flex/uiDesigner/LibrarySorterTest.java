package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

@Flex(version="4.5")
public class LibrarySorterTest extends MxmlTestBase {
  @Override
  protected String generateSdkName(String version) {
    return getName();
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
      final String path = getTestDataPath() + "/ResolveToClassWithBiggestTimestamp/bin/";
      addLibrary(sdkModificator, path + "lib_1.swc");
      addLibrary(sdkModificator, path + "lib_2.swc");
    }

    Disposer.register(myModule, new Disposable() {
      @Override
      public void dispose() {
        final AccessToken token = WriteAction.start();
        try {
          ProjectJdkTable.getInstance().removeJdk(sdk);
        }
        finally {
          token.finish();
        }
      }
    });
  }

  @Override
  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir, List<String> libs) {
    super.modifyModule(model, rootDir, libs);

    if (getName().equals("testOverlappingContent")) {
      final String path = "/Users/develar/Downloads/project/";
      libs.add(path + "flexunit-4.1.0-8-flex_4.1.0.16076.swc");
      libs.add(path + "FlexUnit1Lib.swc");
    }
    else if (getName().equals("testMoveFlexSdkLibToSdkLibsIfNot")) {
      libs.add(flexSdkRootPath + "/framework.swc");
      libs.add("MinimalComps_0_9_10.swc");
      libs.add(getFudHome() + "/test-data-helper/target/test-data-helper.swc");
    }
  }

  @Flex(version="4.1")
  public void testDelete() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  public void testMoveFlexSdkLibToSdkLibsIfNot() throws Exception {
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
    testFile(SPARK_COMPONENTS_FILE);
  }
}