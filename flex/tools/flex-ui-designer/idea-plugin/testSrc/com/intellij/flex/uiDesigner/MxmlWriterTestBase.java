package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

abstract class MxmlWriterTestBase extends AppTestBase {
  private TestClient client;
  private Socket socket;
  private DataInputStream reader;

  private Process adlProcess;
  
  private int passedCounter;
  protected List<Library> libraries;
  private LibrarySet librarySet;
  protected File appRootDir;

  protected List<OriginalLibrary> getLibraries(Consumer<OriginalLibrary> initializer) {
    List<OriginalLibrary> libraries = new ArrayList<OriginalLibrary>(libs.size());
    for (Pair<VirtualFile, VirtualFile> pair : libs) {
      libraries.add(LibraryCollector.createOriginalLibrary(pair.getFirst(), pair.getSecond(), initializer, pair.getSecond().getUserData(IS_USER_LIB) == null));
    }
    
    return libraries;
  }
  
  protected File getAppRootDir() throws IOException {
    File appRootDir;
    if (Boolean.valueOf(System.getProperty("fud.test.debug"))) {
      appRootDir = new File(PathManager.getHomePath() + "/testAppRoot");
      if (!appRootDir.exists() || SystemInfo.isWindows) {
        copySwfAndDescriptor(appRootDir);
      }
    }
    else {
      appRootDir = createTempDir("fud");
      FileUtil.copy(new File(getFudHome() + "/app-loader/target/app-loader-1.0-SNAPSHOT.swf"), new File(appRootDir, "designer.swf"));
    }
    return appRootDir;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(ServiceManager.getService(StringRegistry.class));
    final LibraryStyleInfoCollector styleInfoCollector = new LibraryStyleInfoCollector(myProject, myModule, stringWriter);
    stringWriter.startChange();

    appRootDir = getAppRootDir();
    libraries = new SwcDependenciesSorter(appRootDir).sort(getLibraries(new Consumer<OriginalLibrary>() {
      @Override
      public void consume(OriginalLibrary originalLibrary) {
        styleInfoCollector.collect(originalLibrary);
      }
    }), myProject.getLocationHash(), getFlexVersion(), 0);

    final ServerSocket serverSocket = new ServerSocket(0, 1);
    
    DesignerApplicationUtil.AdlRunConfiguration adlRunConfiguration = new DesignerApplicationUtil.AdlRunConfiguration(System.getProperty("fud.adl"), System.getProperty("fud.air"));
    adlRunConfiguration.arguments = new ArrayList<String>();
    adlRunConfiguration.arguments.add("-p");
    adlRunConfiguration.arguments.add(getFudHome() + "/test-app-plugin/target/test-1.0-SNAPSHOT.swf");

    adlRunConfiguration.arguments.add("-cdd");
    adlRunConfiguration.arguments.add(getFudHome() + "/flex-injection/target");

    adlProcess = DesignerApplicationUtil.runAdl(adlRunConfiguration, getFudHome() + "/designer/src/main/resources/descriptor.xml", serverSocket.getLocalPort(), appRootDir.getPath(), new Consumer<Integer>() {
      @Override
      public void consume(Integer exitCode) {
        if (exitCode != 0) {
          try {
            serverSocket.close();
          }
          catch (IOException ignored) {}
          
          fail("adl return " + exitCode);
        }
      }
    });

    socket = serverSocket.accept();
    reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    client = new TestClient(socket.getOutputStream());

    client.openProject(myProject);
    librarySet = new LibrarySet(myProject.getLocationHash(), ApplicationDomainCreationPolicy.ONE, libraries);
    client.registerLibrarySet(librarySet, stringWriter);
    
    if (!isRequireLocalStyleHolder()) {
      registerModule(new ModuleInfo(myModule), stringWriter);
    }
  }

  private void registerModule(ModuleInfo moduleInfo, StringRegistry.StringWriter stringWriter) throws IOException {
    client.registerModule(myProject, moduleInfo, new String[]{librarySet.getId()}, stringWriter);
  }
  
  protected void testFiles(VirtualFile[] originalVFiles) throws IOException {
    VirtualFile[] testVFiles = configureByFiles(null, originalVFiles).getChildren();
    
    if (isRequireLocalStyleHolder()) {
      ModuleInfo moduleInfo = new ModuleInfo(myModule);
      final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(ServiceManager.getService(StringRegistry.class));
      stringWriter.startChange();
      ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, getFlexVersion(), stringWriter);
      registerModule(moduleInfo, stringWriter);
    }
    
    for (int i = 0, childrenLength = testVFiles.length; i < childrenLength; i++) {
      VirtualFile file = testVFiles[i];
      XmlFile xmlFile = (XmlFile) myPsiManager.findFile(file);
      assert xmlFile != null;
      String filename = file.getNameWithoutExtension();
      System.out.print(filename);
      long start = System.currentTimeMillis();
      client.openDocument(myModule, xmlFile);
      long time = System.currentTimeMillis() - start;

      client.assertStates(filename, originalVFiles[childrenLength - i - 1].getParent().getName());
      assertResult(filename, time);
    }
  }
  
  protected void testFile(String originalVFilePath) throws IOException {
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(getTestMxmlPath() + "/" + originalVFilePath);
    assert virtualFile != null;
    testFiles(new VirtualFile[]{virtualFile});
  }
  
  private void assertResult(String documentName, long time) throws IOException {
    boolean result = reader.readBoolean();
    if (result) {
      System.out.print(" passed (" + time + ")\n");
      passedCounter++;
      LOG.info(documentName + " passed");
    }
    else {
      fail(documentName + "\n" + reader.readUTF());
    }
  }

  @Override
  protected void tearDown() throws Exception {
    System.out.print("\npassed " + passedCounter + " tests.\n");

    client.close();
    reader.close();
    socket.close();

    adlProcess.destroy();
    
    super.tearDown();
  }
}
