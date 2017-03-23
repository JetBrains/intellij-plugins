package com.intellij.flex.bc;

import com.intellij.lang.javascript.ConversionTestBase;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.impl.ProjectJdkTableImpl;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.intellij.testFramework.assertions.Assertions.assertThat;

public abstract class ConversionTestBaseEx extends ConversionTestBase {

  private static final String SDK_HOME_VAR = "TEST_SDK_HOME";
  private static final String SDK_HOME_VAR_2 = "TEST_SDK_HOME_2";
  private static final String PROJECT_VAR = "TEST_PROJECT";
  public static final String JDK_TABLE_XML = "jdk.table.xml";
  public static final String GLOBAL_LIBS_XML = "applicationLibraries.xml";

  private Element myOriginalGlobalLibraries;
  private Element myOriginalSkds;

  protected boolean checkJdk() {
    return true;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myOriginalGlobalLibraries = ApplicationLibraryTable.getApplicationTable().getState();

    if (checkJdk()) {
      myOriginalSkds = ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).getState();
      PathMacros.getInstance().setMacro(SDK_HOME_VAR, FileUtil.toCanonicalPath(getHomePath() + getBasePath() + "_fake_sdk"));
      PathMacros.getInstance().setMacro(SDK_HOME_VAR_2, FileUtil.toCanonicalPath(getHomePath() + getBasePath() + "_fake_sdk_2"));
    }
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      ApplicationManager.getApplication().runWriteAction(() -> {
        final Library[] libraries = ApplicationLibraryTable.getApplicationTable().getLibraries();
        for (Library library : libraries) {
          ApplicationLibraryTable.getApplicationTable().removeLibrary(library);
        }

        ApplicationLibraryTable.getApplicationTable().loadState(myOriginalGlobalLibraries);
        if (checkJdk()) {
          ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).loadState(myOriginalSkds);
        }
      });

      if (checkJdk()) {
        PathMacros.getInstance().removeMacro(SDK_HOME_VAR);
        PathMacros.getInstance().removeMacro(SDK_HOME_VAR_2);
      }
      if (PathMacros.getInstance().getAllMacroNames().contains(PROJECT_VAR)) {
        PathMacros.getInstance().removeMacro(PROJECT_VAR);
      }
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected boolean doTest(String testName, boolean conversionShouldHappen) throws IOException, JDOMException {
    String path = getHomePath() + getBasePath() + testName;

    final File globalBefore = new File(path, "global_before");
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        if (checkJdk()) {
          Element d = JDOMUtil.load(new File(globalBefore, JDK_TABLE_XML));
          ConversionHelper.expandPaths(d);
          ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).loadState(d);
        }

        {
          Element d = JDOMUtil.load(new File(globalBefore, GLOBAL_LIBS_XML));
          ConversionHelper.expandPaths(d);
          ApplicationLibraryTable.getApplicationTable().loadState(d);
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    if (!super.doTest(testName, conversionShouldHappen)) {
      return false;
    }

    Path globalAfter = Paths.get(path, "global_after");
    if (checkJdk()) {
      Element sdkState = ((ProjectJdkTableImpl)ProjectJdkTable.getInstance()).getState();
      ConversionHelper.collapsePaths(sdkState);
      assertThat(sdkState).isEqualTo(globalAfter.resolve(JDK_TABLE_XML));
    }

    {
      Element globalLibState = ApplicationLibraryTable.getApplicationTable().getState();
      ConversionHelper.collapsePaths(globalLibState);
      assertThat(globalLibState).isEqualTo(globalAfter.resolve(GLOBAL_LIBS_XML));
    }
    return true;
  }
}
