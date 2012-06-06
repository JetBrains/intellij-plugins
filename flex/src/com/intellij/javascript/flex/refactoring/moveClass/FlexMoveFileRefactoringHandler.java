package com.intellij.javascript.flex.refactoring.moveClass;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Maxim.Mossienko
 */
public class FlexMoveFileRefactoringHandler extends MoveHandlerDelegate {
  @Override
  public boolean tryToMove(PsiElement element, Project project, DataContext dataContext, @Nullable PsiReference reference, Editor editor) {
    PsiElement adjusted = adjustForMove(element);
    if (adjusted == null) return false;

    if (!JSRefactoringUtil.checkReadOnlyStatus(adjusted, editor, RefactoringBundle.message("move.title"))) return true;
    doMove(project, new PsiElement[]{element}, null, null);
    return true;
  }

  @Override
  public void doMove(Project project, PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable MoveCallback callback) {
    if (elements.length == 0) {
      return;
    }

    Collection<JSQualifiedNamedElement> adjusted = new ArrayList<JSQualifiedNamedElement>(elements.length);
    for (PsiElement element : elements) {
      final JSQualifiedNamedElement e = adjustForMove(element);
      if (!JSRefactoringUtil.checkReadOnlyStatus(e, null, RefactoringBundle.message("move.title"))) return;
      adjusted.add(e);
    }

    new FlexMoveClassDialog(project, adjusted, targetContainer, callback).show();
  }

  @Override
  public boolean isValidTarget(PsiElement psiElement, PsiElement[] sources) {
    return psiElement instanceof PsiDirectoryContainer; // follow Java that handles DND to 'folder' node as plain file moving
  }

  @Override
  public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer) {
    if (elements.length == 0) {
      return false;
    }

    final JSQualifiedNamedElement first = adjustForMove(elements[0]);
    if (first == null) {
      return false;
    }

    if (JSResolveUtil.isFileLocalSymbol(first)) {
      if (elements.length > 1) {
        return false;
      }
      else {
        return super.canMove(elements, targetContainer);
      }
    }

    for (PsiElement element : elements) {
      if (adjustForMove(element) == null) return false;
    }
    return super.canMove(elements, targetContainer);
  }

  @Nullable
  public static JSQualifiedNamedElement adjustForMove(PsiElement element) {
    element = JSResolveUtil.unwrapProxy(element);
    PsiFile file = element.getContainingFile();
    if (file == null || !file.getLanguage().is(JavaScriptSupportLoader.ECMA_SCRIPT_L4) && !JavaScriptSupportLoader.isFlexMxmFile(file)) {
      return null;
    }

    if (element instanceof JSPackageStatement) {
      return null;
    }
    if (element instanceof XmlBackedJSClassImpl) {
      return (JSQualifiedNamedElement)element;
    }
    if (element instanceof JSQualifiedNamedElement) {
      final PsiElement parent = JSResolveUtil.findParent(element);
      if (parent instanceof JSPackageStatement || (parent instanceof JSFile && JSResolveUtil.getXmlBackedClass((JSFile)parent) == null)) {
        return (JSQualifiedNamedElement)element;
      }
    }
    if (element instanceof JSVarStatement && ((JSVarStatement)element).getVariables().length == 1) {
      final PsiElement parent = JSResolveUtil.findParent(element);
      if (parent instanceof JSPackageStatement || (parent instanceof JSFile && JSResolveUtil.getXmlBackedClass((JSFile)parent) == null)) {
        return ((JSVarStatement)element).getVariables()[0];
      }
    }
    if (element instanceof JSFile) {
      final XmlBackedJSClassImpl xmlBackedClass = JSResolveUtil.getXmlBackedClass((JSFile)element);
      if (xmlBackedClass != null) {
        return xmlBackedClass;
      }
      return JSPsiImplUtils.findQualifiedElement((JSFile)element);
    }
    if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
    }
    return null;
  }
}
