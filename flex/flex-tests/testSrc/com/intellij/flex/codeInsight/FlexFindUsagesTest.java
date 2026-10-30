// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.todo.TodoConfiguration;
import com.intellij.ide.todo.nodes.TodoFileNode;
import com.intellij.lang.javascript.JSFindUsagesFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.IndexPattern;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoAttributesUtil;
import com.intellij.psi.search.TodoPattern;
import com.intellij.psi.search.searches.IndexPatternSearch;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.containers.ContainerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSFindUsagesFixture.jsFindUsagesFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestApplication
public class FlexFindUsagesTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSFindUsagesFixture> js =
    jsFindUsagesFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("") + "/as_findUsages/", "js")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getFixture().getTestRootDisposable(), "");
  }

  @Test
  public void testFindConstructorUsages() {
    PsiReference[] references = js.get().findElementAtCaret("ConstructorUsages.js2");
    assertEquals(1, references.length, "Constructor references");
  }

  @Test
  public void testFindClassUsages() {
    PsiReference[] references = js.get().findElementAtCaret("ClassUsages.js2");
    assertEquals(2, references.length, "Class references");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindMxmlComponentsFileReferences() {
    PsiReference[] references = js.get().findElementAtCaret("18.mxml", "C18_2.mxml", "18_3.mxml", "18_4.as");
    assertEquals(5, references.length, "Mxml component files references");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindMxmlComponentsFileReferences_2() {
    PsiReference[] references;

    references = js.get().findElementAtCaret("18.as", "C18_2.mxml", "18_3.mxml", "18_4.as");
    assertEquals(5, references.length, "Mxml component files references");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindMxmlComponentsFileReferences2() {
    PsiReference[] references = js.get().findElementAtCaret("19.mxml", "19_2.as", "19_3.mxml");
    assertEquals(3, references.length, "Mxml component files references");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindParameterWith$InMxmlComponent() {
    PsiReference[] references = js.get().findElementAtCaret("20.mxml");
    assertEquals(2, references.length, "Parameter with $ in mxml component");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindImplementationMethodWhenSearchingFromInterface() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(2, references.length, "Find implementation method when searching for");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindPrivateVarInMxml() {
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), js.get().getFixture().getTestRootDisposable());
    PsiReference[] references = js.get().findElementAtCaret("21.mxml");
    assertEquals(2, references.length, "Mxml component private variable");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindSetterUsagesInMxml() {
    String s = js.get().getTestName(false);
    PsiReference[] references = js.get().findElementAtCaret(s + ".mxml", s + "_2.mxml");
    assertEquals(6, references.length, "Setter usages");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindGetterUsagesInMxml() {
    String s = js.get().getTestName(false);
    PsiReference[] references = js.get().findElementAtCaret(s + ".mxml", s + "_2.mxml");
    assertEquals(3, references.length, "Getter usages");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testFindPackageRefs() {
    String testName = js.get().getTestName(false);
    PsiReference[] references = js.get().findElementAtCaret(testName + ".mxml", testName + "_2.as");
    assertEquals(5, references.length, "Package refs");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMixJsAndJs2() {
    String testName = js.get().getTestName(false);
    PsiReference[] references = js.get().findElementAtCaret(testName + ".js2", testName + ".mxml", testName + ".js");
    assertFalse(ReadAction.computeBlocking(() -> ContainerUtil.exists(references, r -> r.getElement().getContainingFile().getName().endsWith(".js"))));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMixJsAndJs2_2() {
    String testName = js.get().getTestName(false);
    PsiReference[] references = js.get().findElementAtCaret(testName + ".js", testName + ".mxml", testName + ".js2");
    assertEquals(2, references.length);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testNoDynamicUsages() {
    String testName = js.get().getTestName(false);
    PsiReference[] references = js.get().findElementAtCaret(testName + ".js2");
    assertEquals(2, references.length);
  }

  @Test
  public void testSuperCall() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  @Test
  public void testSuperCall2() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  @Test
  public void testBaseMethod() {
    final PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  @Test
  public void testInheritorCall() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  @Test
  public void testInheritorCall2() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  @Test
  public void testReferenceInStringLiteral() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as", js.get().getTestName(false) + "_2.mxml");
    assertEquals(0, references.length);
  }

  @Test
  public void testASGenericsAndDollarsInName() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  @Test
  public void testUnrelatedUnqualifiedDefinition() {
    PsiReference[] references = js.get().findElementAtCaret(js.get().getTestName(false) + ".as"); // IDEA-189640
    assertEquals(0, references.length);
  }

  @Test
  public void testFindToDosInMxml() {
    TodoPattern pattern = new TodoPattern("newtodo", TodoAttributesUtil.createDefault(), true);
    TodoPattern pattern2 = new TodoPattern("newt", TodoAttributesUtil.createDefault(), true);
    TodoConfiguration todoConfiguration = TodoConfiguration.getInstance();
    TodoPattern[] oldPatterns = todoConfiguration.getTodoPatterns();
    todoConfiguration.setTodoPatterns(new TodoPattern[]{pattern, pattern2});
    EdtTestUtil.runInEdtAndWait(() -> PlatformTestUtil.dispatchAllEventsInIdeEventQueue());

    try {
      PsiFile file = js.get().getFixture().configureByFile("ToDos.mxml");
      PsiTodoSearchHelper searchHelper = PsiTodoSearchHelper.getInstance(js.get().getProject());
      assertEquals(4, ReadAction.computeBlocking(() -> searchHelper.getTodoItemsCount(file)));

      IndexPattern[] patterns = todoConfiguration.getIndexPatterns();
      // prefer finding last todo patterns when both of them match
      assertEquals(0, ReadAction.computeBlocking(() -> IndexPatternSearch.search(file, patterns[0]).findAll().size()).intValue());
      // mxml has todos injected
      assertEquals(1, ReadAction.computeBlocking(() -> IndexPatternSearch.search(file, patterns[1]).findAll().size()).intValue());
      assertEquals(4, ReadAction.computeBlocking(() -> TodoFileNode.findAllTodos(file, searchHelper).size()).intValue());
    }
    finally {
      todoConfiguration.setTodoPatterns(oldPatterns);
    }
  }
}
