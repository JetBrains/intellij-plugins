package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.ide.DartNamedElementNode;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartClassDefinition;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author: Fedor.Korotkov
 */
abstract public class BaseCreateMethodsFix<T extends DartComponent> {
  private final Set<T> elementsToProcess = new LinkedHashSet<T>();
  protected final DartClass myDartClass;
  protected final DartGenericSpecialization specializations;
  protected PsiElement anchor = null;

  public BaseCreateMethodsFix(final DartClass dartClass) {
    myDartClass = dartClass;
    specializations = DartClassResolveResult.create(dartClass).getSpecialization();
  }

  protected void evalAnchor(@Nullable Editor editor, PsiElement context) {
    if (editor == null) return;
    final int caretOffset = editor.getCaretModel().getOffset();
    if (myDartClass instanceof DartClassDefinition) {
      final PsiElement body = DartResolveUtil.getBody(myDartClass);
      assert body != null;
      for (PsiElement child : body.getChildren()) {
        if (child.getTextOffset() > caretOffset) break;
        anchor = child;
      }
    }
    else {
      anchor = context.findElementAt(caretOffset);
    }
    PsiElement next = anchor == null ? null : anchor.getNextSibling();
    while (next != null && (UsefulPsiTreeUtil.isWhitespaceOrComment(next) || ";".equals(next.getText()))) {
      anchor = next;
      next = anchor.getNextSibling();
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
    processElements(project, getElementsToProcess());
  }

  protected void processElements(Project project, Set<T> elementsToProcess) {
    for (T e : elementsToProcess) {
      anchor = doAddMethodsForOne(project, buildFunctionsText(e), anchor);
    }
  }

  protected abstract String buildFunctionsText(T e);

  public PsiElement doAddMethodsForOne(final Project project, final String functionsText, PsiElement anchor)
    throws IncorrectOperationException {
    if (functionsText != null && functionsText.length() > 0) {
      // todo: change to TemplateManager. See CreateDartFunctionActionBase for example.
      List<DartComponent> elements = DartElementGenerator.createFunctionsFromText(project, functionsText);
      final PsiElement insert = myDartClass instanceof DartClassDefinition ?
                                DartResolveUtil.getBody(myDartClass) : myDartClass;
      assert insert != null;
      for (DartComponent element : elements) {
        anchor = insert.addAfter(element, anchor);
        anchor = afterAddHandler(element, anchor);
      }
    }
    return anchor;
  }

  protected PsiElement afterAddHandler(DartComponent element, PsiElement anchor) {
    final PsiElement newLineNode =
      PsiParserFacade.SERVICE.getInstance(element.getProject()).createWhiteSpaceFromText("\n\n");
    anchor.getParent().addBefore(newLineNode, anchor);
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
      addElementToProcess((T)el.getPsiElement());
    }
  }

  public Set<T> getElementsToProcess() {
    final T[] objects = (T[])elementsToProcess.toArray(new DartComponent[elementsToProcess.size()]);
    final Comparator<T> tComparator = new Comparator<T>() {
      public int compare(final T o1, final T o2) {
        return o1.getTextOffset() - o2.getTextOffset();
      }
    };

    final int size = elementsToProcess.size();
    final LinkedHashSet<T> result = new LinkedHashSet<T>(size);
    final List<T> objectsFromSameFile = new ArrayList<T>();
    PsiFile containingFile = null;

    for (int i = 0; i < size; ++i) {
      final T object = objects[i];
      final PsiFile currentContainingFile = object.getContainingFile();

      if (currentContainingFile != containingFile) {
        if (containingFile != null) {
          Collections.sort(objectsFromSameFile, tComparator);
          result.addAll(objectsFromSameFile);
          objectsFromSameFile.clear();
        }
        containingFile = currentContainingFile;
      }

      objectsFromSameFile.add(object);
    }

    Collections.sort(objectsFromSameFile, tComparator);
    result.addAll(objectsFromSameFile);

    elementsToProcess.clear();
    elementsToProcess.addAll(result);
    return elementsToProcess;
  }
}
