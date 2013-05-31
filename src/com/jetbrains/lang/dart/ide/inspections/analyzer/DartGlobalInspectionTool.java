package com.jetbrains.lang.dart.ide.inspections.analyzer;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.lang.dart.analyzer.AnalyzerMessage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartGlobalInspectionTool extends GlobalInspectionTool {
  @Override
  public void runInspection(AnalysisScope scope,
                            final InspectionManager manager,
                            final GlobalInspectionContext globalContext,
                            final ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    final DartGlobalInspectionContext inspectionContext = globalContext.getExtension(DartGlobalInspectionContext.KEY);
    if (inspectionContext == null) {
      return;
    }

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        for (List<AnalyzerMessage> messageList : inspectionContext.getLibraryRoot2Errors().values()) {
          for (AnalyzerMessage message : messageList) {
            processMessage(message, globalContext, manager, problemDescriptionsProcessor);
          }
        }
      }
    });
  }

  protected void processMessage(AnalyzerMessage message,
                                GlobalInspectionContext globalContext,
                                InspectionManager manager,
                                ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    final PsiFile psiFile = PsiManager.getInstance(globalContext.getProject()).findFile(message.getVirtualFile());
    final ProblemDescriptor descriptor = computeProblemDescriptor(manager, psiFile, message);
    if (descriptor != null && psiFile != null) {
      problemDescriptionsProcessor.addProblemElement(globalContext.getRefManager().getReference(psiFile), descriptor);
    }
  }

  @Nullable
  private static ProblemDescriptor computeProblemDescriptor(InspectionManager manager, @Nullable PsiFile psiFile, AnalyzerMessage message) {
    if (psiFile == null) {
      return null;
    }
    final Document document = psiFile.getViewProvider().getDocument();
    if (document == null) {
      return null;
    }
    final int startOffset = document.getLineStartOffset(message.getLine()) + message.getOffset();
    final TextRange textRange = new TextRange(startOffset, startOffset + message.getLength());
    PsiElement element = psiFile.findElementAt(startOffset + message.getLength() / 2);
    while (element != null && textRange.getStartOffset() < element.getTextOffset()) {
      element = element.getParent();
    }

    if (element != null && textRange.equals(element.getTextRange())) {
      return computeProblemDescriptor(manager, message, element);
    }
    return computeProblemDescriptor(manager, message, psiFile, textRange);
  }

  private static ProblemDescriptor computeProblemDescriptor(InspectionManager manager, AnalyzerMessage message, PsiElement element) {
    return manager.createProblemDescriptor(element, message.getMessage(), (LocalQuickFix)null, annotateElement(message), true);
  }


  private static ProblemDescriptor computeProblemDescriptor(InspectionManager manager,
                                                            AnalyzerMessage message,
                                                            PsiFile psiFile,
                                                            TextRange range) {
    return manager.createProblemDescriptor(
      psiFile,
      range,
      message.getMessage(),
      annotateElement(message),
      true
    );
  }


  private static ProblemHighlightType annotateElement(AnalyzerMessage message) {
    switch (message.getType()) {
      case INFO:
        return ProblemHighlightType.WEAK_WARNING;
      case WARNING:
        return ProblemHighlightType.WEAK_WARNING;
      case ERROR:
        return ProblemHighlightType.ERROR;
    }
    return ProblemHighlightType.WEAK_WARNING;
  }
}
