package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.generated.types.AnalysisError;
import com.google.dart.server.generated.types.AnalysisErrorSeverity;
import com.google.dart.server.generated.types.AnalysisErrorType;
import com.google.dart.server.generated.types.Location;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.fixes.DartQuickFixSet;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import com.jetbrains.lang.dart.psi.DartExpressionCodeFragment;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartAnalysisServerAnnotator
  extends ExternalAnnotator<DartAnalysisServerAnnotator.AnnotatorInfo, AnalysisError[]> {

  public static class AnnotatorInfo {
    @NotNull public final Project myProject;
    @NotNull public final String myFilePath;
    private boolean myLongerAnalysisTimeout;

    public AnnotatorInfo(@NotNull final Project project, @NotNull final String filePath) {
      myProject = project;
      myFilePath = filePath;
    }

    public void setLongerAnalysisTimeout(final boolean longerAnalysisTimeout) {
      myLongerAnalysisTimeout = longerAnalysisTimeout;
    }

    public boolean isLongerAnalysisTimeout() {
      return myLongerAnalysisTimeout;
    }
  }

  @Nullable
  @Override
  public AnnotatorInfo collectInformation(@NotNull final PsiFile psiFile, @Nullable final Editor editor, boolean hasErrors) {
    final VirtualFile annotatedFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (annotatedFile == null) return null;

    if (!serverReadyForRequest(psiFile)) return null;

    if (editor != null) {
      // editor is null if DartAnalysisServerGlobalInspectionContext.analyzeFile() is running
      DartAnalysisServerService.getInstance().addPriorityFile(annotatedFile);
    }

    DartAnalysisServerService.getInstance().updateFilesContent();

    return new AnnotatorInfo(psiFile.getProject(), annotatedFile.getPath());
  }

  public static boolean serverReadyForRequest(@NotNull PsiFile psiFile) {
    if (psiFile instanceof DartExpressionCodeFragment) return false;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return true;

    final Project project = module.getProject();
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !isDartSDKVersionSufficient(sdk)) return false;

    if (!DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) return false;

    if (psiFile instanceof XmlFile && !containsDartEmbeddedContent((XmlFile)psiFile)) return false;

    if (DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile)) return false;

    if (!DartAnalysisServerService.getInstance().serverReadyForRequest(project, sdk)) return false;

    return true;
  }

  public static boolean isDartSDKVersionSufficient(@NotNull final DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), DartAnalysisServerService.MIN_SDK_VERSION) > 0;
  }

  @Nullable
  @Override
  public AnalysisError[] doAnnotate(@NotNull final AnnotatorInfo info) {
    return DartAnalysisServerService.getInstance().analysis_getErrors(info);
  }

  @Override
  public void apply(@NotNull final PsiFile psiFile, @Nullable final AnalysisError[] errors, @NotNull final AnnotationHolder holder) {
    if (errors == null || errors.length == 0) return;

    final long psiModificationCount = psiFile.getManager().getModificationTracker().getModificationCount();

    for (AnalysisError error : errors) {
      final Annotation annotation = annotate(holder, error, psiFile.getTextLength());

      if (annotation != null) {
        final DartQuickFixSet quickFixSet = new DartQuickFixSet(FileUtil.toSystemIndependentName(error.getLocation().getFile()),
                                                                error.getLocation().getOffset(),
                                                                psiModificationCount);
        for (IntentionAction quickFix : quickFixSet.getQuickFixes()) {
          annotation.registerFix(quickFix);
        }
      }
    }
  }

  public static boolean shouldIgnoreMessageFromDartAnalyzer(@NotNull final AnalysisError error) {
    // already done using IDE engine
    if (AnalysisErrorType.TODO.equals(error.getType())) return true;

    return false;
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

  @Nullable
  private static Annotation annotate(@NotNull final AnnotationHolder holder, @NotNull final AnalysisError error, final int fileTextLength) {
    final Location location = error.getLocation();

    int highlightingStart = location.getOffset();
    int highlightingEnd = location.getOffset() + location.getLength();
    if (highlightingEnd > fileTextLength) highlightingEnd = fileTextLength;
    if (highlightingStart > 0 && highlightingStart >= highlightingEnd) highlightingStart = highlightingEnd - 1;

    final TextRange textRange = new TextRange(highlightingStart, highlightingEnd);

    final String severity = error.getSeverity();
    final String message = StringUtil.notNullize(error.getMessage());

    final Annotation annotation = AnalysisErrorSeverity.INFO.equals(severity)
                                  ? holder.createWeakWarningAnnotation(textRange, message)
                                  : AnalysisErrorSeverity.WARNING.equals(severity)
                                    ? holder.createWarningAnnotation(textRange, message)
                                    : AnalysisErrorSeverity.ERROR.equals(severity)
                                      ? holder.createErrorAnnotation(textRange, message)
                                      : null;

    final ProblemHighlightType specialHighlightType = annotation == null ? null : getSpecialHighlightType(message);
    if (specialHighlightType != null) {
      annotation.setHighlightType(specialHighlightType);
    }

    return annotation;
  }

  @Nullable
  private static ProblemHighlightType getSpecialHighlightType(@NotNull final String errorMessage) {
    // see [Dart repo]/pkg/analyzer/lib/src/generated/error.dart

    if (errorMessage.equals("Unused import") ||
        errorMessage.equals("Duplicate import") ||
        errorMessage.endsWith(" is not used")) {
      return ProblemHighlightType.LIKE_UNUSED_SYMBOL;
    }

    if (errorMessage.endsWith(" is deprecated")) {
      return ProblemHighlightType.LIKE_DEPRECATED;
    }

    return null;
  }
}
