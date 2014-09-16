package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.error.AnalysisError;
import com.google.dart.engine.error.ErrorCode;
import com.google.dart.engine.error.HintCode;
import com.google.dart.engine.error.TodoCode;
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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import com.jetbrains.lang.dart.psi.DartExpressionCodeFragment;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.validation.fixes.DartResolverErrorCode;
import com.jetbrains.lang.dart.validation.fixes.DartTypeErrorCode;
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

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) return null;

    if (psiFile instanceof XmlFile && !containsDartEmbeddedContent((XmlFile)psiFile)) return null;

    if (FileUtil.isAncestor(sdk.getHomePath(), annotatedFile.getPath(), true)) return null;

    final List<VirtualFile> libraries = DartResolveUtil.findLibrary(psiFile, GlobalSearchScope.projectScope(project));
    final VirtualFile libraryFile = libraries.isEmpty() || libraries.contains(annotatedFile) ? annotatedFile : libraries.get(0);

    return new DartAnnotatorInfo(DartAnalyzerService.getInstance(project).getAnalysisContext(annotatedFile, sdk.getHomePath()),
                                 DartFileBasedSource.getSource(project, annotatedFile),
                                 DartFileBasedSource.getSource(project, libraryFile));
  }

  private static boolean containsDartEmbeddedContent(final XmlFile file) {
    final String text = file.getText();
    int i = -1;
    while ((i = text.indexOf("application/dart", i + 1)) != -1) {
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
    final AnalysisError[] messages = analysisContext.getErrors(source).getErrors();
    if (messages == null || messages.length == 0) return;

    final int fileTextLength = psiFile.getTextLength();

    for (AnalysisError message : messages) {
      if (shouldIgnoreMessageFromDartAnalyzer(message)) continue;

      if (source != message.getSource()) {
        LOG.warn("Unexpected Source: " + message.getSource() + ",\nfile: " + annotatedFile.getPath());
        continue;
      }

      final Annotation annotation = annotate(holder, message, fileTextLength);
      if (annotation != null) {
        registerFixes(psiFile, annotation, message);
      }
    }
  }

  public static boolean shouldIgnoreMessageFromDartAnalyzer(final @NotNull AnalysisError message) {
    if (message.getErrorCode() == TodoCode.TODO) return true; // // already done using IDE engine
    if (message.getErrorCode() == HintCode.DEPRECATED_MEMBER_USE) return true; // already done as DartDeprecatedApiUsageInspection
    return false;
  }

  private static void registerFixes(final PsiFile psiFile, final Annotation annotation, final AnalysisError message) {
    List<? extends IntentionAction> fixes = Collections.emptyList();

    //noinspection EnumSwitchStatementWhichMissesCases
    switch (message.getErrorCode().getType()) {
      case STATIC_WARNING:
        final DartResolverErrorCode resolverErrorCode = DartResolverErrorCode.findError(message.getErrorCode().toString());
        if (resolverErrorCode != null) {
          fixes = resolverErrorCode.getFixes(psiFile, message.getOffset(), message.getMessage());
        }
        break;
      case STATIC_TYPE_WARNING:
      case COMPILE_TIME_ERROR:
        final DartTypeErrorCode typeErrorCode = DartTypeErrorCode.findError(message.getErrorCode().toString());
        if (typeErrorCode != null) {
          fixes = typeErrorCode.getFixes(psiFile, message.getOffset(), message.getMessage());
        }
        break;
    }


    if (!fixes.isEmpty()) {
      PsiElement element = psiFile.findElementAt(message.getOffset() + message.getLength() / 2);
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
  private static Annotation annotate(final AnnotationHolder holder, final AnalysisError message, final int fileTextLength) {
    int highlightingStart = message.getOffset();
    int highlightingEnd = message.getOffset() + message.getLength();
    if (highlightingEnd > fileTextLength) highlightingEnd = fileTextLength;
    if (highlightingStart >= highlightingEnd) highlightingStart = highlightingEnd - 1;

    final TextRange textRange = new TextRange(highlightingStart, highlightingEnd);
    final ErrorCode errorCode = message.getErrorCode();

    switch (errorCode.getErrorSeverity()) {
      case NONE:
        return null;
      case INFO:
        final Annotation annotation = holder.createWeakWarningAnnotation(textRange, message.getMessage());
        if (errorCode == HintCode.UNUSED_IMPORT || errorCode == HintCode.DUPLICATE_IMPORT) {
          annotation.setHighlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL);
        }
        return annotation;
      case WARNING:
        return holder.createWarningAnnotation(textRange, message.getMessage());
      case ERROR:
        return holder.createErrorAnnotation(textRange, message.getMessage());
    }
    return null;
  }
}
