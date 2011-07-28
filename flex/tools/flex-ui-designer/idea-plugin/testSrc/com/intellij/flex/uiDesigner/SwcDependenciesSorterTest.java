package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.Disposer;

import java.io.File;
import java.util.List;

@Flex(version="4.5")
public class SwcDependenciesSorterTest extends MxmlTestBase {
  @Override
  protected String generateSdkName(String version, boolean air) {
    return getName();
  }

  @Override
  protected void modifySdk(final Sdk sdk, SdkModificator sdkModificator) {
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

    Disposer.register(myModule, new Disposable() {
      @Override
      public void dispose() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
            projectJdkTable.removeJdk(sdk);
          }
        });
      }
    });
  }

  @Override
  protected void assertAfterInitLibrarySets() {
    super.assertAfterInitLibrarySets();

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      for (LibrarySetItem item : myLibraries()) {
        assertFalse(item.library.getPath().contains("spark_dmv"));
      }
    }
  }

  private List<LibrarySetItem> myLibraries() {
    return client.getRegisteredProjects().getInfo(myProject).getSdkLibrarySet().getItems();
  }

  public void testDeleteIfAllDefitionsHaveUnresolvedDependencies() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }
  
  @SuppressWarnings({"UnusedDeclaration"})
  @Flex(version="4.1")
  public void _TODO_testDelete() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  public void testResolveToClassWithBiggestTimestamp() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  @Override
  protected void tearDown() throws Exception {
    if (appDir != null && appDir.exists()) {
      LibrarySet sdkLibrarySet = client.getRegisteredProjects().getInfo(myProject).getSdkLibrarySet();
      for (LibrarySetItem item : myLibraries()) {
        //noinspection ResultOfMethodCallIgnored
        new File(appDir, item.library.getPath() + (item.filtered ? "_" + sdkLibrarySet.getId() + ".swf" : ".swf")).delete();
      }
    }

    super.tearDown();
  }
}