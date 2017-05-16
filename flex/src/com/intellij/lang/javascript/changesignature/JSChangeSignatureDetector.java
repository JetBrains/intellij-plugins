package com.intellij.lang.javascript.changesignature;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.refactoring.changeSignature.JSChangeSignatureDialog;
import com.intellij.lang.javascript.refactoring.changeSignature.JSMethodDescriptor;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.changeSignature.inplace.LanguageChangeSignatureDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class JSChangeSignatureDetector implements LanguageChangeSignatureDetector<JSChangeInfo> {

  @NotNull
  @Override
  public JSChangeInfo createInitialChangeInfo(@NotNull PsiElement element) {
    JSFunction method = PsiTreeUtil.getParentOfType(element, JSFunction.class, false);
    if (method == null || !isInsideMethodSignature(element, method)) {
      return null;
    }
    if (PsiTreeUtil.hasErrorElements(method.getParameterList())) {
      return null;
    }
    return new JSChangeInfo(method);
  }

  private static boolean isInsideMethodSignature(PsiElement element, @NotNull JSFunction method) {
    TextRange r = getRange(method);
    if (r != null && r.contains(element.getTextOffset())) {
      return true;
    }
    if (element instanceof PsiErrorElement && element.getPrevSibling().getNode().getElementType() == JSTokenTypes.COLON) {
      // function foo():<caret> {}
      return true;
    }
    if (element instanceof PsiWhiteSpace &&
        element.getPrevSibling() instanceof PsiErrorElement &&
        element.getPrevSibling().getPrevSibling().getNode().getElementType() == JSTokenTypes.COLON) {
      // function foo():<caret> {}
      return true;
    }
    return false;
  }

  @Override
  public void performChange(JSChangeInfo changeInfo, Editor editor, @NotNull final String oldText) {
    final JSChangeInfo jsChangeInfo = changeInfo;
    JSMethodDescriptor descriptor = new JSMethodDescriptor(jsChangeInfo.getMethod(), false) {
      @Override
      public String getName() {
        return jsChangeInfo.getNewName();
      }

      @Override
      public List<JSParameterInfo> getParameters() {
        return Arrays.asList(jsChangeInfo.getNewParameters());
      }

      @Override
      public int getParametersCount() {
        return getParameters().size();
      }

      @Override
      public String getVisibility() {
        return jsChangeInfo.getNewVisibility().name();
      }

      @Override
      public JSFunction getMethod() {
        return jsChangeInfo.getMethod();
      }
    };
    JSChangeSignatureDialog d = new JSChangeSignatureDialog(descriptor, changeInfo.getMethod()) {
      @Override
      protected void invokeRefactoring(BaseRefactoringProcessor processor) {
        revertChanges(jsChangeInfo.getMethod(), oldText);
        super.invokeRefactoring(processor);
      }
    };
    d.showAndGet();
  }

  // TODO generalize
  private static void revertChanges(final PsiElement method, final String oldText) {
    //UndoManager.getInstance(method.getProject()).undoableActionPerformed(new );
    ApplicationManager.getApplication().runWriteAction(() -> {
      final PsiFile file = method.getContainingFile();
      final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(method.getProject());
      final Document document = documentManager.getDocument(file);
      if (document != null) {
        final TextRange textRange = method.getTextRange();
        document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), oldText);
        documentManager.commitDocument(document);
      }
    });
  }

  @Override
  public boolean isChangeSignatureAvailableOnElement(PsiElement element, JSChangeInfo currentInfo) {
    return element.getNode().getElementType() == JSTokenTypes.IDENTIFIER &&
           Comparing.equal(currentInfo.getMethod(), element.getParent().getParent());
  }



  @Nullable
  private static TextRange getRange(PsiElement element) {
    JSFunction f = PsiTreeUtil.getParentOfType(element, JSFunction.class, false);
    if (f == null) return null;
    ASTNode identifier = f.findNameIdentifier();
    if (identifier == null) {
      return null;
    }
    PsiElement e = f.getReturnTypeElement();
    if (e == null) {
      ASTNode colon = f.getNode().findChildByType(JSTokenTypes.COLON);
      if (colon != null) {
        e = colon.getPsi();
      }
    }
    if (e == null) {
      e = f.getParameterList();
    }
    return new TextRange(identifier.getTextRange().getStartOffset(), e.getTextRange().getEndOffset());
  }

  @Override
  public boolean ignoreChanges(PsiElement element) {
    return false;
  }

  @Override
  public TextRange getHighlightingRange(@NotNull JSChangeInfo changeInfo) {
    return getRange(changeInfo.getMethod());
  }

  @Override
  public FileType getFileType() {
    return JavaScriptFileType.INSTANCE;
  }

  @Override
  public JSChangeInfo createNextChangeInfo(String signature, @NotNull JSChangeInfo currentInfo, boolean delegate) {
    return null;
  }
}
