package com.intellij.flex.config;

import com.intellij.conversion.ConversionListener;
import com.intellij.conversion.ConversionService;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.impl.ProjectJdkTableImpl;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.ArrayUtil;
import junit.framework.Assert;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FlexIdeConversionTest extends PlatformTestCase {

  private static final String BASE_PATH = "/flex/ideTestData/conversion/";

  private static final String SDK_HOME_VAR = "TEST_SDK_HOME";
  private static final String PROJECT_VAR = "TEST_PROJECT";
  public static final String JDK_TABLE_XML = "jdk.table.xml";
  public static final String GLOBAL_LIBS_XML = "applicationLibraries.xml";

  private Element myOriginalGlobalLibraries;
  private Element myOriginalSkds;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexLibraryIdGenerator.resetTestState();
    myOriginalGlobalLibraries = ApplicationLibraryTable.getApplicationTable().getState();
    myOriginalSkds = ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).getState();

    PathMacros.getInstance().setMacro(SDK_HOME_VAR, getHomePath() + BASE_PATH + "_fake_sdk");

    //ApplicationManager.getApplication().runWriteAction(new Runnable() {
    //  @Override
    //  public void run() {
    //    ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
    //    createFlexSdk(jdkTable, "some_flex_sdk", sdkHomePath, IFlexSdkType.Subtype.Flex);
    //    createFlexSdk(jdkTable, "some_air_sdk", sdkHomePath, IFlexSdkType.Subtype.AIR);
    //    createFlexSdk(jdkTable, "some_airmobile_sdk", sdkHomePath, IFlexSdkType.Subtype.AIRMobile);
    //  }
    //});
    //Element e = ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).getState();
    //ConversionHelper.collapsePaths(e);
    //Document d = new Document(e);
    //String s = JDOMUtil.writeDocument(d, "\n");
  }

  //private static void createFlexSdk(ProjectJdkTable jdkTable, String name, String homePath, IFlexSdkType.Subtype type) {
  //  SdkType sdkType;
  //  if (type == IFlexSdkType.Subtype.Flex) {
  //    sdkType = FlexSdkType.getInstance();
  //  }
  //  else if (type == IFlexSdkType.Subtype.AIR) {
  //    sdkType = AirSdkType.getInstance();
  //  }
  //  else if (type == IFlexSdkType.Subtype.AIRMobile) {
  //    sdkType = AirMobileSdkType.getInstance();
  //  }
  //  else {
  //    throw new IllegalArgumentException(type.toString());
  //  }
  //
  //  Sdk flexSdk1 = jdkTable.createSdk(name, sdkType);
  //  jdkTable.addJdk(flexSdk1);
  //  SdkModificator modificator = flexSdk1.getSdkModificator();
  //  modificator.setHomePath(homePath);
  //  modificator.commitChanges();
  //}

  @Override
  protected void tearDown() throws Exception {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ApplicationLibraryTable.getApplicationTable().loadState(myOriginalGlobalLibraries);
        ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).loadState(myOriginalSkds);
      }
    });
    PathMacros.getInstance().removeMacro(SDK_HOME_VAR);
    if (PathMacros.getInstance().getAllMacroNames().contains(PROJECT_VAR)) {
      PathMacros.getInstance().removeMacro(PROJECT_VAR);
    }
    super.tearDown();
  }

  @Override
  protected void setUpProject() throws Exception {
  }

  public void testFacets() throws IOException, JDOMException {
    doTest(getTestName(false), true);
  }

  public void testJavaProject() throws IOException, JDOMException {
    doTest(getTestName(false), false);
  }

  private void doTest(String testName, boolean conversionShouldHappen) throws IOException, JDOMException {
    String path = getHomePath() + BASE_PATH + testName;

    final File globalBefore = new File(path, "global_before");
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        try {
          {
            Document d = JDOMUtil.loadDocument(new File(globalBefore, JDK_TABLE_XML));
            ConversionHelper.expandPaths(d.getRootElement());
            ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).loadState(d.getRootElement());
          }
          {
            Document d = JDOMUtil.loadDocument(new File(globalBefore, GLOBAL_LIBS_XML));
            ConversionHelper.expandPaths(d.getRootElement());
            ApplicationLibraryTable.getApplicationTable().loadState(d.getRootElement());
          }
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    File projectBefore = new File(path, "project_before");
    assertTrue(projectBefore.isDirectory());

    File tempDir = createTempDirectory();
    PathMacros.getInstance().setMacro(PROJECT_VAR, tempDir.getCanonicalPath());
    FileUtil.copyDir(projectBefore, tempDir, true);

    MyConversionListener l = new MyConversionListener();
    ConversionService.getInstance().convertSilently(tempDir.getAbsolutePath(), l);
    if (!conversionShouldHappen) {
      assertTrue(!l.isConversionNeeded());
      assertTrue(!l.isConverted());
      return;
    }

    assertTrue(l.isConversionNeeded());
    assertTrue(l.isConverted());

    VirtualFile expectedDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path, "project_after"));
    final String[] extensionsToCheck = new String[]{"iml", "xml"};
    PlatformTestUtil.assertDirectoriesEqual(expectedDir,
                                            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempDir), new VirtualFileFilter() {
      @Override
      public boolean accept(VirtualFile file) {
        return file.isDirectory() || ArrayUtil.contains(file.getExtension(), extensionsToCheck);
      }
    });

    File globalAfter = new File(path, "global_after");
    {
      Document d = JDOMUtil.loadDocument(new File(globalAfter, JDK_TABLE_XML));
      Element sdkState = ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).getState();
      ConversionHelper.collapsePaths(sdkState);
      assertTrue(JDOMUtil.areElementsEqual(d.getRootElement(), sdkState));
    }

    {
      Document d = JDOMUtil.loadDocument(new File(globalAfter, GLOBAL_LIBS_XML));
      Element globalLibState = ApplicationLibraryTable.getApplicationTable().getState();
      ConversionHelper.collapsePaths(globalLibState);
      PlatformTestUtil.assertElementsEqual(d.getRootElement(), globalLibState);
    }
  }

  public void testUniqueNames() {
    doTestUniqueNames(new String[]{}, new String[]{});
    doTestUniqueNames(new String[]{"a", "b", "c", "a"}, new String[]{"a", "b", "c", "a (1)"});
    doTestUniqueNames(new String[]{"a", "b", "c", "a", "a"}, new String[]{"a", "b", "c", "a (1)", "a (2)"});
    doTestUniqueNames(new String[]{"a", "b", "c", "a", "a (1)", "a (2)"}, new String[]{"a", "b", "c", "a (3)", "a (1)", "a (2)"});
  }

  private static void doTestUniqueNames(String[] input, String[] output) {
    List<String> result = FlexBuildConfigurationManagerImpl.generateUniqueNames(Arrays.asList(input));
    assertTrue("output: " + Arrays.toString(result.toArray()) + ", expected: " + Arrays.toString(output),
               result.equals(Arrays.asList(output)));
  }

  private static class MyConversionListener implements ConversionListener {
    private boolean myConversionNeeded;
    private boolean myConverted;

    @Override
    public void conversionNeeded() {
      myConversionNeeded = true;
    }

    @Override
    public void successfullyConverted(File backupDir) {
      myConverted = true;
    }

    @Override
    public void error(String message) {
      Assert.fail(message);
    }

    @Override
    public void cannotWriteToFiles(List<File> readonlyFiles) {
    }

    public boolean isConversionNeeded() {
      return myConversionNeeded;
    }

    public boolean isConverted() {
      return myConverted;
    }
  }

  private static String toString(Element e) {
    Element clone = (Element)e.clone();
    ConversionHelper.collapsePaths(clone);
    return JDOMUtil.writeDocument(new Document(clone), "\n");
  }
}
