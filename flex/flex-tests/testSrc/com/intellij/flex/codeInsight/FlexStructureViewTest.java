// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.JSAbstractStructureViewTest;
import com.intellij.lang.javascript.StructureViewTestUtil;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.structureView.JSStructureViewModel;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.flex.util.FlexTestUtils.getPathToMockFlex;

public class FlexStructureViewTest extends JSAbstractStructureViewTest {
  private static final String BASE_PATH = "/as_fileStructure/";
  private static final int OBJECT_METHODS_COUNT = 11;

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new LightProjectDescriptor() {
      @Override
      public @NotNull String getModuleTypeId() {
        return FlexModuleType.MODULE_TYPE_ID;
      }

      @Override
      public @NotNull Sdk getSdk() {
        return FlexTestUtils.createSdk(getPathToMockFlex(FlexStructureViewTest.class, getTestName(false)),
                                       "3.4.0", false, false, getTestRootDisposable());
      }
    };
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  public void testECMAL4Structure()  {
    myFixture.configureByFile(BASE_PATH + "15.js2");
    checkStructureView(true);
  }

  public void testGroupByClass2()  {
    myFixture.configureByFile(BASE_PATH + "15.js2");
    Object[] items = getTopLevelItems();
    assertEquals(3, items.length);
    assertEquals("ZZZ", getText(items[1]));
    PsiElement element = checkIfCurrentEditorElementIsValid((AbstractTreeNode<?>)items[1], JSClass.class);
    assertTrue(JSStructureViewModel.shouldEnterElementStatic(element));
  }

  public void testGroupByClass3() {
    myFixture.configureByFiles(BASE_PATH + "15_2.js2", BASE_PATH + "15_3.js2");
    Object[] items = getTopLevelItems();
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

    checkIfCurrentEditorElementIsValid(treeNodes.get(2), JSFunction.class);
  }

  public void testBug3() {
    myFixture.configureByFiles(BASE_PATH + "22.js2", BASE_PATH + "22_2.js2");
    checkStructureView(true);
  }

  public void testIncludedMembers() {
    myFixture.configureByFiles(BASE_PATH + "IncludedMembers.js2", BASE_PATH + "IncludedMembers_2.js2");

    Object[] items = getTopLevelItems();
    assertEquals(1, items.length);

    assertEquals("C", getText(items[0]));
    List<AbstractTreeNode<?>> treeNodes = getChildren(items[0]);
    assertEquals(2 + OBJECT_METHODS_COUNT, treeNodes.size());
    assertEquals("__aaa__():*", getText(treeNodes.get(0)));
  }

  public void testInherited1()  {
    myFixture.configureByFile(BASE_PATH + "Inherited1.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  public void testInherited2()  {
    myFixture.configureByFiles(BASE_PATH + "Inherited2.as", BASE_PATH + "Inherited2_2.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  public void testInherited3()  {
    myFixture.configureByFiles(BASE_PATH + "Inherited3.as",
                     BASE_PATH + "Inherited3_1.as",
                     BASE_PATH + "Inherited3_2.as",
                     BASE_PATH + "Inherited3_3.as",
                     BASE_PATH + "Inherited3_4.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  public void testWithNameAttribute()  {
    myFixture.configureByFile(BASE_PATH + getTestName(false) + ".mxml");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  public void testWithIncludedFiles1()  {
    myFixture.configureByFiles(BASE_PATH + getTestName(false) + ".mxml",
                     BASE_PATH + getTestName(false) + "_1.css",
                     BASE_PATH + getTestName(false) + "_2.as",
                     BASE_PATH + getTestName(false) + "_3.as",
                     BASE_PATH + getTestName(false) + "_4.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  public void testSuperClasses()  {
    String s = BASE_PATH + getTestName(false);
    myFixture.configureByFiles(s + ".as", s + "_2.as", s + "_3.as", s + "_4.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testLocalVarDerived3()  {
    trivialOneFileTest("js2");
  }

  public void testNsDeclShouldBeOneTime()  {
    trivialOneFileTest("js2");
  }

  public void testExtraTopMembers()  {
    trivialOneFileTest("js2");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testObjectFields()  {
    String s = BASE_PATH + getTestName(false);
    myFixture.configureByFiles(s + ".js2");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor(), false);
  }
}
