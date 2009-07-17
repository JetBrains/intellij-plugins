package com.intellij.tapestry.tests;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.facet.TapestryFacet;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.util.ArrayUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Alexey Chmutov
 *         Date: Jul 13, 2009
 *         Time: 3:49:34 PM
 */
public abstract class TapestryBaseTestCase extends UsefulTestCase {
  static final String TEST_APPLICATION_PACKAGE = "com.testapp";
  static final String COMPONENTS = "components";
  static final String PAGES = "pages";
  static final String COMPONENTS_PACKAGE_PATH = TEST_APPLICATION_PACKAGE.replace('.', '/') + "/" + COMPONENTS + "/";
  static final String PAGES_PACKAGE_PATH = TEST_APPLICATION_PACKAGE.replace('.', '/') + "/" + PAGES + "/";

  @NonNls
  protected abstract String getBasePath();

  @NonNls
  protected final String getTestDataPath() {
    return getCommonTestDataPath() + getBasePath();
  }

  private String getCommonTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + "/plugins/tapestry/tests/testData/";
  }

  protected CodeInsightTestFixture myFixture;
  protected Project myProject;
  protected Module myModule;

  protected Class<? extends JavaModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return JavaModuleFixtureBuilder.class;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = JavaTestFixtureFactory.createFixtureBuilder();
    JavaModuleFixtureBuilder moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());
    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());
    myFixture.setTestDataPath(getTestDataPath());
    configureModule(moduleBuilder);

    myFixture.setUp();
    myProject = myFixture.getProject();
    myModule = moduleBuilder.getFixture().getModule();

    createFacet();
  }

  protected TapestryFacet createFacet() {
    final RunResult<TapestryFacet> runResult = new WriteCommandAction<TapestryFacet>(myProject) {
      protected void run(final Result<TapestryFacet> result) throws Throwable {
        final TapestryFacetType facetType = TapestryFacetType.INSTANCE;
        final FacetManager facetManager = FacetManager.getInstance(myModule);
        final TapestryFacet facet = facetManager.addFacet(facetType, facetType.getPresentableName(), null);
        facet.getConfiguration().setApplicationPackage(TEST_APPLICATION_PACKAGE);
        result.setResult(facet);
        Assert.assertNotNull(facetManager.getFacetByType(TapestryFacetType.ID));
      }
    }.execute();
    if (runResult.hasException()) {
      throw new RuntimeException(runResult.getThrowable());
    }
    Assert.assertTrue("Not Tapestry module", TapestryUtils.isTapestryModule(myModule));
    Assert.assertNotNull("No TapestryModuleSupportLoader", TapestryModuleSupportLoader.getInstance(myModule));
    final TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(myModule);
    Assert.assertNotNull("No TapestryProject", tapestryProject);
    Assert.assertNotNull(tapestryProject.getApplicationRootPackage());
    Assert.assertNotNull(tapestryProject.getApplicationLibrary());
    return runResult.getResultObject();
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath());
    moduleBuilder.addSourceRoot("");
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    addTapestryLibraries(moduleBuilder);
  }

  protected void addTapestryLibraries(final JavaModuleFixtureBuilder moduleBuilder) {
    //PsiTestUtil.addLibrary(myModule, "JavaEE", PathManager.getHomePath(), "/lib/", "javaee.jar", "javase-javax.jar");
    moduleBuilder.addLibraryJars("tapestry_5.1.0.5", getCommonTestDataPath() + "libs", "antlr-runtime-3.1.1.jar", "commons-codec.jar",
                                 "javassist.jar", "log4j.jar", "slf4j-api.jar", "slf4j-log4j12.jar", "stax2.jar",
                                 "tapestry5-annotations.jar", "tapestry-core.jar", "tapestry-ioc.jar", "wstx-asl.jar");
  }

  protected String getElementTagName() {
    return "t:" + getElementName().toLowerCase();
  }

  protected String getElementName() {
    return getTestName(false);
  }

  protected String getElementClassFileName() {
    return getElementName() + ".java";
  }

  protected String getElementTemplateFileName() {
    return getElementName() + ".tml";
  }

  protected void initByComponent() throws IOException {
    copyOrCreateElementClassFile(getElementClassFileName());
    final String tName = getElementTemplateFileName();
    VirtualFile vFile = myFixture.copyFileToProject(tName, COMPONENTS_PACKAGE_PATH + tName);
    myFixture.configureFromExistingVirtualFile(vFile);
  }

  protected File getFileByPath(@NonNls String filePath) {
    return new File(myFixture.getTestDataPath() + "/" + filePath);
  }

  protected void copyOrCreateElementClassFile(@NonNls String classFileName) throws IOException {
    String targetPath = COMPONENTS_PACKAGE_PATH + classFileName;
    if (getFileByPath(classFileName).exists()) {
      myFixture.copyFileToProject(classFileName, targetPath);
    }
    else {
      myFixture.addFileToProject(targetPath,
                                 "package " + TEST_APPLICATION_PACKAGE + "." + COMPONENTS + "; public class " + getElementName() + " {}");
    }
  }

  protected void addComponentToProject(String className) throws IOException {
    addElementToProject(COMPONENTS_PACKAGE_PATH, className, ".java");
  }

  protected void addPageToProject(String className) throws IOException {
    addElementToProject(PAGES_PACKAGE_PATH, className, ".tml");
    addElementToProject(PAGES_PACKAGE_PATH, className, ".java");
  }

  private void addElementToProject(String relativePath, String className, String ext) throws IOException {
    String targetPath = relativePath + className + ext;
    Assert.assertNotNull(myFixture.addFileToProject(targetPath, getCommonTestDataFileText(className + ext)));
  }

  protected String getCommonTestDataFileText(@NotNull String fileName) throws IOException {
    File file = new File(getCommonTestDataPath() + "/" + fileName);
    Assert.assertTrue(file + " doesn't exists", file.exists());
    return FileUtil.loadTextAndClose(new FileReader(file));
  }

  protected String[] mergeArrays(String[] array, @NonNls String... list) throws Throwable {
    return ArrayUtil.mergeArrays(array, list, String.class);
  }

  @Override
  protected void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
    myProject = null;
    myModule = null;
    super.tearDown();
  }
}



