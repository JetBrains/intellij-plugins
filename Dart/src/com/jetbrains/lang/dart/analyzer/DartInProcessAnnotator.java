package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.error.*;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import com.jetbrains.lang.dart.psi.DartExpressionCodeFragment;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.validation.fixes.DartWarningCode;
import com.jetbrains.lang.dart.validation.fixes.FixAndIntentionAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DartInProcessAnnotator extends ExternalAnnotator<DartInProcessAnnotator.DartAnnotatorInfo, AnalysisContext> {
  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartInProcessAnnotator");

  public static class DartAnnotatorInfo {
    @NotNull private final AnalysisContext analysisContext;
    @NotNull private final DartFileBasedSource annotatedFile;
    @NotNull private final DartFileBasedSource libraryFile;

    public DartAnnotatorInfo(@NotNull final AnalysisContext analysisContext,
                             @NotNull final DartFileBasedSource annotatedFile,
                             @NotNull final DartFileBasedSource libraryFile) {
      this.analysisContext = analysisContext;
      this.annotatedFile = annotatedFile;
      this.libraryFile = libraryFile;
    }
  }

  @Override
  @Nullable
  public DartAnnotatorInfo collectInformation(@NotNull final PsiFile psiFile) {
    final Project project = psiFile.getProject();

    if (psiFile instanceof DartExpressionCodeFragment) return null;

    final VirtualFile annotatedFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (annotatedFile == null) return null;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return null;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return null;

    if (psiFile instanceof XmlFile && !containsDartEmbeddedContent((XmlFile)psiFile)) return null;

    if (FileUtil.isAncestor(sdk.getHomePath(), annotatedFile.getPath(), true)) return null;

    final List<VirtualFile> libraries = DartResolveUtil.findLibrary(psiFile);
    final VirtualFile libraryFile = libraries.isEmpty() || libraries.contains(annotatedFile) ? annotatedFile : libraries.get(0);

    return new DartAnnotatorInfo(DartAnalyzerService.getInstance(project).getAnalysisContext(annotatedFile, sdk.getHomePath()),
                                 DartFileBasedSource.getSource(project, annotatedFile),
                                 DartFileBasedSource.getSource(project, libraryFile));
  }

  static boolean containsDartEmbeddedContent(@NotNull final XmlFile file) {
    final String text = file.getText();
    int i = -1;
    while ((i = text.indexOf(DartLanguage.DART_MIME_TYPE, i + 1)) != -1) {
      final PsiElement element = file.findElementAt(i);
      final XmlTag tag = element == null ? null : PsiTreeUtil.getParentOfType(element, XmlTag.class);
      if (tag != null && HtmlUtil.isScriptTag(tag) && PsiTreeUtil.getChildOfType(tag, DartEmbeddedContent.class) != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  @Nullable
  public AnalysisContext doAnnotate(final DartAnnotatorInfo annotatorInfo) {
    try {
      if (annotatorInfo.annotatedFile != annotatorInfo.libraryFile) {
        annotatorInfo.analysisContext.computeErrors(annotatorInfo.libraryFile);
      }
      annotatorInfo.analysisContext.computeErrors(annotatorInfo.annotatedFile);
      return annotatorInfo.analysisContext;
    }
    catch (AnalysisException e) {
      LOG.info(e);
    }
    return null;
  }

  @Override
  public void apply(@NotNull final PsiFile psiFile,
                    @Nullable final AnalysisContext analysisContext,
                    @NotNull final AnnotationHolder holder) {
    if (analysisContext == null || !psiFile.isValid()) return;

    final VirtualFile annotatedFile = DartResolveUtil.getRealVirtualFile(psiFile);
    final DartFileBasedSource source = annotatedFile == null ? null : DartFileBasedSource.getSource(psiFile.getProject(), annotatedFile);
    if (source == null) return;

    // analysisContext.getErrors() doesn't perform analysis and returns already calculated errors
    final AnalysisError[] errors = analysisContext.getErrors(source).getErrors();
    if (errors == null || errors.length == 0) return;

    final int fileTextLength = psiFile.getTextLength();

    for (AnalysisError error : errors) {
      if (shouldIgnoreMessageFromDartAnalyzer(error)) continue;

      if (source != error.getSource()) {
        LOG.warn("Unexpected Source: " + error.getSource() + ",\nfile: " + annotatedFile.getPath());
        continue;
      }

      final Annotation annotation = annotate(holder, error, fileTextLength);
      if (annotation != null) {
        registerFixes(psiFile, annotation, error);
      }
    }
  }

  public static boolean shouldIgnoreMessageFromDartAnalyzer(@NotNull final AnalysisError message) {
    if (message.getErrorCode() == TodoCode.TODO) return true; // // already done using IDE engine
    if (message.getErrorCode() == HintCode.DEPRECATED_MEMBER_USE) return true; // already done as DartDeprecatedApiUsageInspection
    return false;
  }

  private static void registerFixes(@NotNull final PsiFile psiFile,
                                    @NotNull final Annotation annotation,
                                    @NotNull final AnalysisError error) {
    //noinspection EnumSwitchStatementWhichMissesCases
    final ErrorCode errorCode = error.getErrorCode();
    final String errorMessage = error.getMessage();
    final ErrorSeverity errorSeverity = errorCode.getErrorSeverity();

    if (!errorSeverity.equals(ErrorSeverity.WARNING)) return;

    List<? extends IntentionAction> fixes = Collections.emptyList();

    final DartWarningCode resolverErrorCode = DartWarningCode.findError(errorCode.toString());
    if (resolverErrorCode != null) {
      fixes = resolverErrorCode.getFixes(psiFile, error.getOffset(), errorMessage);
    }

    if (!fixes.isEmpty()) {
      PsiElement element = psiFile.findElementAt(error.getOffset() + error.getLength() / 2);
      while (element != null && ((annotation.getStartOffset() < element.getTextOffset()) ||
                                 annotation.getEndOffset() > element.getTextRange().getEndOffset())) {
        element = element.getParent();
      }

      if (element != null && (annotation.getStartOffset() != element.getTextRange().getStartOffset() ||
                              annotation.getEndOffset() != element.getTextRange().getEndOffset())) {
        element = null;
      }

      for (IntentionAction intentionAction : fixes) {
        if (intentionAction instanceof FixAndIntentionAction) {
          ((FixAndIntentionAction)intentionAction).setElement(element);
        }
        annotation.registerFix(intentionAction);
      }
    }
  }

  @Nullable
  private static Annotation annotate(@NotNull final AnnotationHolder holder, @NotNull final AnalysisError error, final int fileTextLength) {
    int highlightingStart = error.getOffset();
    int highlightingEnd = error.getOffset() + error.getLength();
    if (highlightingEnd > fileTextLength) highlightingEnd = fileTextLength;
    if (highlightingStart > 0 && highlightingStart >= highlightingEnd) highlightingStart = highlightingEnd - 1;

    final TextRange textRange = new TextRange(highlightingStart, highlightingEnd);
    final ErrorCode errorCode = error.getErrorCode();

    switch (errorCode.getErrorSeverity()) {
      case INFO:
        final Annotation annotation = holder.createWeakWarningAnnotation(textRange, error.getMessage());
        if (errorCode == HintCode.UNUSED_IMPORT || errorCode == HintCode.DUPLICATE_IMPORT) {
          annotation.setHighlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
        return annotation;
      case WARNING:
        return holder.createWarningAnnotation(textRange, error.getMessage());
      case ERROR:
        return holder.createErrorAnnotation(textRange, error.getMessage());
      default:
        return null;
    }
  }
}
