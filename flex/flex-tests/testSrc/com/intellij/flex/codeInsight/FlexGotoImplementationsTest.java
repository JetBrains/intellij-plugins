// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.codeInsight;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.codeInsight.ShowImplementationsTestUtil;
import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlexGotoImplementationsTest extends JavaCodeInsightTestCase {

  private static final String BASE_PATH = "gotoImplementations/";

  private Runnable myAfterCommitRunnable = null;

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

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  private void doTestForClass(String baseClassQualifiedName, String expected) throws Exception {
    configureByElement(baseClassQualifiedName, null);
    invokeAndCheck(expected);
  }

  private void doTestForMethod(String baseClassQualifiedName, @Nullable String methodName, int inheritors) throws Exception {
    configureByElement(baseClassQualifiedName, methodName);
    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < inheritors; i++) {
      if (expected.length() > 0) expected.append(",");
      expected.append(methodName);
    }
    invokeAndCheck(expected.toString());
  }

  // open editor for a class with specified qname
  private void configureByElement(String classQName, @Nullable String methodName) throws IOException {
    ApplicationManager.getApplication().runWriteAction((ThrowableComputable<Object, IOException>)() -> {
      myFile = null;
      myEditor = null;

      final ModuleRootManager rootManager = ModuleRootManager.getInstance(myModule);
      final ModifiableRootModel rootModel = rootManager.getModifiableModel();
      if (clearModelBeforeConfiguring()) {
        rootModel.clear();
      }

      File toDirIO = createTempDirectory();
      VirtualFile toDir =
        LocalFileSystem.getInstance().refreshAndFindFileByPath(toDirIO.getCanonicalPath().replace(File.separatorChar, '/'));

      boolean sourceRootAdded = false;
      if (isAddDirToContentRoot()) {
        // we may need a content root to add a library
        final ContentEntry contentEntry = rootModel.addContentEntry(toDir);
        if (isAddDirToSource()) {
          sourceRootAdded = true;
          contentEntry.addSourceFolder(toDir, false);
        }
      }

      doCommitModel(rootModel);

      if (sourceRootAdded) {
        sourceRootAdded(toDir);
      }
      return null;
    });

    PsiElement element = JSDialectSpecificHandlersFactory.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
      .findClassByQName(classQName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(getModule()));
    assertTrue("Class " + classQName + " not found", element instanceof JSClass);

    if (methodName != null) {
      element = ((JSClass)element).findFunctionByName(methodName);
      assertNotNull("Class " + classQName + " has not have method " + methodName, element);
    }

    PsiElement navElement = element.getNavigationElement();

    final VirtualFile virtualFile = navElement.getContainingFile().getVirtualFile();
    setActiveEditor(createEditor(virtualFile));
    myEditor.getCaretModel().moveToOffset(navElement.getTextOffset());
  }

  private void invokeAndCheck(String expected) {
    GotoTargetHandler.GotoData pair = CodeInsightTestUtil.gotoImplementation(getEditor(), getFile());
    PsiElement base = pair.source;
    PsiElement[] implementations = pair.targets;

    check(expected, implementations);

    PsiElement[] result = ShowImplementationsTestUtil.getImplementations();
    implementations = ArrayUtil.remove(result, base);
    check(expected, implementations);
  }

  private static void check(String expected, PsiElement[] implementations) {
    List<String> strings = new ArrayList<>(implementations.length);
    for (PsiElement element : implementations) {
      assertTrue("unexpected result item: " + element, element instanceof JSNamedElement);
      strings.add(((JSNamedElement)element).getName());
    }

    Collections.sort(strings);
    assertEquals(expected, StringUtil.join(strings, ","));
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testClassInheritors() throws Exception {
    addLib();
    doTestForClass("MySkin", "MySkinEx,MySkinEx2");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testClassMethodInheritors() throws Exception {
    addLib();
    doTestForMethod("MySkin", "foo", 2);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testInterfaceInheritors() throws Exception {
    addLib();
    doTestForClass("MyInt", "MyImpl,MyImpl2,MyIntEx");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testIntergaceMethodInheritors() throws Exception {
    addLib();
    doTestForMethod("MyInt", "zz", 2);
  }


  private void addLib() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "TestLib", getTestDataPath() + BASE_PATH, "untitled37lib.swc", "untitled37lib.zip", null);
  }
}
