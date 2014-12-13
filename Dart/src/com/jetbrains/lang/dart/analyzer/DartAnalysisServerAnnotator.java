package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.generated.types.*;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
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
import com.jetbrains.lang.dart.validation.fixes.DartServerFixIntention;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartAnalysisServerAnnotator extends ExternalAnnotator<PsiFile, AnalysisError[]> {

  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator");

  @Override
  @Nullable
  public PsiFile collectInformation(@NotNull final PsiFile psiFile) {
    if (psiFile instanceof DartExpressionCodeFragment) return null;

    final VirtualFile annotatedFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (annotatedFile == null) return null;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return null;

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) return null;

    if (psiFile instanceof XmlFile && !containsDartEmbeddedContent((XmlFile)psiFile)) return null;

    if (FileUtil.isAncestor(sdk.getHomePath(), annotatedFile.getPath(), true)) return null;

    if (PsiDocumentManager.getInstance(psiFile.getProject()).getCachedDocument(psiFile) == null) return null;

    return psiFile;
  }

  @Override
  @Nullable
  public AnalysisError[] doAnnotate(@NotNull final PsiFile psiFile) {
    // todo: need to figure out how to send only the diff
    //final CharSequence charSequence = editor.getDocument().getCharsSequence();
    //final String fileContent = charSequence.toString();
    //final THashMap<String, Object> files = new THashMap<String, Object>();
    //final ArrayList<SourceEdit> sourceEdits = new ArrayList<SourceEdit>(1);
    //final SourceEdit sourceEdit = new SourceEdit(0, fileContent.length() - 1, fileContent, null);
    //sourceEdits.add(sourceEdit);
    //files.put(psiFile.getOriginalFile().getVirtualFile().getPath(), new ChangeContentOverlay(sourceEdits));
    //DartAnalysisServerService.getInstance(psiFile.getProject()).updateContent(files);
    return DartAnalysisServerService.getInstance().analysis_getErrors(psiFile);
  }

  @Override
  public void apply(@NotNull final PsiFile psiFile, @Nullable final AnalysisError[] errors, @NotNull final AnnotationHolder holder) {
    if (errors == null) return;

    final Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getCachedDocument(psiFile);
    if (document == null) return;

    for (AnalysisError error : errors) {
      if (shouldIgnoreMessageFromDartAnalyzer(error)) continue;
      final Annotation annotation = annotate(document, holder, error);
      if (annotation != null) {
        registerFixes(psiFile, document, annotation, error);
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
  private static Annotation annotate(@NotNull final Document document,
                                     @NotNull final AnnotationHolder holder,
                                     @NotNull final AnalysisError error) {
    final String severity = error.getSeverity();
    if (severity != null) {
      final TextRange textRange = getRealTextRange(document, error.getLocation());
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

  private static void registerFixes(@NotNull final PsiFile psiFile,
                                    @NotNull Document document,
                                    @NotNull final Annotation annotation,
                                    @NotNull final AnalysisError error) {
    final TextRange textRange = getRealTextRange(document, error.getLocation());
    final List<AnalysisErrorFixes> fixes = DartAnalysisServerService.getInstance().analysis_getFixes(psiFile, textRange.getStartOffset());
    if (fixes == null) return;
    for (AnalysisErrorFixes fixList : fixes) {
      for (SourceChange change : fixList.getFixes()) {
        annotation.registerFix(new DartServerFixIntention(change));
      }
    }
  }

  @NotNull
  private static TextRange getRealTextRange(Document document, Location location) {
    final int realStartLineOffset = document.getLineStartOffset(location.getStartLine() - 1);
    final int realOffset = realStartLineOffset + location.getStartColumn() - 1;
    // todo if there are CRLF chars within the length after the realOffset, then realLength will be incorrect:
    final int realLength = realOffset + location.getLength();
    return new TextRange(realOffset, realLength);
  }

  private static boolean containsDartEmbeddedContent(@NotNull final XmlFile file) {
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
}
