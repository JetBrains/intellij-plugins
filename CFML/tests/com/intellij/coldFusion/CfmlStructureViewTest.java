/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion;

import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.files.CfmlFileViewProvider;
import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.smartTree.Group;
import com.intellij.ide.util.treeView.smartTree.SmartTreeStructure;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.LanguageStructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;

/**
 * @author vnikolaenko
 */
public class CfmlStructureViewTest extends CfmlCodeInsightFixtureTestCase {
  public void testScriptAndTagFunctions() {
    myFixture.configureByFile(getTestName(true) + ".test.cfc");
    final Object[] topLevelObjects = getTopLevelItems();
    assertEquals(topLevelObjects.length, 2);
    assertEquals("someFunction1(arg1, [arg2])", getText(topLevelObjects[0]));
    assertEquals("someFunction2()", getText(topLevelObjects[1]));
  }

  public void testScriptFunctions() {
    myFixture.configureByFile(getTestName(true) + ".test.cfml");
    final Object[] topLevelObjects = getTopLevelItems();
    assertEquals(topLevelObjects.length, 3);
    assertEquals("someFunction1(arg1, [arg2])", getText(topLevelObjects[0]));
    assertEquals("someFunction2()", getText(topLevelObjects[1]));
    assertEquals("someFunction3()", getText(topLevelObjects[2]));
  }

  public void testTagFunctions() {
    myFixture.configureByFile(getTestName(true) + ".test.cfml");
    final Object[] topLevelObjects = getTopLevelItems();
    assertEquals(topLevelObjects.length, 2);
    assertEquals("someFunction1(arg1, [arg2])", getText(topLevelObjects[0]));
    assertEquals("someFunction2()", getText(topLevelObjects[1]));
  }

  public void testMixedFunctionsTypes() {
    myFixture.configureByFile(getTestName(true) + ".test.cfml");
    final Object[] topLevelObjects = getTopLevelItems();
    assertEquals(topLevelObjects.length, 2);
    assertEquals("someFunction1(arg1 : int, [arg2 : array]) : string", getText(topLevelObjects[0]));
    assertEquals("someFunction2(arg1, arg2)", getText(topLevelObjects[1]));
  }

  public void testNewFunctionSyntax() {
    myFixture.configureByFile(getTestName(true) + ".test.cfml");
    final Object[] topLevelObjects = getTopLevelItems();
    assertEquals(topLevelObjects.length, 1);
    assertEquals("someFunction(arg1 : string, [arg2 : int]) : void", getText(topLevelObjects[0]));
  }

  public void testDeeperFunctionLocation() {
    myFixture.configureByFile(getTestName(true) + ".test.cfml");
    final Object[] topLevelObjects = getTopLevelItems();
    assertEquals(topLevelObjects.length, 3);
    assertEquals("f1() : void", getText(topLevelObjects[0]));
    assertEquals("f2() : void", getText(topLevelObjects[1]));
    assertEquals("f3() : void", getText(topLevelObjects[2]));
  }

  private Object[] getTopLevelItems() {
    StructureView structureView = createStructureViewModel();
    final StructureViewModel structureViewModel = structureView.getTreeModel();

    AbstractTreeStructure jsTreeStructure = new SmartTreeStructure(getProject(), structureViewModel);

    Object[] items = jsTreeStructure.getChildElements(jsTreeStructure.getRootElement());
    Disposer.dispose(structureViewModel);
    Disposer.dispose(structureView);
    return items;
  }

  private StructureView createStructureViewModel() {
    VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

    final FileType fileType = virtualFile.getFileType();
    final StructureViewBuilder structureViewBuilder;

    if (fileType == CfmlFileType.INSTANCE) {
      CfmlFileViewProvider viewProvider = (CfmlFileViewProvider)myFixture.getFile().getViewProvider();
      structureViewBuilder = LanguageStructureViewBuilder.INSTANCE.forLanguage(viewProvider.getBaseLanguage())
        .getStructureViewBuilder(viewProvider.getPsi(viewProvider.getBaseLanguage()));
    } else {
      structureViewBuilder = StructureViewBuilder.PROVIDER.getStructureViewBuilder(fileType, virtualFile, getProject());
    }

    return structureViewBuilder.createStructureView(FileEditorManager.getInstance(getProject()).getSelectedEditor(virtualFile), getProject());
  }

  private String getText(final Object item) {
    final Object value = ((AbstractTreeNode<?>)item).getValue();
    if (value instanceof TreeElement) return ((TreeElement)value).getPresentation().getPresentableText();
    if (value instanceof Group) return ((Group)value).getPresentation().getPresentableText();
    Assert.fail("Unexpected tree node type: " + item);
    return null;
  }

  @Override
  protected String getBasePath() {
    return "/structureView/";
  }
}
