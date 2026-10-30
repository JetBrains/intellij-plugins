// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSStructureViewFixture;
import com.intellij.lang.javascript.StructureViewTestUtil;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.structureView.JSStructureViewModel;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.flex.util.FlexTestUtils.getPathToMockFlex;
import static com.intellij.lang.javascript.JSStructureViewFixture.getChildren;
import static com.intellij.lang.javascript.JSStructureViewFixture.getText;
import static com.intellij.lang.javascript.JSStructureViewFixture.jsStructureViewFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class FlexStructureViewTest {
  private static final String BASE_PATH = "/as_fileStructure/";
  private static final int OBJECT_METHODS_COUNT = 11;

  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSStructureViewFixture> js =
    jsStructureViewFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath(""))
        .withBasePath(BASE_PATH)
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getTestRootDisposable(), "");
    // The JUnit 3 project descriptor gave the module a mock Flex SDK for the test.
    Sdk sdk = FlexTestUtils.createSdk(getPathToMockFlex(FlexStructureViewTest.class, js.get().getTestName(false)), "3.4.0", false, false,
                                      js.get().getTestRootDisposable());
    WriteAction.runAndWait(() -> {
      ModifiableRootModel model = ModuleRootManager.getInstance(js.get().getFixture().getModule()).getModifiableModel();
      model.setSdk(sdk);
      model.commit();
    });
  }

  @Test
  public void testECMAL4Structure()  {
    js.get().getFixture().configureByFile(BASE_PATH + "15.js2");
    js.get().checkStructureView(true);
  }

  @Test
  public void testGroupByClass2()  {
    js.get().runTest(() -> {
      js.get().getFixture().configureByFile(BASE_PATH + "15.js2");
      Object[] items = js.get().getTopLevelItems();
      assertEquals(3, items.length);
      assertEquals("ZZZ", getText(items[1]));
      PsiElement element = js.get().checkIfCurrentEditorElementIsValid((AbstractTreeNode<?>)items[1], JSClass.class);
      assertTrue(JSStructureViewModel.shouldEnterElementStatic(element));
    });
  }

  @Test
  public void testGroupByClass3() {
    js.get().runTest(() -> {
      js.get().getFixture().configureByFiles(BASE_PATH + "15_2.js2", BASE_PATH + "15_3.js2");
      Object[] items = js.get().getTopLevelItems();
      assertEquals(1, items.length);

      assertEquals("A15", getText(items[0]));

      List<AbstractTreeNode<?>> treeNodes = getChildren(items[0]);
      assertEquals(3, treeNodes.size());

      assertEquals("mypackage.ZZZ", getText(treeNodes.get(0)));
      assertEquals("Object", getText(treeNodes.get(1)));

      List<AbstractTreeNode<?>> treeNodes2 = getChildren(treeNodes.get(0));
      assertEquals(2, treeNodes2.size());

      assertEquals("aaa:*", getText(treeNodes2.get(0)));
      assertEquals("c2:*", getText(treeNodes2.get(1)));

      js.get().checkIfCurrentEditorElementIsValid(treeNodes.get(2), JSFunction.class);
    });
  }

  @Test
  public void testBug3() {
    js.get().getFixture().configureByFiles(BASE_PATH + "22.js2", BASE_PATH + "22_2.js2");
    js.get().checkStructureView(true);
  }

  @Test
  public void testIncludedMembers() {
    js.get().runTest(() -> {
      js.get().getFixture().configureByFiles(BASE_PATH + "IncludedMembers.js2", BASE_PATH + "IncludedMembers_2.js2");

      Object[] items = js.get().getTopLevelItems();
      assertEquals(1, items.length);

      assertEquals("C", getText(items[0]));
      List<AbstractTreeNode<?>> treeNodes = getChildren(items[0]);
      assertEquals(2 + OBJECT_METHODS_COUNT, treeNodes.size());
      assertEquals("__aaa__():*", getText(treeNodes.get(0)));
    });
  }

  @Test
  public void testInherited1()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      myFixture.configureByFile(BASE_PATH + "Inherited1.as");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor());
    });
  }

  @Test
  public void testInherited2()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      myFixture.configureByFiles(BASE_PATH + "Inherited2.as", BASE_PATH + "Inherited2_2.as");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor());
    });
  }

  @Test
  public void testInherited3()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      myFixture.configureByFiles(BASE_PATH + "Inherited3.as",
                       BASE_PATH + "Inherited3_1.as",
                       BASE_PATH + "Inherited3_2.as",
                       BASE_PATH + "Inherited3_3.as",
                       BASE_PATH + "Inherited3_4.as");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor());
    });
  }

  @Test
  public void testWithNameAttribute()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      myFixture.configureByFile(BASE_PATH + js.get().getTestName(false) + ".mxml");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor());
    });
  }

  @Test
  public void testWithIncludedFiles1()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      myFixture.configureByFiles(BASE_PATH + js.get().getTestName(false) + ".mxml",
                       BASE_PATH + js.get().getTestName(false) + "_1.css",
                       BASE_PATH + js.get().getTestName(false) + "_2.as",
                       BASE_PATH + js.get().getTestName(false) + "_3.as",
                       BASE_PATH + js.get().getTestName(false) + "_4.as");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor());
    });
  }

  @Test
  public void testSuperClasses()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      String s = BASE_PATH + js.get().getTestName(false);
      myFixture.configureByFiles(s + ".as", s + "_2.as", s + "_3.as", s + "_4.as");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor());
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testLocalVarDerived3()  {
    js.get().trivialOneFileTest("js2");
  }

  @Test
  public void testNsDeclShouldBeOneTime()  {
    js.get().trivialOneFileTest("js2");
  }

  @Test
  public void testExtraTopMembers()  {
    js.get().trivialOneFileTest("js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testObjectFields()  {
    js.get().runTest(() -> {
      CodeInsightTestFixture myFixture = js.get().getFixture();
      String s = BASE_PATH + js.get().getTestName(false);
      myFixture.configureByFiles(s + ".js2");
      StructureViewTestUtil.checkStructureView(myFixture.getEditor(), false);
    });
  }
}
