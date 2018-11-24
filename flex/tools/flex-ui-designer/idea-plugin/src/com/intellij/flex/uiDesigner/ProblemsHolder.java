package com.intellij.flex.uiDesigner;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProblemsHolder {
  private static final Logger LOG = Logger.getInstance(ProblemsHolder.class.getName());

  private final List<ProblemDescriptor> problems = new ArrayList<>();

  private VirtualFile currentFile;
  private boolean logDisabled;

  public boolean isEmpty() {
    return problems.isEmpty();
  }

  public void disableLog() {
    logDisabled = true;
  }

  private static int getLineNumber(PsiElement element) {
    InjectedLanguageManager manager = InjectedLanguageManager.getInstance(element.getProject());
    final int elementTextOffset = manager.injectedToHost(element, element.getTextOffset());
    final PsiFile psiFile = manager.getTopLevelFile(element);
    final Document document = PsiDocumentManager.getInstance(element.getProject()).getCachedDocument(psiFile);
    assert document != null;
    return document.getLineNumber(elementTextOffset) + 1;
  }

  public void setCurrentFile(@Nullable VirtualFile currentFile) {
    this.currentFile = currentFile;
  }

  public List<ProblemDescriptor> getProblems() {
    LOG.assertTrue(currentFile == null);
    return problems;
  }

  public void add(InvalidPropertyException e) {
    final ProblemDescriptor problemDescriptor = new ProblemDescriptor(e.getMessage(), currentFile,
                                                                      e.getPsiElement() == null ? -1 : getLineNumber(e.getPsiElement()));
    problems.add(problemDescriptor);
    if (e.getCause() != null && !logDisabled) {
      LOG.error(LogMessageUtil.createEvent(e.getMessage(), e.getCause(), problemDescriptor));
    }
  }

  public void add(final PsiElement element, final RuntimeException e, final String propertyName) {
    String error;
    boolean dontLog = logDisabled;
    if (e instanceof NumberFormatException) {
      error = e.getMessage();
      final String prefix = "For input string: \"";
      if (error.startsWith(prefix)) {
        error = FlashUIDesignerBundle.message("error.write.property.numeric.value",
                                              error.substring(prefix.length(), error.charAt(error.length() - 1) == '"'
                                                                               ? error.length() - 1
                                                                               : error.length()), propertyName);
        dontLog = true;
      }
    }
    else {
      error = FlashUIDesignerBundle.message("error.write.property", propertyName);
    }

    final ProblemDescriptor problemDescriptor = new ProblemDescriptor(error, currentFile, getLineNumber(element));
    if (!dontLog) {
      LOG.error(LogMessageUtil.createEvent(error, e, problemDescriptor));
    }
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
