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
  
  protected String getRawProjectRoot() throws NoSuchMethodException {
    return useRawProjectRoot() ? getTestPath() : null;
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
          assertTrue(problemsHolder.isEmpty());

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

  protected VirtualFile[] configureByFiles(final VirtualFile rawProjectRoot, final VirtualFile... vFiles) throws Exception {
    final VirtualFile[] files = super.configureByFiles(rawProjectRoot, vFiles);
    launchAndInitializeApplication();
    return files;
  }

  protected void testFiles(final int auxiliaryBorder, final VirtualFile[] originalVFiles) throws Exception {
    testFiles(new MyTester(), auxiliaryBorder, originalVFiles);
  }
  
  protected void testFiles(String[] files, String... auxiliaryFiles) throws Exception {
    VirtualFile[] originalVFiles = new VirtualFile[files.length + auxiliaryFiles.length];
    int i = 0;
    for (String file : files) {
      originalVFiles[i++] = getVFile(getTestPath() + "/" + file);
    }
    int auxiliaryBorder = i;
    for (String file : auxiliaryFiles) {
      originalVFiles[i++] = getVFile(getTestPath() + "/" + file);
    }
    
    testFiles(new MyTester(), auxiliaryBorder, originalVFiles);
  }
  
  protected void testFile(String... originalVFilePath) throws Exception {
    testFile(new MyTester(), originalVFilePath);
  }
  
  protected void testFile(Tester tester, String... originalPaths) throws Exception {
    VirtualFile[] originalVFiles = new VirtualFile[originalPaths.length];
    for (int i = 0, originalPathsLength = originalPaths.length; i < originalPathsLength; i++) {
      originalVFiles[i] = getVFile(getTestPath() + "/" + originalPaths[i]);
    }
    
    testFiles(tester, 1, originalVFiles);
  }

  protected void testFiles(final Tester tester, final int auxiliaryBorder, final VirtualFile... originalVFiles) throws Exception {
    VirtualFile[] testVFiles = configureByFiles(useRawProjectRoot() ? getVFile(getRawProjectRoot()) : null, originalVFiles);
    final PsiManager psiManager = PsiManager.getInstance(myProject);
    for (int childrenLength = testVFiles.length, i = childrenLength - auxiliaryBorder; i < childrenLength; i++) {
      final VirtualFile file = testVFiles[i];
      if (!file.getName().endsWith(JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT)) {
        continue;
      }
      
      final VirtualFile originalVFile = originalVFiles[childrenLength - i - 1];
      final XmlFile xmlFile =  (XmlFile)psiManager.findFile(file);
      assert xmlFile != null;

      final Callable<Void> action = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          tester.test(file, xmlFile, originalVFile);
          return null;
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

    try {
      client.closeProject(myProject);
    }
    finally {
      super.tearDown();
    }
  }

  private class MyTester implements Tester {
    @Override
    public void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
      String documentName = file.getNameWithoutExtension();
      System.out.print(documentName + '\n');
      client.openDocument(myModule, xmlFile);
      client.test(myModule, documentName, originalFile.getParent().getName());
      socketInputHandler.setExpectedErrorMessage(expectedErrorForDocument(documentName));
      socketInputHandler.process();
      passedCounter++;
    }
  }

  protected String expectedErrorForDocument(String documentName) {
    return null;
  }
}

interface Tester {
  void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception;
}