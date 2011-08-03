package com.intellij.flex.uiDesigner;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProblemsHolder {
  private static final Logger LOG = Logger.getInstance(ProblemsHolder.class.getName());

  private final List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();

  private VirtualFile currentFile;

  private static Document getDocument(@NotNull PsiElement element) {
    VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    assert virtualFile != null;
    return FileDocumentManager.getInstance().getDocument(virtualFile);
  }

  private static int getLineNumber(PsiElement element) {
    return getDocument(element).getLineNumber(element.getTextOffset()) + 1;
  }

  public void setCurrentFile(@Nullable VirtualFile currentFile) {
    this.currentFile = currentFile;
  }

  public ProblemDescriptor[] getResultList() {
    LOG.assertTrue(currentFile == null);
    return problems.toArray(new ProblemDescriptor[problems.size()]);
  }

  public void clear() {
    problems.clear();
  }

  public void add(InvalidPropertyException e) {
    problems.add(new ProblemDescriptor(e.getMessage(), currentFile, e.getPsiElement() == null ? -1 : getLineNumber(e.getPsiElement())));
    if (e.getCause() != null) {
      LOG.error(e.getCause());
    }
  }

  public void add(PsiElement element, RuntimeException e, String propertyName) {
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

    LOG.error(e);
    problems.add(new ProblemDescriptor(error, currentFile, getLineNumber(element)));
  }

  public boolean isEmpty() {
    return problems.isEmpty();
  }

  @SuppressWarnings("MethodMayBeStatic")
  public void add(RuntimeException e) {
    LOG.error(e);
  }

  @SuppressWarnings("MethodMayBeStatic")
  public void add(AssertionError e) {
    LOG.error(e);
  }

  public void add(Throwable e) {
    if (e instanceof InvalidPropertyException) {
      add(((InvalidPropertyException)e));
    }
    else if (e instanceof RuntimeException) {
      add(((RuntimeException)e));
    }
    else {
      add(((AssertionError)e));
    }
  }

  public void add(PsiElement element, String message) {
      problems.add(new ProblemDescriptor(message, currentFile, getLineNumber(element)));
    }

  public void add(String message, int lineNumber) {
    problems.add(new ProblemDescriptor(message, currentFile, lineNumber));
  }

  public void add(String message) {
    problems.add(new ProblemDescriptor(message, null, -1));
  }
}
