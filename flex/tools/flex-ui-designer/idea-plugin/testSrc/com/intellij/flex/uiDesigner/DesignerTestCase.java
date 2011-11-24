package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import com.intellij.testFramework.LightPlatformTestCase;
import junit.framework.AssertionFailedError;

import java.io.File;
import java.util.Arrays;

public class DesignerTestCase extends LightPlatformTestCase {
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected Sdk getProjectJDK() {
    final String flexVersion = getFlexVersion();
    return getOrCeateFlexSdk(DebugPathManager.getTestDataPath() + "/lib/flex-sdk/" + flexVersion, true,
      flexVersion + "." + (flexVersion.equals("4.1") ? "16076" : "20967"));
  }

  public void testF() {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        int g = 4;
        g++;
      }
    });
  }

  private static Sdk getOrCeateFlexSdk(final String homePath, final boolean air, final String version) {
    final String name = version + (air ? "-air-" : "-flex");
    final ProjectJdkTable projectSdkTable = ProjectJdkTable.getInstance();
    Sdk sdk = projectSdkTable.findJdk(name);
    if (sdk == null) {
      SdkType sdkType = air ? AirSdkType.getInstance() : FlexSdkType.getInstance();
      sdk = PeerFactory.getInstance().createProjectJdk(name, version, homePath, sdkType);
      sdkType.setupSdkPaths(sdk);
      projectSdkTable.addJdk(sdk);
    }

    final SdkModificator modificator = sdk.getSdkModificator();
    modificator.addRoot(getVirtualFile("lib/playerglobal"), OrderRootType.CLASSES);
    String[] list = new File(homePath).list();
    Arrays.sort(list);
    for (String filename : list) {
      if (filename.endsWith(".swc")) {
        addLibrary(modificator, homePath + "/" + filename);
      }
    }
    modificator.commitChanges();

    return sdk;
  }

  private static void addLibrary(SdkModificator modificator, String path) {
    if (path.charAt(0) != '/') {
      path = DebugPathManager.getTestDataPath() + "/lib/" + path;
    }

    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(getVirtualFile(path));
    assertNotNull(jarFile);

    modificator.addRoot(jarFile, OrderRootType.CLASSES);
  }

  protected static VirtualFile getVirtualFile(String path) {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(path.charAt(0) == '/' ? path : (DebugPathManager.getTestDataPath() + '/' + path));
    assertNotNull(vFile);
    return vFile;
  }

  private String getFlexVersion() {
    try {
      Flex annotation = getClass().getMethod(getName()).getAnnotation(Flex.class);
      if (annotation == null || annotation.version().isEmpty()) {
        annotation = getClass().getAnnotation(Flex.class);
      }

      assert annotation != null && !annotation.version().isEmpty();
      return annotation.version();
    }
    catch (NoSuchMethodException e) {
      throw new AssertionFailedError(e.getMessage());
    }
  }

  protected boolean isRequireLocalStyleHolder() {
    try {
      Flex annotation = getClass().getMethod(getName()).getAnnotation(Flex.class);
      return annotation != null && annotation.requireLocalStyleHolder();
    }
    catch (NoSuchMethodException e) {
      throw new AssertionFailedError(e.getMessage());
    }
  }
}
