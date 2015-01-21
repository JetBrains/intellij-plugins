package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.generated.types.*;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.psi.DartExpressionCodeFragment;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.validation.fixes.DartServerFixIntention;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartAnalysisServerAnnotator
  extends ExternalAnnotator<DartAnalysisServerAnnotator.AnnotatorInfo, DartAnalysisServerAnnotator.ServerResult> {

  static class AnnotatorInfo {
    @NotNull public final Project myProject;
    @NotNull public final String myFilePath;

    public AnnotatorInfo(@NotNull final Project project, @NotNull final String filePath) {
      myProject = project;
      myFilePath = filePath;
    }
  }

  static class ServerResult {
    @NotNull private final Map<AnalysisError, List<AnalysisErrorFixes>> myErrorsAndFixes =
      new THashMap<AnalysisError, List<AnalysisErrorFixes>>();

    void add(@NotNull final AnalysisError error, @NotNull final List<AnalysisErrorFixes> fixes) {
      myErrorsAndFixes.put(error, fixes);
    }

    @NotNull
    public Map<AnalysisError, List<AnalysisErrorFixes>> getErrorsAndFixes() {
      return myErrorsAndFixes;
    }
  }

  @Nullable
  @Override
  public AnnotatorInfo collectInformation(@NotNull final PsiFile psiFile, @NotNull final Editor editor, final boolean hasErrors) {
    if (hasErrors) return null;

    if (psiFile instanceof DartExpressionCodeFragment) return null;

    final VirtualFile annotatedFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (annotatedFile == null) return null;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return null;

    final DartSdk sdk = DartSdk.getDartSdk(module.getProject());
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), DartAnalysisServerService.MIN_SDK_VERSION) < 0) return null;

    if (!DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) return null;

    if (psiFile instanceof XmlFile && !DartInProcessAnnotator.containsDartEmbeddedContent((XmlFile)psiFile)) return null;

    if (DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile)) return null;

    if (!DartAnalysisServerService.getInstance().serverReadyForRequest(module.getProject(), sdk.getHomePath())) return null;

    DartAnalysisServerService.getInstance().updateFilesContent();

    return new AnnotatorInfo(psiFile.getProject(), annotatedFile.getPath());
  }

  @Override
  @Nullable
  public ServerResult doAnnotate(@NotNull final AnnotatorInfo info) {
    final AnalysisError[] errors = DartAnalysisServerService.getInstance().analysis_getErrors(info);
    if (errors == null || errors.length == 0) return null;

    final ServerResult result = new ServerResult();

    for (AnalysisError error : errors) {
      if (shouldIgnoreMessageFromDartAnalyzer(error)) continue;

      final List<AnalysisErrorFixes> fixes =
        DartAnalysisServerService.getInstance().analysis_getFixes(info, error.getLocation().getOffset());
      result.add(error, fixes != null ? fixes : Collections.<AnalysisErrorFixes>emptyList());
    }

    return result;
  }

  @Override
  public void apply(@NotNull final PsiFile psiFile, @Nullable final ServerResult serverResult, @NotNull final AnnotationHolder holder) {
    if (serverResult == null || serverResult.getErrorsAndFixes().isEmpty()) return;

    final Map<AnalysisError, List<AnalysisErrorFixes>> errorsAndFixesMap = serverResult.getErrorsAndFixes();
    for (Map.Entry<AnalysisError, List<AnalysisErrorFixes>> entry : errorsAndFixesMap.entrySet()) {
      final AnalysisError error = entry.getKey();
      final List<AnalysisErrorFixes> fixes = entry.getValue();

      final Annotation annotation = annotate(holder, error);
      if (annotation != null && fixes != null) {
        for (AnalysisErrorFixes fixList : fixes) {
          for (SourceChange change : fixList.getFixes()) {
            annotation.registerFix(new DartServerFixIntention(change));
          }
        }
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
  private static Annotation annotate(@NotNull final AnnotationHolder holder,
                                     @NotNull final AnalysisError error) {
    final TextRange textRange = convertLocationToTextRange(error.getLocation());
    final String severity = error.getSeverity();
    if (AnalysisErrorSeverity.INFO.equals(severity)) {
      final Annotation annotation = holder.createWeakWarningAnnotation(textRange, error.getMessage());
      if ("Unused import".equals(error.getMessage()) || "Duplicate import".equals(error.getMessage())) {
        annotation.setHighlightType(ProblemHighlightType.LIKE_UNUSED_SYMBOL);
      }
      return annotation;
    }
    else if (AnalysisErrorSeverity.WARNING.equals(severity)) {
      return holder.createWarningAnnotation(textRange, error.getMessage());
    }
    else if (AnalysisErrorSeverity.ERROR.equals(severity)) {
      return holder.createErrorAnnotation(textRange, error.getMessage());
    }

    return null;
  }

  @NotNull
  private static TextRange convertLocationToTextRange(@NotNull final Location location) {
    final int offset = location.getOffset();
    return new TextRange(location.getOffset(), offset + location.getLength());
  }
}
