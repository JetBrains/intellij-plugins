package com.intellij.flex.uiDesigner;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class AppTestBase extends FlashUIDesignerBaseTestCase {
  protected TestClient client;
  protected TestSocketInputHandler socketInputHandler;

  protected String flexSdkRootPath;
  protected final List<Pair<VirtualFile, VirtualFile>> libs = new ArrayList<>();

  protected String getSourceBasePath() {
    return "common";
  }

  private VirtualFile testDir;
  protected final VirtualFile getTestDir() {
    if (testDir == null) {
      testDir = DesignerTests.getFile("src", getSourceBasePath());
    }

    return testDir;
  }

  protected final VirtualFile getSource(String relativePath) {
    VirtualFile file = getTestDir().findFileByRelativePath(relativePath);
    assertNotNull(file);
    return file;
  }

  @Override
  protected void setUpJdk() {
    final String flexVersion = getFlexVersion();
    flexSdkRootPath = DesignerTests.getTestDataPath() + "/lib/flex-sdk/" + flexVersion;

    Flex annotation = getFlexAnnotation();
    doSetupFlexSdk(myModule, flexSdkRootPath, annotation == null ? TargetPlatform.Desktop : annotation.platform(), flexVersion);
  }

  protected Disposable getSdkParentDisposable() {
    return ApplicationManager.getApplication();
  }

  private void doSetupFlexSdk(final Module module, final String flexSdkRootPath, final TargetPlatform targetPlatform, final String sdkVersion) {
    final String sdkName = generateSdkName(sdkVersion);
    Sdk sdk = ProjectJdkTable.getInstance().findJdk(sdkName);
    if (sdk == null) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        FlexSdkType2 sdkType = FlexSdkType2.getInstance();
        Sdk sdk1 = new ProjectJdkImpl(sdkName, sdkType, flexSdkRootPath, "");
        ProjectJdkTable.getInstance().addJdk(sdk1);

        Disposer.register(getSdkParentDisposable(), new Disposable() {
          @Override
          public void dispose() {
            ApplicationManager.getApplication().runWriteAction(() -> {
              ProjectJdkTable sdkTable = ProjectJdkTable.getInstance();
              sdkTable.removeJdk(sdkTable.findJdk(sdkName));
            });
          }
        });

        final SdkModificator modificator = sdk1.getSdkModificator();
        modificator.setVersionString(FlexSdkType2.getInstance().getVersionString(sdk1.getHomePath()));
        modifySdk(sdk1, modificator);
        modificator.commitChanges();
      });
    }

    FlexTestUtils.modifyBuildConfiguration(module, bc -> {
      bc.setNature(new BuildConfigurationNature(targetPlatform, false, getOutputType()));
      bc.getDependencies().setSdkEntry(Factory.createSdkEntry(sdkName));
    });
  }

  protected OutputType getOutputType() {
    return OutputType.Application;
  }

  protected String generateSdkName(String sdkVersion) {
    return "test-" + sdkVersion;
  }
  
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    final VirtualFile sdkHome = sdk.getHomeDirectory();
    assert sdkHome != null;
    final VirtualFile frameworksDir = sdkHome.findChild("frameworks");
    assert frameworksDir != null;
    sdkModificator.addRoot(DesignerTests.getFile("lib", "playerglobal"), OrderRootType.CLASSES);
    FlexSdkType2.addFlexSdkSwcRoots(sdkModificator, frameworksDir);
  }

  @SuppressWarnings("ConstantConditions")
  protected void addLibrary(SdkModificator sdkModificator, String path) {
    VirtualFile virtualFile = path.charAt(0) != '/' ? DesignerTests.getFile("lib", path) : DesignerTests.getFile(path);
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
    assert jarFile != null;

    libs.add(new Pair<>(virtualFile, jarFile));
    sdkModificator.addRoot(jarFile, OrderRootType.CLASSES);
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

  private Flex getFlexAnnotation() {
    try {
      return getClass().getMethod(getName()).getAnnotation(Flex.class);
    }
    catch (NoSuchMethodException e) {
      throw new AssertionFailedError(e.getMessage());
    }
  }
  
  protected boolean isRequireLocalStyleHolder() {
    Flex annotation = getFlexAnnotation();
    return annotation != null && annotation.requireLocalStyleHolder();
  }

  protected void assertAfterInitLibrarySets(List<XmlFile> unregisteredDocumentReferences) throws IOException {
  }

  protected VirtualFile[] configureByFiles(String... paths) throws Exception {
    final VirtualFile[] files = new VirtualFile[paths.length];
    for (int i = 0; i < paths.length; i++) {
      VirtualFile file = getSource(paths[i]);
      assertNotNull(file);
      files[i] = file;
    }
    return configureByFiles(null, files, null);
  }

  @Override
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

      new DesignerApplicationLauncher(myModule, new DesignerApplicationLauncher.PostTask() {
        @Override
        public boolean run(Module module,
                           ProjectComponentReferenceCounter projectComponentReferenceCounter,
                           ProgressIndicator indicator,
                           ProblemsHolder problemsHolder) {
          assertTrue(DocumentProblemManager.getInstance().toString(problemsHolder.getProblems()), problemsHolder.isEmpty());

          client = (TestClient)Client.getInstance();
          client.flush();

          try {
            assertAfterInitLibrarySets(projectComponentReferenceCounter.unregistered);
          }
          catch (IOException e) {
            throw new AssertionError(e);
          }

          return true;
        }
      }).run(new EmptyProgressIndicator());
    }
    else {
      client = (TestClient)Client.getInstance();
      ProblemsHolder problemsHolder = new ProblemsHolder();
      ProjectComponentReferenceCounter projectComponentReferenceCounter = LibraryManager.getInstance().registerModule(myModule, problemsHolder, isRequireLocalStyleHolder());
      assertTrue(problemsHolder.isEmpty());
      assertAfterInitLibrarySets(projectComponentReferenceCounter.unregistered);
    }

    socketInputHandler = (TestSocketInputHandler)SocketInputHandler.getInstance();
    applicationLaunchedAndInitialized();
  }

  protected abstract void changeServicesImplementation();

  protected void applicationLaunchedAndInitialized() {
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (client != null) {
        client.closeProject(myProject);
      }
    }
    finally {
      super.tearDown();
    }
  }
}