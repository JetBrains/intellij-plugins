package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.AssertionFailedError;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class AppTestBase extends FlexUIDesignerBaseTestCase {
  protected static final String PASSED = "__passed__";
  protected static final Key<Boolean> IS_USER_LIB = Key.create("FUD_IS_USER_LIB");
  private static final String BASE_PATH = "/mxml";

  protected String flexSdkRootPath;
  protected final List<Pair<VirtualFile, VirtualFile>> libs = new ArrayList<Pair<VirtualFile, VirtualFile>>();
  
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
  
  protected static VirtualFile getVFile(String file) {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(file);
    assert vFile != null;
    return vFile;
  }
  
  protected static void changeServiceImplementation(Class key, Class implementation) {
    MutablePicoContainer picoContainer = (MutablePicoContainer) ApplicationManager.getApplication().getPicoContainer();
    picoContainer.unregisterComponent(key.getName());
    picoContainer.registerComponentImplementation(key.getName(), implementation);
  }

  @Override
  protected void setUpJdk() {    
    flexSdkRootPath = getTestDataPath() + "/sdk/" + getFlexVersion();
    doSetupFlexSdk(myModule, flexSdkRootPath, true, getFlexVersion() + ".0");
  }
  
  private void doSetupFlexSdk(final Module module, final String flexSdkRootPath, final boolean air, final String sdkVersion) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        //otherwise that brilliant javac won't compile
        //noinspection RedundantCast
        final Sdk sdk = FlexSdkUtils.createOrGetSdk(air ? (SdkType)AirSdkType.getInstance() : (SdkType)FlexSdkType.getInstance(),
                                                    flexSdkRootPath);
        assert sdk != null;
        final SdkModificator modificator = sdk.getSdkModificator();

        modificator.setVersionString(sdkVersion);
        modifySdk(sdk, modificator);
        modificator.commitChanges();

        final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        rootModel.setSdk(sdk);
        rootModel.commit();

        Disposer.register(module, new Disposable() {
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
    });
  }
  
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
  
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    sdkModificator.addRoot(LocalFileSystem.getInstance().findFileByPath(getPlayerGlobalRootPath()), OrderRootType.CLASSES);
    
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
    VirtualFile virtualFile = getVFile(path);
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
    assert jarFile != null;
    
    if (!fromSdk) {
      jarFile.putUserData(IS_USER_LIB, true);
    }
    
    libs.add(new Pair<VirtualFile, VirtualFile>(virtualFile, jarFile));
    sdkModificator.addRoot(jarFile, OrderRootType.CLASSES);
  }

  protected void addLibrary(ModifiableRootModel model, String path) {
    VirtualFile virtualFile = getVFile(path);
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
    assert jarFile != null;
    jarFile.putUserData(IS_USER_LIB, true);

    libs.add(new Pair<VirtualFile, VirtualFile>(virtualFile, jarFile));
    Library.ModifiableModel libraryModel = model.getModuleLibraryTable().createLibrary(path).getModifiableModel();
    libraryModel.addRoot(jarFile, OrderRootType.CLASSES);
    libraryModel.commit();
  }

  @Override
  protected void tearDown() throws Exception {
    StringRegistry.getInstance().reset();

    BinaryFileManager.getInstance().reset();
    LibraryManager.getInstance().reset();
    
    super.tearDown();
  }
  
  protected String getFlexVersion() {
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

  protected static void copySwfAndDescriptor(final File rootDir) throws IOException {
    //noinspection ResultOfMethodCallIgnored
    rootDir.mkdirs();
    FileUtil.copy(new File(getFudHome(), "app-loader/target/app-loader-1.0-SNAPSHOT.swf"), new File(rootDir, FlexUIDesignerApplicationManager.DESIGNER_SWF));
    FileUtil.copy(new File(getFudHome(), "designer/src/main/resources/descriptor.xml"), new File(rootDir, FlexUIDesignerApplicationManager.DESCRIPTOR_XML));
  }
}
