package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

abstract class MxmlTestBase extends AppTestBase {
  private static int TIMEOUT = Boolean.valueOf(System.getProperty("fud.test.debug")) ? 0 : 8;

  protected static final String SPARK_COMPONENTS_FILE = "SparkComponents.mxml";

  private int passedCounter;

  @Override
  protected void changeServicesImplementation() {
    Tests.changeDesignerServicesImplementation();
    Tests.changeServiceImplementation(DocumentProblemManager.class, TestDesignerApplicationManager.MyDocumentProblemManager.class);
  }

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

      final Callable<String> action = new Callable<String>() {
        @Override
        public String call() throws Exception {
          return tester.test(file, xmlFile, originalVFile);
        }
      };

      final String failMessage;
      if (TIMEOUT == 0) {
        failMessage = action.call();
      }
      else {
        failMessage = ApplicationManager.getApplication().executeOnPooledThread(action).get(TIMEOUT, TimeUnit.SECONDS);
      }

      if (failMessage != null) {
        fail(failMessage);
      }
    }
  }

  @Override
  protected void tearDown() throws Exception {
    System.out.print("\npassed " + passedCounter + " tests.\n");
    super.tearDown();
  }

  private class MyTester implements Tester {
    @Override
    public String test(VirtualFile file, XmlFile xmlFile, final VirtualFile originalFile) throws Exception {
      final String documentName = file.getNameWithoutExtension();
      System.out.print(documentName + '\n');
      socketInputHandler.setExpectedErrorMessage(expectedErrorForDocument(documentName));
      AsyncResult<DocumentFactoryManager.DocumentInfo> renderResult = client.renderDocument(myModule, xmlFile, new ProblemsHolder());
      client.flush();
      socketInputHandler.processUntil(renderResult);
      if (renderResult.isDone()) {
        ActionCallback testCallback = client.test(DocumentFactoryManager.getInstance().getId(file), documentName, originalFile.getParent().getName());
        socketInputHandler.processUntil(testCallback);
        if (testCallback.isDone()) {
          passedCounter++;
        }
        else {
          throw new AssertionError(socketInputHandler.reader.readUTF());
        }
      }

      return null;
    }
  }

  protected String expectedErrorForDocument(String documentName) {
    return null;
  }
}

interface Tester {
  String test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception;
}