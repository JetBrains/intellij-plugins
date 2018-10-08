// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.base;

import com.intellij.codeInsight.EditorInfo;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSFunctionItem;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.refactoring.changeSignature.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.util.*;


public abstract class FlexChangeSignatureTestBase extends MultiFileTestCase {

  protected Runnable myAfterCommitRunnable = null;
  protected boolean myIgnoreConflicts;

  public static final String PROPAGATE_MARKER = "propagate";

  @Override
  protected String getTestDataPath() {
    return JSTestUtils.getTestDataPath();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myDoCompare = true;
    myAfterCommitRunnable = null;
    myIgnoreConflicts = false;
  }

  protected abstract String[] getActiveFileNames();


  @Override
  protected void prepareProject(VirtualFile rootDir) {
    super.prepareProject(rootDir);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  protected void doTestForFile(String mainFileName,
                               final String newName,
                               final JSAttributeList.AccessType visibility,
                               @SuppressWarnings("SameParameterValue") final String returnType,
                               @NotNull Function<JSParameterInfo[], JSParameterInfo[]> updateParameters) {
    doTestForFile(mainFileName, (rootDir, rootAfter) -> this.performRefactoring(newName, visibility, returnType, updateParameters));
  }

  protected void doTest(final String newName,
                        final JSAttributeList.AccessType visibility,
                        final String returnType,
                        @Nullable final JSParameterInfo... params) {
    doDefaultTest((rootDir, rootAfter) -> this.performRefactoring(newName, visibility, returnType, params));
  }

  protected void doTestConflicts(final String newName, 
                                 final JSAttributeList.AccessType visibility,
                                 final String returnType,
                                 final String[] conflicts,
                                 final JSParameterInfo... params) {
    assertConflicts(() -> doTest(newName, visibility, returnType, params), conflicts);
  }

  protected void doDefaultTest(PerformAction action) {
    doTest((rootDir, rootAfter) -> {
      configureFileAndEditor(findVirtualFileByAnyName(getActiveFileNames()));
      action.performAction(rootDir, rootAfter);
    }, false);
  }

  protected void doTestForFile(String mainFileName, PerformAction action) {
    doTest((rootDir, rootAfter) -> {
      configureFileAndEditor(findVirtualFileByAnyName(mainFileName));
      action.performAction(rootDir, rootAfter);
    }, false);
  }

  protected void performRefactoring(String newName,
                                    JSAttributeList.AccessType visibility,
                                    String returnType,
                                    @Nullable JSParameterInfo... params) {
    this.performRefactoring(newName, visibility, returnType, 
                            originalParams -> params == null ? originalParams : params, Collections.emptySet());
  }

  protected void performRefactoring(String newName,
                                    JSAttributeList.AccessType visibility,
                                    String returnType,
                                    @NotNull Function<JSParameterInfo[], JSParameterInfo[]> updateParameters) {
    this.performRefactoring(newName, visibility, returnType, updateParameters, Collections.emptySet());
  }

  protected void performRefactoring(String newName,
                                    JSAttributeList.AccessType visibility,
                                    String returnType,
                                    @NotNull Function<JSParameterInfo[], JSParameterInfo[]> updateParameters,
                                    Set<JSElement> importedElements) {

    Set<JSFunction> methodsToPropagateParams = extractMethodsToPropagateFromMarkers(myFile, myEditor);
    PsiElement element = getTargetElement();
    List<JSParameterInfo> originalParameters = new JSMethodDescriptor((JSFunction)element, false).getParameters();
    JSParameterInfo[] params = updateParameters.fun(ContainerUtil.toArray(originalParameters, JSParameterInfo[]::new));

    MyProcessor p = new MyProcessor((JSFunction)element, visibility, newName, returnType, params, methodsToPropagateParams, importedElements);
    p.run();
    ApplicationManager.getApplication().runWriteAction(() -> myProject.getComponent(PostprocessReformattingAspect.class).doPostponedFormatting());
    FileDocumentManager.getInstance().saveAllDocuments();
  }

  protected void assertConflicts(Runnable runnable, String[] conflicts) {
    myDoCompare = false;
    try {
      runnable.run();
      assertEquals("Conflicts expected:\n" + StringUtil.join(conflicts, "\n"), 0, conflicts.length);
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertNotNull("Conflicts not expected but found:" + e.getMessage(), conflicts);
      assertSameElements(e.getMessages(), conflicts);
    }
  }

  @SuppressWarnings("SameParameterValue")
  protected void assertDeclarationsAndUsagesCounts(int declarationsCount, int otherUsagesCount) {
    PsiElement element = getTargetElement();
    MyProcessor p = new MyProcessor((JSFunction)element, JSAttributeList.AccessType.PUBLIC, "renamed", "", JSParameterInfo.EMPTY_ARRAY,
                                    Collections.emptySet(), Collections.emptySet());
    p.assertUsages(declarationsCount, otherUsagesCount);
  }

  protected void assertPropagationCandidates(@NotNull String[] firstLevelPropagationCandidates) {
    assertSameElements(calculatePropagationCandidates((JSFunction)getTargetElement()), firstLevelPropagationCandidates);
  }

  @NotNull
  private PsiElement getTargetElement() {
    PsiElement element = new JSChangeSignatureHandler().findTargetMember(myFile, myEditor);
    final JSFunctionItem functionItem = JSPsiImplUtils.calculatePossibleFunction(element);
    if (functionItem != null) element = functionItem;
    assertTrue("No target element found at caret", element instanceof JSFunction);
    assertTrue("Refactoring should be accessible", JSChangeSignatureHandler.canRefactor(element, myEditor));
    return element;
  }

  @NotNull
  private List<String> calculatePropagationCandidates(JSFunction element) {
    JSMethodNode node = new JSMethodNode(element, new HashSet<>(), myProject, EmptyRunnable.INSTANCE);
    List<String> actual = new ArrayList<>(node.getChildCount());
    for (int j = 0; j < node.getChildCount(); j++) {
      JSFunction method = ((JSMethodNode)node.getChildAt(j)).getMember();
      actual.add(method.getName());
    }
    return actual;
  }

  private static Set<JSFunction> extractMethodsToPropagateFromMarkers(PsiFile file, Editor editor) {
    Set<JSFunction> methodsToPropagateParams = new HashSet<>();
    int i = 0;
    while (true) {
      i = file.getText().indexOf(PROPAGATE_MARKER, i);
      if (i == -1) {
        break;
      }

      PsiElement element = file.findElementAt(i);
      if (PsiTreeUtil.getParentOfType(element, PsiComment.class, false) != null) {
        JSFunction toPropagate = PsiTreeUtil.getParentOfType(element, JSFunction.class);
        assertNotNull("Function to propagate not found for marker at line " + editor.getDocument().getLineNumber(i), toPropagate);
        methodsToPropagateParams.add(toPropagate);
      }
      i += PROPAGATE_MARKER.length();
    }
    return methodsToPropagateParams;
  }

  @NotNull
  protected VirtualFile findVirtualFileByAnyName(String... names) {
    final Optional<VirtualFile> file = Arrays.stream(names)
      .map(t -> ModuleRootManager.getInstance(myModule).getContentRoots()[0].findChild(t))
      .filter(c -> c != null)
      .findFirst();
    if (!file.isPresent()) {
      Assert.fail(String.format("Could not find file from any of names [ %s ]",
                                StringUtil.join(names, ", ")));
    }
    return file.get();
  }

  protected void configureFileAndEditor(@NotNull VirtualFile file) {
    myEditor = createEditor(file);
    final EditorInfo editorInfo = new EditorInfo(myEditor.getDocument().getText());
    WriteCommandAction.runWriteCommandAction(null, () -> myEditor.getDocument().setText(editorInfo.getNewFileText()));

    editorInfo.applyToEditor(myEditor);
    PsiDocumentManager.getInstance(myProject).commitDocument(myEditor.getDocument());

    myFile = myPsiManager.findFile(file);

    PsiFile injectedFile = InjectedLanguageUtil.findInjectedPsiNoCommit(myFile, myEditor.getCaretModel().getOffset());
    if (injectedFile != null) {
      myEditor = InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(myEditor, myFile);
      myFile = injectedFile;
    }
  }
  
  class MyProcessor extends JSChangeSignatureProcessor {

    MyProcessor(JSFunction method,
                       JSAttributeList.AccessType visibility,
                       String methodName,
                       String returnType,
                       JSParameterInfo[] parameters,
                       Set<JSFunction> methodsToPropagateParameters,
                       Set<JSElement> importedElements) {
      super(method, visibility, methodName, returnType, parameters, methodsToPropagateParameters, importedElements);
    }

    @Override
    protected boolean showConflicts(@NotNull MultiMap<PsiElement, String> conflicts, UsageInfo[] usages) {
      if (myIgnoreConflicts) {
        prepareSuccessful();
        return true;
      }
      else {
        return super.showConflicts(conflicts, usages);
      }
    }

    public void assertUsages(int declarationsCount, int otherUsagesCount) {
      UsageInfo[] usages = findUsages();
      assertEquals(declarationsCount + otherUsagesCount, usages.length);
      assertEquals(declarationsCount, ContainerUtil.findAll(usages, usageInfo -> !(usageInfo instanceof OtherUsageInfo)).size());
    }
  }
}
