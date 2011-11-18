package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceDescriptor;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

abstract class MxmlTestBase extends AppTestBase {
  private static int TIMEOUT = Boolean.valueOf(System.getProperty("fud.test.debug")) ? 0 : 8;

  protected static final String SPARK_COMPONENTS_FILE = "SparkComponents.mxml";

  protected TestClient client;
  protected TestSocketInputHandler socketInputHandler;
  
  private int passedCounter;
  protected File appDir;
  
  protected VirtualFile getRawProjectRoot() throws NoSuchMethodException {
    return useRawProjectRoot() ? getTestDir() : null;
  }
  
  private boolean useRawProjectRoot() throws NoSuchMethodException {
    Flex annotation = getClass().getMethod(getName()).getAnnotation(Flex.class);
    return annotation != null && annotation.rawProjectRoot();
  }

  protected static String[] getLastProblems() {
    return TestDesignerApplicationManager.getLastProblems();
  }

  protected void assertAfterInitLibrarySets(List<XmlFile> unregisteredDocumentReferences) throws IOException {
  }

  static void changeServiceImplementation(Class key, Class implementation) {
    MutablePicoContainer picoContainer = (MutablePicoContainer)ApplicationManager.getApplication().getPicoContainer();
    picoContainer.unregisterComponent(key.getName());
    picoContainer.registerComponentImplementation(key.getName(), implementation);
  }

  private void launchAndInitializeApplication() throws Exception {
    if (DesignerApplicationManager.getInstance().isApplicationClosed()) {
      final ExtensionPoint<ServiceDescriptor> extensionPoint = DesignerApplicationManager.getExtensionPoint();
      ServiceDescriptor[] extensions = extensionPoint.getExtensions();
      for (ServiceDescriptor extension : extensions) {
        if (extension.serviceInterface.equals(SocketInputHandler.class.getName())) {
          extension.serviceImplementation = TestSocketInputHandler.class.getName();
        }
        else if (extension.serviceInterface.equals(Client.class.getName())) {
          extension.serviceImplementation = TestClient.class.getName();
        }
      }

      changeServiceImplementation(DocumentProblemManager.class, TestDesignerApplicationManager.MyDocumentProblemManager.class);

      new DesignerApplicationLauncher(myModule, false, new DesignerApplicationLauncher.PostTask() {
        @Override
        public boolean run(List<XmlFile> unregisteredDocumentReferences, ProgressIndicator indicator, ProblemsHolder problemsHolder) {
          assertTrue(DocumentProblemManager.getInstance().toString(problemsHolder.getProblems()), problemsHolder.isEmpty());

          client = (TestClient)Client.getInstance();
          client.flush();

          try {
            assertAfterInitLibrarySets(unregisteredDocumentReferences);
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
      List<XmlFile> unregistedDocumentReferences = LibraryManager.getInstance().initLibrarySets(myModule, isRequireLocalStyleHolder(), problemsHolder);
      assertTrue(problemsHolder.isEmpty());
      assertAfterInitLibrarySets(unregistedDocumentReferences);
    }

    appDir = DesignerApplicationManager.APP_DIR;
    socketInputHandler = (TestSocketInputHandler)SocketInputHandler.getInstance();
  }

  protected VirtualFile[] configureByFiles(@Nullable VirtualFile rawProjectRoot, VirtualFile[] files, @Nullable VirtualFile[] auxiliaryFiles) throws Exception {
    final VirtualFile[] sourceFiles = super.configureByFiles(rawProjectRoot, files, auxiliaryFiles);
    launchAndInitializeApplication();
    return sourceFiles;
  }

  protected void testFiles(VirtualFile[] files, VirtualFile[] auxiliaryFiles) throws Exception {
    testFiles(new MyTester(), files, auxiliaryFiles);
  }
  
  protected void testFiles(String[] paths, String... auxiliaryPaths) throws Exception {
    final VirtualFile[] files = new VirtualFile[paths.length];
    final VirtualFile[] auxiliaryFiles = auxiliaryPaths.length > 0 ? new VirtualFile[auxiliaryPaths.length] : null;
    int i = 0;
    for (String file : paths) {
      files[i++] = getSource(file);
    }
    i = 0;
    for (String file : auxiliaryPaths) {
      //noinspection ConstantConditions
      auxiliaryFiles[i++] = getSource(file);
    }
    
    testFiles(new MyTester(), files, auxiliaryFiles);
  }
  
  protected void testFile(String... originalVFilePath) throws Exception {
    testFile(new MyTester(), originalVFilePath);
  }
  
  protected void testFile(Tester tester, String... paths) throws Exception {
    final VirtualFile[] files = new VirtualFile[1];
    final VirtualFile[] auxiliaryFiles = paths.length > 1 ? new VirtualFile[paths.length - 1] : null;
    for (int i = 0; i < paths.length; i++) {
      if (i == 0) {
        files[i] = getSource(paths[i]);
      }
      else {
        //noinspection ConstantConditions
        auxiliaryFiles[i - 1] = getSource(paths[i]);
      }
    }

    testFiles(tester, files, auxiliaryFiles);
  }

  protected void testFiles(final Tester tester, VirtualFile[] files, VirtualFile[] auxiliaryFiles) throws Exception {
    VirtualFile[] testFiles = configureByFiles(getRawProjectRoot(), files, auxiliaryFiles);
    final PsiManager psiManager = PsiManager.getInstance(myProject);
    for (int i = 0, n = testFiles.length; i < n; i++) {
      final VirtualFile file = testFiles[i];
      if (!file.getName().endsWith(JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT)) {
        continue;
      }

      final VirtualFile originalVFile = files[i];
      final XmlFile xmlFile = (XmlFile)psiManager.findFile(file);
      assert xmlFile != null;

      final Callable<Boolean> action = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return tester.test(file, xmlFile, originalVFile);
        }
      };

      if (TIMEOUT == 0) {
        action.call();
      }
      else {
        ApplicationManager.getApplication().executeOnPooledThread(action).get(TIMEOUT, TimeUnit.SECONDS);
      }
    }
  }

  @Override
  protected void tearDown() throws Exception {
    System.out.print("\npassed " + passedCounter + " tests.\n");

    if (client != null) {
      try {
        client.closeProject(myProject);
      }
      finally {
        super.tearDown();
      }
    }
  }

  private class MyTester implements Tester {
    @Override
    public boolean test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
      String documentName = file.getNameWithoutExtension();
      System.out.print(documentName + '\n');
      client.openDocument(myModule, xmlFile);
      client.test(myModule, documentName, originalFile.getParent().getName());
      socketInputHandler.setExpectedErrorMessage(expectedErrorForDocument(documentName));
      try {
        socketInputHandler.process();
      }
      catch (IOException e) {
        LOG.error(e);
        return false;
      }

      passedCounter++;
      return true;
    }
  }

  protected String expectedErrorForDocument(String documentName) {
    return null;
  }
}

interface Tester {
  boolean test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception;
}