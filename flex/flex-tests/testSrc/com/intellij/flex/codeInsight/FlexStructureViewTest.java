// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.JSAbstractStructureViewTest;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.StructureViewTestUtil;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.structureView.JSStructureViewModel;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.icons.RowIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.intellij.flex.util.FlexTestUtils.getPathToMockFlex;
import static com.intellij.lang.javascript.StructureViewTestUtil.getIcon;

public class FlexStructureViewTest extends JSAbstractStructureViewTest {
  @Override
  protected boolean isIconRequired() {
    return true;
  }

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
      @NotNull
      @Override
      public String getModuleTypeId() {
        return FlexModuleType.MODULE_TYPE_ID;
      }

      @NotNull
      @Override
      public Sdk getSdk() {
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
    final Object[] items = getTopLevelItems();
    assertEquals(3, items.length);

    assertEquals("ZZZ", getText(items[1]));
    assertEquals("ZZZ2", getText(items[2]));
    assertEquals("A15", getText(items[0]));

    List<AbstractTreeNode<?>> treeNodes = getChildren(items[1]);
    assertEquals(4 + OBJECT_METHODS_COUNT, treeNodes.size());

    assertEquals("EE", getText(treeNodes.get(0)));
    assertEquals("aaa:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 1)));
    assertEquals("c2:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 2)));
    assertEquals("ttt:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 3)));

    treeNodes = getChildren(items[0]);
    assertEquals(7 + OBJECT_METHODS_COUNT, treeNodes.size());

    assertEquals("automation:Object", getText(treeNodes.get(0)));
    assertEquals("initialized:Boolean", getText(treeNodes.get(3)));
    assertEquals("register(c:Object):void", getText(treeNodes.get(8)));
    assertEquals("_automation:Object", getText(treeNodes.get(OBJECT_METHODS_COUNT + 3)));
    assertEquals("aaa:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 4)));
    assertEquals("c2:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 5)));
    assertEquals("delegate:Object", getText(treeNodes.get(OBJECT_METHODS_COUNT + 6)));
  }

  public void testGroupByClass2()  {
    myFixture.configureByFile(BASE_PATH + "15.js2");
    Object[] items = getTopLevelItems();
    assertEquals(3, items.length);

    assertEquals("ZZZ", getText(items[1]));
    assertEquals("ZZZ2", getText(items[2]));
    assertEquals("A15", getText(items[0]));

    List<AbstractTreeNode<?>> treeNodes = getChildren(items[0]);
    assertEquals(6, treeNodes.size());
    Iterator<? extends AbstractTreeNode<?>> iterator = treeNodes.iterator();

    AbstractTreeNode node = iterator.next();
    assertEquals("mypackage.ZZZ", getText(node));

    Collection<? extends AbstractTreeNode<?>> treeNodes2 = getChildren(node);
    assertEquals(2, treeNodes2.size());
    Iterator<? extends AbstractTreeNode<?>> iterator2 = treeNodes2.iterator();

    assertEquals("aaa:*", getText(iterator2.next()));
    assertEquals("c2:*", getText(iterator2.next()));

    assertEquals("automation:Object", getText(iterator.next()));
    assertEquals("initialized:Boolean", getText(iterator.next()));
    assertEquals("register(c:Object):void", getText(iterator.next()));
    assertEquals("_automation:Object", getText(iterator.next()));
    assertEquals("delegate:Object", getText(iterator.next()));

    PsiElement element = checkIfCurrentEditorElementIsValid((AbstractTreeNode)items[1], JSClass.class);
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
    Object[] items = getTopLevelItems();
    assertEquals(2, items.length);
    assertEquals("XXX", getText(items[0]));
    assertEquals("YYY", getText(items[1]));

    List<AbstractTreeNode<?>> treeNodes = getChildren(items[0]);
    assertEquals(3 + OBJECT_METHODS_COUNT, treeNodes.size());
    assertEquals("constructor:*", getText(treeNodes.get(0)));
    assertEquals("aaa:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 1)));
    assertEquals("bbb:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 2)));
    AbstractTreeNode node = treeNodes.get(OBJECT_METHODS_COUNT - 5);
    assertEquals("staticFun():*", getText(node));

    Icon icon = getIcon(node);
    assertTrue(icon instanceof RowIcon);
    // static mark blended in
    assertTrue(((RowIcon)icon).getIcon(0) instanceof LayeredIcon);

    treeNodes = getChildren(items[1]);
    assertEquals(2 + OBJECT_METHODS_COUNT, treeNodes.size());
    assertEquals("aaa:*", getText(treeNodes.get(OBJECT_METHODS_COUNT)));
    assertEquals("bbb:*", getText(treeNodes.get(OBJECT_METHODS_COUNT + 1)));
  }

  public void testPrivateIconsForClassMembers()  {
    myFixture.configureByFile(BASE_PATH + "15.js2");
    Object[] items = getTopLevelItems();
    assertEquals(3, items.length);
    List<AbstractTreeNode<?>> treeNodes = getChildren(items[0]);
    assertEquals(JSAttributeList.AccessType.PUBLIC, getAccessType(treeNodes.get(1), false)); // automation():Object

    treeNodes = getChildren(items[1]);
    assertTrue(treeNodes.size() + ">" + (3 + OBJECT_METHODS_COUNT), treeNodes.size() > 3 + OBJECT_METHODS_COUNT);
    assertEquals(JSAttributeList.AccessType.PRIVATE, getAccessType(treeNodes.get(3 + OBJECT_METHODS_COUNT))); // ttt

    assertEquals(JSAttributeList.AccessType.PRIVATE, getAccessType(treeNodes.get(0))); // EE
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

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testWithIncludedFiles1()  {
    myFixture.configureByFiles(BASE_PATH + getTestName(false) + ".mxml",
                     BASE_PATH + getTestName(false) + "_1.css",
                     BASE_PATH + getTestName(false) + "_2.as",
                     BASE_PATH + getTestName(false) + "_3.as",
                     BASE_PATH + getTestName(false) + "_4.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testSuperClasses()  {
    String s = BASE_PATH + getTestName(false);
    myFixture.configureByFiles(s + ".as", s + "_2.as", s + "_3.as", s + "_4.as");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor());
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testLocalVarDerived3()  {
    trivialOneFileTest("js2");
  }

  public void testNsDeclShouldBeOneTime()  {
    trivialOneFileTest("js2");
  }

  public void testExtraTopMembers()  {
    trivialOneFileTest("js2");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testObjectFields()  {
    String s = BASE_PATH + getTestName(false);
    myFixture.configureByFiles(s + ".js2");
    StructureViewTestUtil.checkStructureView(myFixture.getEditor(), false);
  }
}
