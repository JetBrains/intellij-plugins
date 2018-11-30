package com.intellij.tapestry.tests;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.facet.TapestryFacet;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Alexey Chmutov
 */
public abstract class TapestryBaseTestCase extends UsefulTestCase {
  static final String TEST_APPLICATION_PACKAGE = "com.testapp";
  static final String COMPONENTS = "components";
  static final String ABSTRACT_COMPONENTS = "base";
  static final String PAGES = "pages";
  static final String MIXINS = "mixins";
  static final String COMPONENTS_PACKAGE_PATH = TEST_APPLICATION_PACKAGE.replace('.', '/') + "/" + COMPONENTS + "/";
  static final String ABSTRACT_COMPONENTS_PACKAGE_PATH = TEST_APPLICATION_PACKAGE.replace('.', '/') + "/" + ABSTRACT_COMPONENTS + "/";
  static final String MIXINS_PACKAGE_PATH = TEST_APPLICATION_PACKAGE.replace('.', '/') + "/" + MIXINS + "/";
  static final String PAGES_PACKAGE_PATH = TEST_APPLICATION_PACKAGE.replace('.', '/') + "/" + PAGES + "/";

  @NonNls
  protected abstract String getBasePath();

  @NonNls
  protected final String getTestDataPath() {
    return Util.getCommonTestDataPath() + getBasePath();
  }

  protected CodeInsightTestFixture myFixture;
  protected Module myModule;

  protected Class<? extends JavaModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return JavaModuleFixtureBuilder.class;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = JavaTestFixtureFactory.createFixtureBuilder(getName());
    JavaModuleFixtureBuilder moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());
    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());
    myFixture.setTestDataPath(getTestDataPath());
    configureModule(moduleBuilder);

    myFixture.setUp();
    myModule = moduleBuilder.getFixture().getModule();

    createFacet();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFixture.tearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      myFixture = null;
      myModule = null;
      super.tearDown();
    }
  }

  protected TapestryFacet createFacet() {
    return WriteCommandAction.runWriteCommandAction(myFixture.getProject(), (Computable<TapestryFacet>)() -> {
        final TapestryFacetType facetType = TapestryFacetType.getInstance();
        final FacetManager facetManager = FacetManager.getInstance(myModule);
        final TapestryFacet facet = facetManager.addFacet(facetType, facetType.getPresentableName(), null);
        facet.getConfiguration().setApplicationPackage(TEST_APPLICATION_PACKAGE);
        Assert.assertNotNull(facetManager.getFacetByType(TapestryFacetType.ID));
        Assert.assertTrue("Not Tapestry module", TapestryUtils.isTapestryModule(myModule));
        Assert.assertNotNull("No TapestryModuleSupportLoader", TapestryModuleSupportLoader.getInstance(myModule));
        final TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(myModule);
        Assert.assertNotNull("No TapestryProject", tapestryProject);
        Assert.assertNotNull(tapestryProject.getApplicationRootPackage());
        Assert.assertNotNull(tapestryProject.getApplicationLibrary());
        return facet;
      });
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath());
    moduleBuilder.addSourceRoot("");
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    addTapestryLibraries(moduleBuilder);
  }

  protected void addTapestryLibraries(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("tapestry_5.1.0.5", Util.getCommonTestDataPath() + "libs", "antlr-runtime-3.1.1.jar", "commons-codec.jar",
                                 "javassist.jar", "log4j.jar", "slf4j-api.jar", "slf4j-log4j12.jar", "stax2.jar",
                                 "tapestry5-annotations.jar", "tapestry-core.jar", "tapestry-ioc.jar", "wstx-asl.jar");
  }

  protected String getElementTagName() {
    return "t:" + getLowerCaseElementName();
  }

  protected String getLowerCaseElementName() {
    return getElementName().toLowerCase();
  }

  protected String getElementName() {
    return getTestName(false);
  }

  protected String getElementClassFileName() {
    return getElementName() + getComponentClassExtension();
  }

  protected String getComponentClassExtension() {
    return Util.DOT_JAVA;
  }

  protected String getAuxClassExtension() {
    return Util.DOT_JAVA;
  }

  protected String getTemplateExtension() {
    return Util.DOT_TML;
  }

  protected String getElementTemplateFileName() {
    return getElementName() + getTemplateExtension();
  }

  protected void initByComponent() {
    initByComponent(true);
  }

  @NotNull
  protected VirtualFile initByComponent(boolean configureByTmlNotJava) {
    VirtualFile javaFile = copyOrCreateComponentClassFile();
    final String tmlName = getElementTemplateFileName();

    boolean copyTmlFile = configureByTmlNotJava || new File(myFixture.getTestDataPath() + "/" + tmlName).exists();
    VirtualFile tmlFile = copyTmlFile ? myFixture.copyFileToProject(tmlName, COMPONENTS_PACKAGE_PATH + tmlName) : null;
    final VirtualFile result = configureByTmlNotJava ? tmlFile : javaFile;
    myFixture.configureFromExistingVirtualFile(result);
    return result;
  }


  protected void checkResultByFile() {
    String afterFileName = getElementName() + Util.AFTER + getTemplateExtension();
    myFixture.checkResultByFile(afterFileName);
  }

  protected File getFileByPath(@NonNls String filePath) {
    return new File(myFixture.getTestDataPath() + "/" + filePath);
  }

  protected VirtualFile copyOrCreateComponentClassFile() {
    String existingComponentClassFile = getExistingComponentClassFileName();
    String targetPath = COMPONENTS_PACKAGE_PATH + getElementClassFileName();
    final VirtualFile destFile;
    if (existingComponentClassFile != null) {
      destFile = myFixture.copyFileToProject(existingComponentClassFile, targetPath);
      myFixture.allowTreeAccessForFile(destFile);
    }
    else {
      addFileAndAllowTreeAccess(targetPath,
                                "package " + TEST_APPLICATION_PACKAGE + "." + COMPONENTS + "; public class " + getElementName() + " {}");
      File ioFile = new File(myFixture.getTempDirPath() + "/" + targetPath);
      destFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(ioFile);
    }
    Assert.assertNotNull(destFile);
    return destFile;
  }

  @Nullable
  protected String getExistingComponentClassFileName() {
    return checkTestDataFileExists(getElementClassFileName());
  }

  @Nullable
  protected String checkTestDataFileExists(String fileName) {
    return getFileByPath(fileName).exists() ? fileName : null;
  }

  protected void addComponentToProject(String className) {
    addElementToProject(COMPONENTS_PACKAGE_PATH, className, getAuxClassExtension());
  }

  protected void addAbstractComponentToProject(String className) {
    addElementToProject(ABSTRACT_COMPONENTS_PACKAGE_PATH, className, getAuxClassExtension());
  }

  protected void addMixinToProject(String className) {
    addElementToProject(MIXINS_PACKAGE_PATH, className, getAuxClassExtension());
  }

  protected VirtualFile addPageToProject(String className) {
    addElementToProject(PAGES_PACKAGE_PATH, className, getAuxClassExtension());
    return addElementToProject(PAGES_PACKAGE_PATH, className, getTemplateExtension());
  }

  protected VirtualFile addElementToProject(String relativePath, String className, String ext) {
    final int afterDotIndex = className.lastIndexOf('.');
    String fileText;
    if (afterDotIndex != -1) { // we want the element to be placed in the subpackage
      final String subpackage = className.substring(0, afterDotIndex);
      relativePath += subpackage.replace('.', '/') + '/';
      className = className.substring(afterDotIndex + 1);
      fileText = Util.getCommonTestDataFileText(className + ext);
      if (fileText.startsWith("package " + TEST_APPLICATION_PACKAGE)) {
        int toPasteSubpackageIndex = fileText.indexOf(';');
        fileText = fileText.substring(0, toPasteSubpackageIndex) + '.' + subpackage + fileText.substring(toPasteSubpackageIndex);
      }
    }
    else {
      fileText = Util.getCommonTestDataFileText(className + ext);
    }
    return addFileAndAllowTreeAccess(relativePath + className + ext, fileText);
  }

  private VirtualFile addFileAndAllowTreeAccess(String targetPath, String fileText) {
    final PsiFile file = myFixture.addFileToProject(targetPath, fileText);
    Assert.assertNotNull(file);
    final VirtualFile virtualFile = file.getVirtualFile();
    Assert.assertNotNull(virtualFile);
    myFixture.allowTreeAccessForFile(virtualFile);
    return virtualFile;
  }

  @Nullable
  protected PsiReference getReferenceAtCaretPosition() {
    return myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());
  }

  @NotNull
  protected PsiElement resolveReferenceAtCaretPosition() {
    PsiReference ref = getReferenceAtCaretPosition();
    Assert.assertNotNull("No reference at caret", ref);
    final PsiElement element = ref.resolve();
    Assert.assertNotNull("unresolved reference '" + ref.getCanonicalText() + "'", element);
    return element;
  }
}



