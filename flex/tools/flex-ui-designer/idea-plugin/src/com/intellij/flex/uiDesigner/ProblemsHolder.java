// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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

import static com.intellij.flex.uiDesigner.LogMessageUtil.createAttachment;

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
    int line = e.getPsiElement() == null ? -1 : getLineNumber(e.getPsiElement());
    ProblemDescriptor problemDescriptor = new ProblemDescriptor(e.getMessage(), currentFile, line);
    problems.add(problemDescriptor);
    if (e.getCause() != null && !logDisabled) {
      LOG.error(e.getMessage() + ", line: " + problemDescriptor.getLineNumber(), e, createAttachment(problemDescriptor.getFile()));
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

    ProblemDescriptor problemDescriptor = new ProblemDescriptor(error, currentFile, getLineNumber(element));
    if (!dontLog) {
      LOG.error(e.getMessage() + ", line: " + problemDescriptor.getLineNumber(), e, createAttachment(problemDescriptor.getFile()));
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
