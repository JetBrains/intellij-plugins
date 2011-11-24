package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.mxml.ProjectDocumentReferenceCounter;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class AppTestBase extends FlashUIDesignerBaseTestCase {
  protected TestClient client;
  protected TestSocketInputHandler socketInputHandler;

  protected String flexSdkRootPath;
  protected Sdk sdk;
  protected final List<Pair<VirtualFile, VirtualFile>> libs = new ArrayList<Pair<VirtualFile, VirtualFile>>();

  protected String getSourceBasePath() {
    return "common";
  }
  
  private VirtualFile testDir;
  protected final VirtualFile getTestDir() {
    if (testDir == null) {
      testDir = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + "/src/" + getSourceBasePath());
    }

    return testDir;
  }
  
  protected final VirtualFile getSource(String relativePath) {
    return getTestDir().findFileByRelativePath(relativePath);
  }

  @Override
  protected void setUpJdk() {
    final String flexVersion = getFlexVersion();
    flexSdkRootPath = getTestDataPath() + "/lib/flex-sdk/" + flexVersion;
    doSetupFlexSdk(myModule, flexSdkRootPath, true, flexVersion + "." + (flexVersion.equals("4.1") ? "16076" : "20967"));
  }

  protected String generateSdkName(String version, boolean air) {
    return version + (air ? "-air-" : "-flex");
  }
  
  private void doSetupFlexSdk(final Module module, final String flexSdkRootPath, final boolean air, final String sdkVersion) {
    final AccessToken token = WriteAction.start();
    try {
      final String sdkName = generateSdkName(sdkVersion, air);
      sdk = ProjectJdkTable.getInstance().findJdk(sdkName);
      if (sdk == null) {
        //noinspection RedundantCast
        sdk = FlexSdkUtils.createOrGetSdk(air ? (SdkType)AirSdkType.getInstance() : (SdkType)FlexSdkType.getInstance(),
                                          flexSdkRootPath);
        assert sdk != null;
      }

      final SdkModificator modificator = sdk.getSdkModificator();
      modificator.setName(sdkName);
      modificator.setVersionString(sdkVersion);
      modifySdk(sdk, modificator);
      modificator.commitChanges();

      final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
      rootModel.setSdk(sdk);
      rootModel.commit();
    }
    finally {
      token.finish();
    }
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
  
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    modifySdk(sdkModificator, null);
  }

  protected void modifySdk(SdkModificator sdkModificator, @Nullable Condition<String> filter) {
    sdkModificator.addRoot(getVFile("lib/playerglobal"), OrderRootType.CLASSES);

    String[] list = new File(flexSdkRootPath).list();
    Arrays.sort(list);
    for (String name : list) {
      if (name.endsWith(".swc") && (filter == null || filter.value(name))) {
        addLibrary(sdkModificator, flexSdkRootPath + "/" + name);
      }
    }
  }

  protected void addLibrary(SdkModificator sdkModificator, String path) {
    if (path.charAt(0) != '/') {
      path = getTestDataPath() + "/lib/" + path;
    }

    VirtualFile virtualFile = getVFile(path);
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
    assert jarFile != null;

    
    libs.add(new Pair<VirtualFile, VirtualFile>(virtualFile, jarFile));
    sdkModificator.addRoot(jarFile, OrderRootType.CLASSES);
  }

  protected void addLibrary(ModifiableRootModel model, String path) {
    if (path.charAt(0) != '/') {
      path = getTestDataPath() + "/lib/" + path;
    }

    VirtualFile virtualFile = getVFile(path);
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
    assert jarFile != null;

    libs.add(new Pair<VirtualFile, VirtualFile>(virtualFile, jarFile));
    Library.ModifiableModel libraryModel = model.getModuleLibraryTable().createLibrary(path).getModifiableModel();
    libraryModel.addRoot(jarFile, OrderRootType.CLASSES);
    libraryModel.commit();
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

  protected void assertAfterInitLibrarySets(List<XmlFile> unregisteredDocumentReferences) throws IOException {
  }

  protected VirtualFile[] configureByFiles(@Nullable VirtualFile rawProjectRoot,
                                           VirtualFile[] files,
                                           @Nullable VirtualFile[] auxiliaryFiles) throws Exception {
    final VirtualFile[] sourceFiles = super.configureByFiles(rawProjectRoot, files, auxiliaryFiles);
    launchAndInitializeApplication();
    return sourceFiles;
  }

  private void launchAndInitializeApplication() throws Exception {
    if (DesignerApplicationManager.getInstance().isApplicationClosed()) {
      changeServicesImplementation();

      new DesignerApplicationLauncher(myModule, false, new DesignerApplicationLauncher.PostTask() {
        @Override
        public boolean run(ProjectDocumentReferenceCounter projectDocumentReferenceCounter,
                           ProgressIndicator indicator,
                           ProblemsHolder problemsHolder) {
          assertTrue(DocumentProblemManager.getInstance().toString(problemsHolder.getProblems()), problemsHolder.isEmpty());

          client = (TestClient)Client.getInstance();
          client.flush();

          try {
            assertAfterInitLibrarySets(projectDocumentReferenceCounter.unregistered);
          }
          catch (IOException e) {
            throw new AssertionError(e);
          }

          return true;
        }

        @Override
        public void end() {
        }
      }).run(new EmptyProgressIndicator());
    }
    else {
      client = (TestClient)Client.getInstance();
      final ProblemsHolder problemsHolder = new ProblemsHolder();
      ProjectDocumentReferenceCounter
        projectDocumentReferenceCounter =
        LibraryManager.getInstance().initLibrarySets(myModule, isRequireLocalStyleHolder(), problemsHolder);
      assertTrue(problemsHolder.isEmpty());
      assertAfterInitLibrarySets(projectDocumentReferenceCounter.unregistered);
    }

    socketInputHandler = (TestSocketInputHandler)SocketInputHandler.getInstance();
    applicationLaunchedAndInitialized();
  }

  protected abstract void changeServicesImplementation();

  protected void applicationLaunchedAndInitialized() {

  }

  @Override
  protected void tearDown() throws Exception {
    if (client != null) {
      try {
        client.closeProject(myProject);
      }
      finally {
        super.tearDown();
      }
    }
  }
}