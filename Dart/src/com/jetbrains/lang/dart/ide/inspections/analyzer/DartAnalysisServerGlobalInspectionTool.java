package com.jetbrains.lang.dart.ide.inspections.analyzer;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DartAnalysisServerGlobalInspectionTool extends GlobalInspectionTool {
  @Override
  public void runInspection(@NotNull final AnalysisScope scope,
                            @NotNull final InspectionManager manager,
                            @NotNull final GlobalInspectionContext globalContext,
                            @NotNull final ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    final DartAnalysisServerGlobalInspectionContext inspectionContext =
      globalContext.getExtension(DartAnalysisServerGlobalInspectionContext.KEY);
    if (inspectionContext == null) return;

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        for (Map.Entry<VirtualFile, AnalysisError[]> entry : inspectionContext.getVirtualFile2ErrorsMap().entrySet()) {
          final VirtualFile file = entry.getKey();
          final AnalysisError[] analysisErrors = entry.getValue();
          for (AnalysisError analysisError : analysisErrors) {
            processMessage(file, analysisError, globalContext, manager, problemDescriptionsProcessor);
          }
        }
      }
    });
  }

  @Override
  public boolean isGraphNeeded() {
    return false;
  }

  protected void processMessage(final @NotNull VirtualFile file,
                                final @NotNull AnalysisError analysisError,
                                final @NotNull GlobalInspectionContext globalContext,
                                final @NotNull InspectionManager manager,
                                final @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    if (DartAnalysisServerAnnotator.shouldIgnoreMessageFromDartAnalyzer(analysisError)) return;

    final PsiFile psiFile = PsiManager.getInstance(globalContext.getProject()).findFile(file);
    if (psiFile == null) return;

    final ProblemDescriptor descriptor = computeProblemDescriptor(manager, psiFile, analysisError);
    problemDescriptionsProcessor.addProblemElement(globalContext.getRefManager().getReference(psiFile), descriptor);
  }

  @NotNull
  private static ProblemDescriptor computeProblemDescriptor(final @NotNull InspectionManager manager,
                                                            final @NotNull PsiFile psiFile,
                                                            final @NotNull AnalysisError analysisError) {
    final Location location = analysisError.getLocation();
    final int startOffset = location.getOffset();
    final TextRange textRange = new TextRange(startOffset, startOffset + location.getLength());
    PsiElement element = psiFile.findElementAt(startOffset + (location.getLength() / 2));
    while (element != null && textRange.getStartOffset() < element.getTextOffset()) {
      element = element.getParent();
    }

    if (element != null && textRange.equals(element.getTextRange())) {
      return computeProblemDescriptor(manager, analysisError, element);
    }
    return computeProblemDescriptor(manager, analysisError, psiFile, textRange);
  }

  @NotNull
  private static ProblemDescriptor computeProblemDescriptor(final @NotNull InspectionManager manager,
                                                            final @NotNull AnalysisError analysisError,
                                                            final @NotNull PsiElement element) {
    return manager
      .createProblemDescriptor(element, analysisError.getMessage(), (LocalQuickFix)null,
                               convertHighlightingType(analysisError.getSeverity()), true);
  }

  @NotNull
  private static ProblemDescriptor computeProblemDescriptor(final @NotNull InspectionManager manager,
                                                            final @NotNull AnalysisError analysisError,
                                                            final @NotNull PsiFile psiFile,
                                                            final @NotNull TextRange range) {
    return manager.createProblemDescriptor(
      psiFile,
      range,
      analysisError.getMessage(),
      convertHighlightingType(analysisError.getSeverity()),
      true
    );
  }

  @NotNull
  private static ProblemHighlightType convertHighlightingType(@Nullable String severity) {
    if (AnalysisErrorSeverity.INFO.equals(severity)) {
      return ProblemHighlightType.WEAK_WARNING;
    }
    else if (AnalysisErrorSeverity.WARNING.equals(severity)) {
      return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
    else if (AnalysisErrorSeverity.ERROR.equals(severity)) {
      return ProblemHighlightType.ERROR;
    }
    else {
      return ProblemHighlightType.INFORMATION;
    }
  }
}
