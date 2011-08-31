package com.intellij.flex.uiDesigner;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProblemsHolder {
  private static final Logger LOG = Logger.getInstance(ProblemsHolder.class.getName());

  private final List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();

  private VirtualFile currentFile;
  private boolean handled;

  public boolean isEmpty() {
    return problems.isEmpty();
  }

  private static int getLineNumber(PsiElement element) {
    final int elementTextOffset = InjectedLanguageManager.getInstance(element.getProject()).injectedToHost(element, element.getTextOffset());
    final PsiFile psiFile = InjectedLanguageUtil.getTopLevelFile(element);
    final Document document = PsiDocumentManager.getInstance(element.getProject()).getCachedDocument(psiFile);
    assert document != null;
    return document.getLineNumber(elementTextOffset) + 1;
  }

  public void setCurrentFile(@Nullable VirtualFile currentFile) {
    this.currentFile = currentFile;
  }

  public ProblemDescriptor[] getResultList() {
    LOG.assertTrue(currentFile == null);
    LOG.assertTrue(!handled);
    handled = true;
    return problems.toArray(new ProblemDescriptor[problems.size()]);
  }

  public void clear() {
    problems.clear();
  }

  public void add(InvalidPropertyException e) {
    final ProblemDescriptor problemDescriptor = new ProblemDescriptor(e.getMessage(), currentFile,
                                                                      e.getPsiElement() == null ? -1 : getLineNumber(e.getPsiElement()));
    problems.add(problemDescriptor);
    if (e.getCause() != null) {
      LOG.error(LogMessageUtil.createEvent(e.getMessage(), e.getCause(), problemDescriptor));
    }
  }

  public void add(final PsiElement element, final RuntimeException e, final String propertyName) {
    String error;
    if (e instanceof NumberFormatException) {
      error = e.getMessage();
      final String prefix = "For input string: \"";
      if (error.startsWith(prefix)) {
        error = FlexUIDesignerBundle.message("error.write.property.numeric.value",
          error.substring(prefix.length(), error.charAt(error.length() - 1) == '"' ? error.length() - 1 : error.length()), propertyName);
      }
    }
    else {
      error = FlexUIDesignerBundle.message("error.write.property", propertyName);
    }

    final ProblemDescriptor problemDescriptor = new ProblemDescriptor(error, currentFile, getLineNumber(element));
    LOG.error(LogMessageUtil.createEvent(error, e, problemDescriptor));
    problems.add(problemDescriptor);
  }

  public void add(Throwable e) {
    if (e instanceof InvalidPropertyException) {
      add(((InvalidPropertyException)e));
    }
    else {
      LogMessageUtil.processInternalError(e, currentFile);
    }
  }

  public void add(PsiElement element, String message) {
    add(message, getLineNumber(element));
  }

  public void add(String message, int lineNumber) {
    problems.add(new ProblemDescriptor(message, currentFile, lineNumber));
  }

  public void add(String message) {
    problems.add(new ProblemDescriptor(message, null, -1));
  }
}
