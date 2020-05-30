// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.ide.DartNamedElementNode;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartGenericSpecialization;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

abstract public class BaseCreateMethodsFix<T extends DartComponent> {
  protected static final String DART_TEMPLATE_GROUP = "Dart";
  private final Set<T> elementsToProcess = new LinkedHashSet<>();
  @NotNull protected final DartClass myDartClass;
  protected final DartGenericSpecialization specializations;
  protected PsiElement anchor = null;

  public BaseCreateMethodsFix(@NotNull final DartClass dartClass) {
    myDartClass = dartClass;
    specializations = DartClassResolveResult.create(dartClass).getSpecialization();
  }

  protected void evalAnchor(@Nullable Editor editor, PsiElement context) {
    if (editor == null) return;
    final int caretOffset = editor.getCaretModel().getOffset();
    final PsiElement body = DartResolveUtil.getBody(myDartClass);
    assert body != null;
    for (PsiElement child : body.getChildren()) {
      if (child.getTextRange().getStartOffset() > caretOffset) break;
      anchor = child;
    }
    PsiElement next = anchor == null ? null : anchor.getNextSibling();
    while (next != null && (next instanceof PsiWhiteSpace || ";".equals(next.getText()))) {
      anchor = next;
      next = anchor.getNextSibling();
    }
    if (anchor == null) {
      anchor = body;
    }
  }

  public void setCaretSafe(Editor editor, int offset) {
    final PsiElement body = DartResolveUtil.getBody(myDartClass);
    if (body == null) {
      editor.getCaretModel().moveToOffset(offset);
    }
    else {
      final TextRange bodyRange = body.getTextRange();
      editor.getCaretModel().moveToOffset(bodyRange.containsOffset(offset) ? offset : bodyRange.getEndOffset());
    }
  }

  /**
   * must be called not in write action
   */
  public void beforeInvoke(@NotNull final Project project, final Editor editor, final PsiElement file) {
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiElement context) throws IncorrectOperationException {
    if (!FileModificationService.getInstance().prepareFileForWrite(context.getContainingFile())) return;
    evalAnchor(editor, context);
    processElements(project, editor, getElementsToProcess());
  }

  protected void processElements(@NotNull final Project project, @NotNull final Editor editor, @NotNull final Set<T> elementsToProcess) {
    if (elementsToProcess.isEmpty()) {
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        HintManager.getInstance().showErrorHint(editor, getNothingFoundMessage());
      }
      return;
    }
    final TemplateManager templateManager = TemplateManager.getInstance(project);
    for (T e : elementsToProcess) {
      anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, e), anchor);
    }
  }

  @NotNull
  protected abstract String getNothingFoundMessage();

  @Nullable
  protected abstract Template buildFunctionsText(TemplateManager templateManager, T e);

  public PsiElement doAddMethodsForOne(@NotNull final Editor editor,
                                       @NotNull final TemplateManager templateManager,
                                       @Nullable final Template functionTemplate,
                                       @NotNull final PsiElement anchor) throws IncorrectOperationException {
    if (functionTemplate != null) {
      setCaretSafe(editor, anchor.getTextRange().getEndOffset());
      templateManager.startTemplate(editor, functionTemplate);
      final PsiElement dartComponent =
        PsiTreeUtil.getParentOfType(anchor.findElementAt(editor.getCaretModel().getOffset()), DartComponent.class);
      return dartComponent != null ? dartComponent : anchor;
    }
    return anchor;
  }

  public void addElementToProcess(final T function) {
    elementsToProcess.add(function);
  }

  public void addElementsToProcessFrom(@Nullable final Collection<DartNamedElementNode> selectedElements) {
    if (selectedElements == null) {
      return;
    }
    for (DartNamedElementNode el : selectedElements) {
      //noinspection unchecked
      addElementToProcess((T)el.getPsiElement());
    }
  }

  @NotNull
  public Set<T> getElementsToProcess() {
    //noinspection unchecked,SuspiciousToArrayCall
    final T[] objects = (T[])elementsToProcess.toArray(new DartComponent[0]);
    final Comparator<T> tComparator = Comparator.comparingInt(o -> o.getTextRange().getStartOffset());

    final int size = elementsToProcess.size();
    final LinkedHashSet<T> result = new LinkedHashSet<>(size);
    final List<T> objectsFromSameFile = new ArrayList<>();
    PsiFile containingFile = null;

    for (int i = 0; i < size; ++i) {
      final T object = objects[i];
      final PsiFile currentContainingFile = object.getContainingFile();

      if (currentContainingFile != containingFile) {
        if (containingFile != null) {
          objectsFromSameFile.sort(tComparator);
          result.addAll(objectsFromSameFile);
          objectsFromSameFile.clear();
        }
        containingFile = currentContainingFile;
      }

      objectsFromSameFile.add(object);
    }

    objectsFromSameFile.sort(tComparator);
    result.addAll(objectsFromSameFile);

    elementsToProcess.clear();
    elementsToProcess.addAll(result);
    return elementsToProcess;
  }
}
