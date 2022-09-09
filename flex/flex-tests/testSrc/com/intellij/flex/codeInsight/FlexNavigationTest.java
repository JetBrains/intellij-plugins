// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.codeInsight;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.documentation.FlexDocumentationProvider;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("ConstantConditions")
public class FlexNavigationTest extends JavaCodeInsightTestCase {
  private static final String BASE_PATH = "/flex_navigation/";

  protected Runnable myAfterCommitRunnable = null;


  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    myAfterCommitRunnable = null;
  }

  @Override
  protected void tearDown() throws Exception {
    myAfterCommitRunnable = null;
    super.tearDown();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  private void doTest(String filename, @Nullable VirtualFile expectedForSource, @Nullable VirtualFile expectedForDoc) throws Exception {
    doTest(filename, expectedForSource, expectedForDoc, null);
  }

  private static void checkClassName(@Nullable String expectedClassName, @NotNull PsiElement element) {
    if (expectedClassName == null) return;
    JSClass jsClass = PsiTreeUtil.getParentOfType(element, JSClass.class);
    assertNotNull("Parent class not found", jsClass);
    assertEquals(expectedClassName, jsClass.getName());
  }

  private void doTest(String filename,
                      @Nullable VirtualFile expectedForSource,
                      @Nullable VirtualFile expectedForDoc,
                      @Nullable String expectedClassName) throws Exception {
    configureByFile(BASE_PATH + filename);
    doTest(expectedForSource, expectedForDoc, expectedClassName);
  }

  private void doTest(VirtualFile expectedForSource, VirtualFile expectedForDoc, String expectedClassName) {
    doTest(myEditor, expectedForSource, expectedForDoc, expectedClassName, null);
  }

  public static void doTest(Editor editor,
                            @Nullable VirtualFile expectedForSource,
                            @Nullable VirtualFile expectedForDoc,
                            @Nullable String expectedClassName,
                            @Nullable Consumer<PsiElement> customCheck) {
    PsiElement clazz = TargetElementUtil
      .findTargetElement(editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);

    Collection<PsiElement> elementsToCheck;
    GlobalSearchScope scope;
    if (!(clazz instanceof JSQualifiedNamedElement)) {
      PsiReference reference = TargetElementUtil.findReference(editor);
      assertNotNull(reference);
      elementsToCheck = TargetElementUtil.getInstance().getTargetCandidates(reference);
      scope = null;
    }
    else {
      elementsToCheck = Arrays.asList(clazz);
      final PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
      final PsiElement elementAt = file.findElementAt(editor.getCaretModel().getOffset());
      scope = JSResolveUtil.getResolveScope(elementAt);
    }

    assertTrue("Target elements not found", elementsToCheck.size() > 0);
    for (PsiElement element : elementsToCheck) {
      if (element instanceof JSQualifiedNamedElement) {
        String qName = ((JSQualifiedNamedElement)element).getQualifiedName();
        GlobalSearchScope searchScope = scope != null ? scope : JSResolveUtil.getResolveScope(element);
        final Collection<JSQualifiedNamedElement> candidates =
          StubIndex.getElements(JSQualifiedElementIndex.KEY, qName, editor.getProject(), searchScope,
                                JSQualifiedNamedElement.class);
        for (JSQualifiedNamedElement candidate : candidates) {
          if (!qName.equals(candidate.getQualifiedName())) {
            continue;
          }
          doCheck(candidate, expectedForSource, expectedForDoc, customCheck);
          checkClassName(expectedClassName, candidate);
        }
      }
      else {
        doCheck(element, expectedForSource, expectedForDoc, customCheck);
        checkClassName(expectedClassName, element);
      }
    }
  }

  private static void doCheck(PsiElement element, @Nullable VirtualFile expectedForSource, @Nullable VirtualFile expectedForDoc,
                              @Nullable Consumer<PsiElement> customCheck) {
    if (customCheck != null) {
      customCheck.consume(element);
    }

    if (expectedForSource != null) {
      PsiElement source = element.getNavigationElement();
      final String expected = FileUtil.toCanonicalPath(expectedForSource.getUrl());
      final String actual = FileUtil.toCanonicalPath(source.getContainingFile().getVirtualFile().getUrl());
      assertEquals("element with source", expected, actual);
    }
    if (expectedForDoc != null) {
      PsiElement doc = FlexDocumentationProvider.findTopLevelNavigationElement((JSQualifiedNamedElement)element);
      final String expected = FileUtil.toCanonicalPath(expectedForDoc.getUrl());
      final String actual = FileUtil.toCanonicalPath(doc.getContainingFile().getVirtualFile().getUrl());
      assertEquals("element with asdoc", expected, actual);
    }
  }

  private VirtualFile getFile(String relativePath) {
    return VirtualFileManager.getInstance()
      .findFileByUrl("jar://" + FileUtil.toSystemIndependentName(getTestDataPath()) + BASE_PATH + relativePath);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testLibraryClass1() throws Exception {
    final String sources = "TestLibSources.zip";
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "TestLib1.swc", null, null);
      FlexTestUtils.addLibrary(myModule, "LibWithSources", getTestDataPath() + BASE_PATH, "TestLib2.swc", sources, null);
      FlexTestUtils.addLibrary(myModule, "LibWithAsdoc", getTestDataPath() + BASE_PATH, "TestLib3.swc", null, "TestLibAsdoc.zip");
    };

    final VirtualFile forSource = getFile(sources + "!/com/test/MyButton.as");
    doTest("LibraryClass.as", forSource, forSource);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testLibraryClass2() throws Exception {
    final String libWithAsDocSwc = "TestLib3.swc";
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "TestLib1.swc", null, null);
      FlexTestUtils.addLibrary(myModule, "LibWithAsdoc", getTestDataPath() + BASE_PATH, libWithAsDocSwc, null, "TestLibAsdoc.zip");
    };

    final VirtualFile forAsdoc = getFile(libWithAsDocSwc + "!/library.swf");
    doTest("LibraryClass.as", null, forAsdoc);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testLibraryClass3() throws Exception {
    myAfterCommitRunnable = () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "TestLib1.swc", null, null);

    final VirtualFile forSource = getFile("TestLib1.swc!/library.swf");
    doTest("LibraryClass.as", forSource, forSource);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testSdkClass1() throws Exception {
    VirtualFile asdoc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + "SdkAsdoc.zip");
    asdoc = JarFileSystem.getInstance().getJarRootForLocalFile(asdoc);
    VirtualFile swc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + "CustomSdk.swc");
    swc = JarFileSystem.getInstance().getJarRootForLocalFile(swc);

    FlexTestUtils.setupCustomSdk(myModule, swc, null, asdoc);

    myAfterCommitRunnable = () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "TestLib1.swc", null, null);

    VirtualFile forAsDoc = swc.findChild("library.swf");
    doTest("SdkClass.as", null, forAsDoc);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testSdkClass2() throws Exception {
    VirtualFile asdoc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + "SdkAsdoc.zip");
    asdoc = JarFileSystem.getInstance().getJarRootForLocalFile(asdoc);
    VirtualFile swc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + "CustomSdk.swc");
    swc = JarFileSystem.getInstance().getJarRootForLocalFile(swc);

    FlexTestUtils.setupCustomSdk(myModule, swc, null, asdoc);

    myAfterCommitRunnable = () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "TestLib1.swc", null, null);

    VirtualFile forAsDoc = swc.findChild("library.swf");
    doTest("SdkClass.as", null, forAsDoc);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testAmbiguousCssSelector() throws Exception {
    final String sources = "StyleableLibSources.zip";
    myAfterCommitRunnable = () -> {
      FlexTestUtils.addLibrary(myModule, "Lib1", getTestDataPath() + BASE_PATH, "StyleableLib.swc", sources, null);
      FlexTestUtils.addLibrary(myModule, "Lib2", getTestDataPath() + BASE_PATH, "StyleableLib1.swc", null, null);
    };
    final VirtualFile styleableFile = getFile(sources + "!/foo/Styleable1.as");
    doTest(getTestName(false) + ".css", styleableFile, null);
    PsiFile file = PsiManager.getInstance(myProject).findFile(styleableFile);
    if (file instanceof PsiFileImpl) {
      assertNull("File should not be parsed", ((PsiFileImpl)file).getTreeElement());
    }
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testIncludes() {
    VirtualFile vFile = configureByFiles(null, BASE_PATH + "Includes.css", BASE_PATH + "Includes.as", BASE_PATH + "Includes1.as");
    VirtualFile file = vFile.getParent().findChild("Includes1.as");
    doTest(file, file, null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyForCustomClass1() throws Exception {
    doCustomClassCssTest("!/foo/Styles.as", null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyForCustomClass2() throws Exception {
    doCustomClassCssTest("!/foo/Styles.as", "CssPropertyForCustomClass1");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyForCustomClass3() throws Exception {
    doCustomClassCssTest("!/foo/Styleable2.as", null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssSelector() throws Exception {
    doLibClassCssTest(true, null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssSelector1() throws Exception {
    doLibClassCssTest(true, null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testLibCss() throws Exception {
    doTestLibCss(0, 1);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testLibCss1() throws Exception {
    doTestLibCss(1, 6);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testLibCss2() throws Exception {
    doTestLibCss(5, 6);
  }


  // we cannot use <caret> method, because lib css file is read-only
  private void doTestLibCss(int line, int column) throws Exception {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "LibWithCssFile.swc", "LibWithCssFile_src.zip", null);
    configureByFile(BASE_PATH + "CssEmptyFile.css");

    final VirtualFile cssFile = getFile("LibWithCssFile.swc!/defaults.css");
    configureByExistingFile(cssFile);

    myEditor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line, column));

    final VirtualFile classFile = getFile("LibWithCssFile_src.zip!/p1/p2/MyClass.as");
    doTest(classFile, null, null);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssProperty1() throws Exception {
    doLibClassCssTest(false, "Container");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssProperty2() throws Exception {
    doLibClassCssTest(false, "Button");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssProperty3() throws Exception {
    doLibClassCssTest(false, null);
  }

  private void doCustomClassCssTest(@NotNull String expectedSourceClass, @Nullable String testName) throws Exception {
    if (testName == null) {
      testName = getTestName(false);
    }
    final String sources = "StyleableLibSources.zip";
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "StyleableLib.swc", sources, null);
    final VirtualFile includedFile = getFile(sources + expectedSourceClass);
    doTest(testName + ".css", includedFile, null);
    PsiFile file = PsiManager.getInstance(myProject).findFile(includedFile);
    if (file instanceof PsiFileImpl) {
      assertNull("File should not be parsed", ((PsiFileImpl)file).getTreeElement());
    }
  }

  private void doLibClassCssTest(boolean expectedForDoc, @Nullable String expectedClassName) throws Exception {
    String testName = getTestName(false);
    String mockFlex = FlexTestUtils.getPathToMockFlex(getClass(), testName) + "/MonkeyPatchingMockFlex.as";
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(mockFlex);
    doTest(testName + ".css", file, expectedForDoc ? file : null, expectedClassName);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testMonkeyPatching() {
    final String testName = getTestName(false);

    myAfterCommitRunnable = () -> {
      final VirtualFile sdkSrc = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + testName + "_sdk_src");
      final SdkModificator sdkModificator = FlexTestUtils.getFlexSdkModificator(myModule);
      sdkModificator.addRoot(sdkSrc, OrderRootType.SOURCES);
      sdkModificator.commitChanges();
    };

    configureByFiles(BASE_PATH + testName, BASE_PATH + testName + "/" + testName + ".as", BASE_PATH + testName + "/mx/events/FlexEvent.as");

    final VirtualFile expectedFile = LocalFileSystem.getInstance()
      .findFileByPath(ModuleRootManager.getInstance(myModule).getSourceRoots()[0].getPath() + "/mx/events/FlexEvent.as");
    assert expectedFile != null;

    final PsiReference reference = TargetElementUtil.findReference(myEditor);
    assertNotNull(reference);
    final Collection<PsiElement> candidates = TargetElementUtil.getInstance().getTargetCandidates(reference);
    assertEquals(1, candidates.size());
    doCheck(candidates.iterator().next(), expectedFile, expectedFile, null);
  }

  public void testClassWithNoExplicitConstructor() {
    final String testName = getTestName(false);

    myAfterCommitRunnable =
      () -> FlexTestUtils
        .addFlexLibrary(false, myModule, "foo", true, getTestDataPath() + BASE_PATH, testName + ".swc", testName + ".zip", null);

    configureByFiles(null, BASE_PATH + testName + ".as");

    final VirtualFile sourcesZip = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + BASE_PATH + testName + ".zip");
    VirtualFile expectedForSource = JarFileSystem.getInstance().getJarRootForLocalFile(sourcesZip).findChild("MyClass3.as");
    assertNotNull(expectedForSource);

    final PsiReference reference = TargetElementUtil.findReference(myEditor);
    assertNotNull(reference);
    final Collection<PsiElement> candidates = TargetElementUtil.getInstance().getTargetCandidates(reference);
    assertEquals(1, candidates.size());
    doCheck(candidates.iterator().next(), expectedForSource, null, null);
  }
}
