// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

import static com.intellij.flex.highlighting.ActionScriptHighlightingTest.invokeShowImplemenationsForLineMarker;
import static com.intellij.flex.highlighting.ActionScriptHighlightingTest.invokeShowImplementations;

public class ActionScriptLineMarkersTest extends ActionScriptDaemonAnalyzerTestCase {
  @NonNls private static final String BASE_PATH = "/js2_highlighting";
  protected Runnable myAfterCommitRunnable = null;

  @Override
  protected boolean isIconRequired() {
    return true;
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  @NonNls
  protected String getExtension() {
    return "js2";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    myAfterCommitRunnable = null;
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

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }


  @Nullable
  private PsiElement invokeGotoSuperMethodAction(@NonNls String destinationClassName) {
    return invokeActionWithCheck(destinationClassName);
  }

  @Nullable
  private PsiElement invokeActionWithCheck(@NonNls String destinationClassName) {
    PlatformTestUtil.invokeNamedAction(IdeActions.ACTION_GOTO_SUPER);
    final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
    JSClass clazz = PsiTreeUtil.getParentOfType(at, JSClass.class);

    assertEquals(destinationClassName, clazz.getName());

    return at;
  }

  private static void checkSetProperty(@Nullable PsiElement at) {
    final JSFunction parentOfType = PsiTreeUtil.getParentOfType(at, JSFunction.class, false);
    assertNotNull(parentOfType);
    assertTrue(parentOfType.isSetProperty());
  }

  private static void checkGetProperty(@Nullable PsiElement at) {
    final JSFunction parentOfType = PsiTreeUtil.getParentOfType(at, JSFunction.class, false);
    assertNotNull(parentOfType);
    assertTrue(parentOfType.isGetProperty());
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  public void testUnusedSymbols4() throws Exception {
    enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doSimpleHighlightingWithInvokeFixAndCheckResult("Remove unused inner class 'Foo'");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testHighlightStaticInstanceMembers() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testGenerics() {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkersWithLineMarkers() {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    defaultTest();
  }


  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkers3() {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, () -> {
      checkGetProperty(myFile.findElementAt(myEditor.getCaretModel().getOffset()));
      PsiElement at = invokeGotoSuperMethodAction("AAA");
      checkGetProperty(at);
      at = invokeShowImplementations(JSFunction.class, at);
      checkGetProperty(at);
      at = invokeGotoSuperMethodAction("IAAA");
      checkGetProperty(at);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithLineMarkers})
  public void testShowImplementationsFromInterface2() {
    doTestFor(true, () -> {
      final PsiElement at = myFile.findElementAt(myEditor.getCaretModel().getOffset());
      JSTestUtils.invokeShowImplementations(JSClass.class, at, 2, false);
      invokeShowImplemenationsForLineMarker(at, 2);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testImplementingMarkerFromSwc() {
    myAfterCommitRunnable =
      () -> FlexTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + BASE_PATH, "ImplementingMarkerFromSwc.swc", null, null);
    doTestFor(true, getTestName(false) + ".as");
  }


  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testNoOverrideForInternal() {
    DaemonCodeAnalyzerSettings myDaemonCodeAnalyzerSettings = DaemonCodeAnalyzerSettings.getInstance();
    myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS = true;
    try {
      defaultTest();
    }
    finally {
      myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS = false;
    }
  }


  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithJsSupportLoader})
  public void testOverridingMarkers2() {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, () -> {
      checkSetProperty(myFile.findElementAt(myEditor.getCaretModel().getOffset()));
      PsiElement at = invokeGotoSuperMethodAction("AAA");
      checkSetProperty(at);
      at = invokeShowImplementations(JSFunction.class, at);
      checkSetProperty(at);
      at = invokeGotoSuperMethodAction("IAAA");
      checkSetProperty(at);
    }, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithLineMarkers})
  public void testImplicitImplementMarker() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithLineMarkers})
  public void testNullQualifiedName() {
    doTestFor(true, getTestName(false) + ".as");
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  public void testStaticMethodDoesNotImplement() throws Exception {
    final Collection<HighlightInfo> infos = doTestFor(true, getTestName(false) + ".as");
    invokeGotoSuperMethodAction("Impl");
    findAndInvokeActionWithExpectedCheck(JavaScriptBundle.message("javascript.fix.implement.methods"), "as", infos);
  }

  @JSTestOptions({JSTestOption.WithLineMarkers, JSTestOption.WithoutSourceRoot})
  public void testLineMarkersInLibrarySource() {
    myAfterCommitRunnable = new Runnable() {
      @Override
      public void run() {
        VirtualFile file = ModuleRootManager.getInstance(myModule).getContentEntries()[0].getFile();
        VirtualFile fakeClassFile = findVirtualFile(BASE_PATH + "/" + getTestName(false) + "_2.js2");
        try {
          VirtualFile classesDir = file.createChildDirectory(this, "classes");
          VfsUtilCore.copyFile(this, fakeClassFile, classesDir);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
        FlexTestUtils.addFlexLibrary(true, myModule, "lib", true, file.getPath(), "classes", "", null);

        ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
        model.removeContentEntry(model.getContentEntries()[0]);
        model.commit();
      }
    };

    doTestFor(true, getTestName(false) + ".js2");
  }

  @JSTestOptions({JSTestOption.WithInfos, JSTestOption.WithSymbolNames})
  public void testSemanticHighlighting() {
    defaultTest(); // IDEA-110040
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithLineMarkers})
  public void testImplementsAndImplementedMarkers() {
    //enableInspectionTool(new JSUnusedLocalSymbolsInspection());
    doTestFor(true, () -> {
      PsiElement at = invokeGotoSuperMethodAction("SecondInterface");
      JSTestUtils.invokeShowImplementations(JSFunction.class, at, 3, false);
      invokeShowImplemenationsForLineMarker(at, 5);
    }, getTestName(false) + ".js2");
  }
}
