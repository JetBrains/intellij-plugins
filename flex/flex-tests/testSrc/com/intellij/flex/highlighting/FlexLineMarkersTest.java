// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.daemon.impl.analysis.XmlPathReferenceInspection;
import com.intellij.codeInsight.navigation.ImplementationSearcher;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.search.JSFunctionsSearch;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.ExtensionTestUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.intellij.codeInsight.daemon.impl.HighlightInfoFilter.EXTENSION_POINT_NAME;

public class FlexLineMarkersTest extends ActionScriptDaemonAnalyzerTestCase {
  @NonNls static final String BASE_PATH = "flex_highlighting";

  protected Runnable myAfterCommitRunnable = null;

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface NeedsJavaModule {
  }

  @Override
  protected Collection<HighlightInfo> defaultTest() {
    return doTestFor(true, getTestName(false) + ".mxml");
  }

  @Override
  protected void doCommitModel(@NotNull final ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    FlexTestUtils.setupFlexLib(myProject, getClass(), getTestName(false));
    FlexTestUtils.addFlexUnitLib(getClass(), getTestName(false), getModule(), FlexTestUtils.getTestDataPath("flexUnit"),
                                 FlexUnitLibs.FLEX_UNIT_0_9_SWC, FlexUnitLibs.FLEX_UNIT_4_SWC);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  private boolean needsJavaModule() {
    final Method method = JSTestUtils.getTestMethod(getClass(), getTestName(false));
    assertNotNull(method);
    return method.getAnnotation(NeedsJavaModule.class) != null;
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    myAfterCommitRunnable = null;
    enableInspectionTool(new XmlPathReferenceInspection());
    suppressXmlNSAnnotator();
  }

  private void suppressXmlNSAnnotator() {
    HighlightInfoFilter filter = (info, file) -> info.forcedTextAttributesKey != XmlHighlighterColors.XML_NS_PREFIX;
    ExtensionTestUtil.maskExtensions(EXTENSION_POINT_NAME, Collections.singletonList(filter), getTestRootDisposable());
  }

  @Override
  protected void tearDown() throws Exception {
    myAfterCommitRunnable = null;
    super.tearDown();
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return needsJavaModule() ? StdModuleTypes.JAVA : FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    if (!needsJavaModule()) {
      FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
    }
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverridingMarkers() throws Exception {
    final String testName = getTestName(false);
    doTestFor(true, (Runnable)null, testName + ".mxml", testName + "_2.mxml");
    invokeNamedActionWithExpectedFileCheck(testName, "OverrideMethods", "mxml");
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testHighlightStaticInstanceMembers() {
    defaultTest();
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverridingMarkersXmlBacked() {
    doTestFor(true, getTestName(false) + "_B.mxml", getTestName(false) + "_A.mxml", getTestName(false) + "_C.mxml",
              getTestName(false) + "_D.as", getTestName(false) + "_MyInterface.as");
    int offset = myEditor.getCaretModel().getOffset();
    PsiElement source = InjectedLanguageUtil.findElementAtNoCommit(myFile, offset);
    source = PsiTreeUtil.getParentOfType(source, JSFunction.class);
    PsiElement[] functions = new ImplementationSearcher().searchImplementations(source, myEditor, true, true);
    assertEquals(3, functions.length);
    Collection<String> classNames = new ArrayList<>();
    for (PsiElement function : functions) {
      assertEquals("foo", ((JSFunction)function).getName());
      PsiElement clazz = function.getParent();
      if (clazz instanceof JSFile) {
        clazz = JSResolveUtil.getXmlBackedClass((JSFile)clazz);
      }
      classNames.add(((JSClass)clazz).getName());
    }
    assertTrue(classNames.contains(getTestName(false) + "_B"));
    assertTrue(classNames.contains(getTestName(false) + "_C"));
    assertTrue(classNames.contains(getTestName(false) + "_D"));
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverridingMarkersXmlBacked2() {
    doTestFor(true, getTestName(false) + "_MyInterface.as", getTestName(false) + "_A.mxml", getTestName(false) + "_B.mxml");
    int offset = myEditor.getCaretModel().getOffset();
    PsiElement source = myFile.findElementAt(offset);
    source = PsiTreeUtil.getParentOfType(source, JSFunction.class);
    PsiElement[] functions = new ImplementationSearcher().searchImplementations(source, myEditor, true, true);
    assertEquals(2, functions.length);
    //assertEquals(3, functions.length); IDEADEV-34319
    Collection<String> classNames = new ArrayList<>();
    for (PsiElement function : functions) {
      assertEquals("bar", ((JSFunction)function).getName());
      PsiElement clazz = function.getParent();
      if (clazz instanceof JSFile) {
        clazz = JSResolveUtil.getXmlBackedClass((JSFile)clazz);
      }
      classNames.add(((JSClass)clazz).getName());
    }
    assertTrue(classNames.contains(getTestName(false) + "_MyInterface"));
    assertTrue(classNames.contains(getTestName(false) + "_A"));
    //assertTrue(classNames.contains(getTestName(false) +"_B")); IDEADEV-34319
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverridingMarkersInlineComponents() {
    doTestFor(true, getTestName(false) + ".mxml");

    int offset = myEditor.getCaretModel().getOffset();
    PsiElement source = InjectedLanguageUtil.findElementAtNoCommit(myFile, offset);
    source = PsiTreeUtil.getParentOfType(source, JSFunction.class);
    Collection<JSClass> classes = JSInheritanceUtil.findDeclaringClasses((JSFunction)source);
    assertEquals(1, classes.size());
    assertEquals("mx.core.UIComponent", classes.iterator().next().getQualifiedName());
    JSFunction baseFunction = classes.iterator().next().findFunctionByName(((JSFunction)source).getName());
    Collection<JSFunction> implementations = JSFunctionsSearch.searchOverridingFunctions(baseFunction, true).findAll();
    assertEquals(2, implementations.size());
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverriddenMarkersInMxml1() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testOverriddenMarkersInMxml2() {
    doTestFor(true, getTestName(false) + ".mxml", getTestName(false) + "_2.as");
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testImplicitImplementMarker() {
    doTestFor(true, getTestName(false) + "Interface.as", getTestName(false) + "Base.mxml", getTestName(false) + ".mxml");
  }

  @JSTestOptions(JSTestOption.WithLineMarkers)
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testFlexWithMockFlexWithLineMarkers() {
    defaultTest();
  }
}
