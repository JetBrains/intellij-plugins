package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.generated.types.AnalysisError;
import com.google.dart.server.generated.types.AnalysisErrorSeverity;
import com.google.dart.server.generated.types.AnalysisErrorType;
import com.google.dart.server.generated.types.Location;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartAnalysisServerAnnotator extends ExternalAnnotator<PsiFile, AnalysisError[]> {

  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator");

  @Override
  @NotNull
  public PsiFile collectInformation(@NotNull final PsiFile psiFile, @NotNull final Editor editor, final boolean hasErrors) {
    // todo: need to figure out how to send only the diff
    //final CharSequence charSequence = editor.getDocument().getCharsSequence();
    //final String fileContent = charSequence.toString();
    //final THashMap<String, Object> files = new THashMap<String, Object>();
    //final ArrayList<SourceEdit> sourceEdits = new ArrayList<SourceEdit>(1);
    //final SourceEdit sourceEdit = new SourceEdit(0, fileContent.length() - 1, fileContent, null);
    //sourceEdits.add(sourceEdit);
    //files.put(psiFile.getOriginalFile().getVirtualFile().getPath(), new ChangeContentOverlay(sourceEdits));
    //DartAnalysisServerService.getInstance(psiFile.getProject()).updateContent(files);
    return psiFile;
  }

  @Override
  @Nullable
  public PsiFile collectInformation(@NotNull final PsiFile psiFile) {
    return psiFile;
  }

  @Override
  @NotNull
  public AnalysisError[] doAnnotate(@NotNull final PsiFile psiFile) {
    return DartAnalysisServerService.getInstance().analysis_getErrors(psiFile);
  }

  @Override
  public void apply(@NotNull PsiFile psiFile, @NotNull AnalysisError[] errors, @NotNull AnnotationHolder holder) {
    for (AnalysisError error : errors) {
      if (shouldIgnoreMessageFromDartAnalyzer(error)) continue;
      final Annotation annotation = annotate(holder, error);
      if (annotation != null) {
        registerFixes(psiFile, annotation, error);
      }
    }
  }

  private static boolean shouldIgnoreMessageFromDartAnalyzer(@NotNull final AnalysisError error) {
    final String errorType = error.getType();
    // already done using IDE engine
    if (AnalysisErrorType.TODO.equals(errorType)) return true;
    // already done as DartDeprecatedApiUsageInspection
    if (AnalysisErrorType.HINT.equals(errorType) && error.getMessage().endsWith("' is deprecated")) return true;
    return false;
  }

  @Nullable
  private static Annotation annotate(@NotNull final AnnotationHolder holder, @NotNull final AnalysisError error) {
    final String severity = error.getSeverity();
    if (severity != null) {
      final Location location = error.getLocation();
      final TextRange textRange = new TextRange(location.getOffset(), location.getOffset() + location.getLength());
      if (severity.equals(AnalysisErrorSeverity.INFO)) {
        return holder.createWeakWarningAnnotation(textRange, error.getMessage());
      }
      else if (severity.equals(AnalysisErrorSeverity.WARNING)) {
        return holder.createWarningAnnotation(textRange, error.getMessage());
      }
      else if (severity.equals(AnalysisErrorSeverity.ERROR)) {
        return holder.createErrorAnnotation(textRange, error.getMessage());
      }
    }
    return null;
  }

  private void registerFixes(@NotNull final PsiFile psiFile, @NotNull final Annotation annotation, @NotNull final AnalysisError error) {
    // todo: implement
  }
}
