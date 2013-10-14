package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.error.AnalysisError;
import com.google.dart.engine.source.Source;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.validation.fixes.DartResolverErrorCode;
import com.jetbrains.lang.dart.validation.fixes.DartTypeErrorCode;
import com.jetbrains.lang.dart.validation.fixes.FixAndIntentionAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class DartInProcessAnnotator extends ExternalAnnotator<Pair<DartFileBasedSource, AnalysisContext>, AnalysisError[]> {
  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartInProcessAnnotator");

  @Override
  public Pair<DartFileBasedSource, AnalysisContext> collectInformation(@NotNull final PsiFile psiFile) {
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (virtualFile == null) return null;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return null;

    final DartSettings settings = DartSettings.getSettingsForModule(module);
    if (settings == null) return null;

    final String sdkPath = settings.getSdkPath();
    if (StringUtil.isEmptyOrSpaces(sdkPath)) return null;

    final File sdkDir = new File(sdkPath);
    if (!sdkDir.isDirectory()) return null;

    final VirtualFile packagesFolder = DartResolveUtil.findPackagesFolder(psiFile);

    return Pair.create(DartFileBasedSource.getSource(psiFile.getProject(), virtualFile),
                       DartAnalyzerService.getInstance(psiFile.getProject()).getAnalysisContext(sdkPath, packagesFolder));
  }

  @Override
  @Nullable
  public AnalysisError[] doAnnotate(final Pair<DartFileBasedSource, AnalysisContext> sourceAndContext) {
    try {
      return sourceAndContext.second.computeErrors(sourceAndContext.first);
    }
    catch (AnalysisException e) {
      LOG.info(e);
    }
    return null;
  }

  @Override
  public void apply(@NotNull PsiFile psiFile, @Nullable AnalysisError[] messages, @NotNull AnnotationHolder holder) {
    if (messages == null || !psiFile.isValid()) return;

    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (virtualFile == null) return;

    for (AnalysisError message : messages) {
      final Source source = message.getSource();
      if (!(source instanceof DartFileBasedSource) || ((DartFileBasedSource)source).getFile() != virtualFile) {
        LOG.warn("Unexpected Source: " + source + ",\nfile: " + virtualFile.getPath());
        continue;
      }

      final Annotation annotation = annotate(holder, message);
      if (annotation != null) {
        registerFixes(psiFile, annotation, message);
      }
    }
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
  private static Annotation annotate(final AnnotationHolder holder, final AnalysisError message) {
    final TextRange textRange = new TextRange(message.getOffset(), message.getOffset() + message.getLength());

    switch (message.getErrorCode().getErrorSeverity()) {
      case NONE:
        return null;
      case INFO:
        return holder.createInfoAnnotation(textRange, message.getMessage());
      case WARNING:
        return holder.createWarningAnnotation(textRange, message.getMessage());
      case ERROR:
        return holder.createErrorAnnotation(textRange, message.getMessage());
    }
    return null;
  }
}
