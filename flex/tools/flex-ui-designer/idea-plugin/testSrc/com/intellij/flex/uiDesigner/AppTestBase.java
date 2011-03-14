package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import js.JSTestUtils;
import junit.framework.AssertionFailedError;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class AppTestBase extends FlexUIDesignerBaseTestCase {
  protected static final Key<Boolean> IS_USER_LIB = Key.create("FUD_IS_USER_LIB");
  private static final String BASE_PATH = "/mxml";
  
  protected String flexSdkRootPath;
  protected List<Pair<VirtualFile, VirtualFile>> libs;
  
  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }
  
  protected String getTestPath() {
    return getTestDataPath() + getBasePath();
  }
  
  private String getPlayerGlobalRootPath() {
    return getTestDataPath() + "/sdk/playerglobal";
  }
  
  protected void changeServiceImplementation(Class key, Class implementation) {
    MutablePicoContainer picoContainer = (MutablePicoContainer) ApplicationManager.getApplication().getPicoContainer();
    picoContainer.unregisterComponent(key.getName());
    picoContainer.registerComponentImplementation(key.getName(), implementation);
  }

  @Override
  protected void setUpJdk() {    
    flexSdkRootPath = getTestDataPath() + "/sdk/" + getFlexVersion();
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), flexSdkRootPath, true);

    Sdk sdk = ModuleRootManager.getInstance(myModule).getSdk();
    assert sdk != null;
    SdkModificator sdkModificator = sdk.getSdkModificator();
    modifySdk(sdk, sdkModificator);
    sdkModificator.commitChanges();
  }
  
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    sdkModificator.setVersionString(getFlexVersion() + ".0");
    
    sdkModificator.addRoot(LocalFileSystem.getInstance().findFileByPath(getPlayerGlobalRootPath()), OrderRootType.CLASSES);
    
    sdkModificator.removeRoot(sdk.getHomeDirectory(), OrderRootType.CLASSES);
    sdkModificator.removeRoot(sdk.getHomeDirectory(), OrderRootType.SOURCES);
    
    libs = new ArrayList<Pair<VirtualFile, VirtualFile>>();
    String[] list = new File(flexSdkRootPath).list();
    Arrays.sort(list);
    for (String name : list) {
      if (name.endsWith(".swc")) {
        addLibrary(sdkModificator, flexSdkRootPath + "/" + name);
      }
    }
  }
  
  protected void addLibrary(SdkModificator sdkModificator, String path) {
    addLibrary(sdkModificator, path, true);
  }

  protected void addLibrary(SdkModificator sdkModificator, String path, boolean fromSdk) {
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
    assert virtualFile != null;
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
    assert jarFile != null;
    
    if (!fromSdk) {
      jarFile.putUserData(IS_USER_LIB, true);
    }
    
    libs.add(new Pair<VirtualFile, VirtualFile>(virtualFile, jarFile));
    sdkModificator.addRoot(jarFile, OrderRootType.CLASSES);
  }

  @Override
  protected void tearDown() throws Exception {
    ServiceManager.getService(StringRegistry.class).reset();

    for (Pair<VirtualFile, VirtualFile> lib : libs) {
      LibraryCollector.clearCache(lib.getSecond());
    }
    
    super.tearDown();
  }
  
  protected String getFlexVersion() {
    try {
      return getClass().getMethod(getName()).getAnnotation(Flex.class).version();
    }
    catch (NoSuchMethodException e) {
      throw new AssertionFailedError(e.getMessage());
    }
  }
  
  protected boolean isRequireLocalStyleHolder() {
    try {
      return getClass().getMethod(getName()).getAnnotation(Flex.class).requireLocalStyleHolder();
    }
    catch (NoSuchMethodException e) {
      throw new AssertionFailedError(e.getMessage());
    }
  }

  protected void copySwfAndDescriptor(final File rootDir) {
    //noinspection ResultOfMethodCallIgnored
    rootDir.mkdirs();
    try {
      FileUtil.copy(new File(getFudHome(), "app-loader/target/app-loader-1.0-SNAPSHOT.swf"), new File(rootDir, "designer.swf"));
      FileUtil.copy(new File(getFudHome(), "designer/src/main/resources/descriptor.xml"), new File(rootDir, "descriptor.xml"));
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }
}
