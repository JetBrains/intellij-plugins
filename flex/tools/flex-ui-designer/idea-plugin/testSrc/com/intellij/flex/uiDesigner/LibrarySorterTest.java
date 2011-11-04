package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.libraries.LibrarySorter.FlexLibsNames;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;

import java.io.IOException;
import java.util.List;

@Flex(version="4.5")
public class LibrarySorterTest extends MxmlTestBase {
  @Override
  protected String generateSdkName(String version, boolean air) {
    return getName();
  }

  @Override
  protected void modifySdk(final Sdk sdk, SdkModificator sdkModificator) {
    Condition<String> condition = null;
    // must be added before super (i. e. before framework.swc)
    if (getName().equals("testDelete")) {
      addLibrary(sdkModificator, "flash-integration_4.1.swc");
    }
    else if (getName().equals("testIgnoreSwcWithoutLibraryFile")) {
      addLibrary(sdkModificator, "swcWithoutLibrarySwf.swc");
    }
    else if (getName().equals("testMoveFlexSdkLibToSdkLibsIfNot")) {
      condition = new Condition<String>() {
        @Override
        public boolean value(String name) {
          return !name.startsWith(FlexLibsNames.FRAMEWORK);
        }
      };
    }

    super.modifySdk(sdk, sdkModificator, condition);

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      addLibrary(sdkModificator, "spark_dmv_4.5.swc");
    }
    else if (getName().equals("testResolveToClassWithBiggestTimestamp")) {
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
  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir) {
    super.modifyModule(model, rootDir);

    if (getName().equals("testOverlappingContent")) {
      final String path = "/Users/develar/Downloads/project/";
      addLibrary(model, path + "flexunit-4.1.0-8-flex_4.1.0.16076.swc");
      addLibrary(model, path + "FlexUnit1Lib.swc");
    }
    else if (getName().equals("testMoveFlexSdkLibToSdkLibsIfNot")) {
      addLibrary(model, flexSdkRootPath + "/framework.swc");
      addLibrary(model, "MinimalComps_0_9_10.swc");
      addLibrary(model, getFudHome() + "/test-data-helper/target/test-data-helper.swc");
    }
  }

  @Override
  protected void assertAfterInitLibrarySets(XmlFile[] unregisteredDocumentReferences) throws IOException {
    super.assertAfterInitLibrarySets(unregisteredDocumentReferences);

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      //for (Library library : myLibraries()) {
      //  assertFalse(library.getPath().contains("spark_dmv"));
      //}
    }
  }

  private List<Library> myLibraries() {
    return client.getRegisteredModules().getInfo(myModule).flexLibrarySet.getLibraries();
  }

  public void testDeleteIfAllDefitionsHaveUnresolvedDependencies() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }
  
  @SuppressWarnings({"UnusedDeclaration"})
  @Flex(version="4.1")
  public void _TODO_testDelete() throws Exception {
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
  public void _testOverlappingContent() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      //if (appDir != null && appDir.exists()) {
      //  LibrarySet sdkLibrarySet = client.getRegisteredModules().getInfo(myModule).flexLibrarySet;
      //  for (LibrarySetItem item : myLibraries()) {
      //    //noinspection ResultOfMethodCallIgnored
      //    new File(appDir, item.library.getPath() + ".swf").delete();
      //  }
      //}
    }
    finally {
      super.tearDown();
    }
  }
}