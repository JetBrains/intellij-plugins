package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.generated.types.AnalysisError;
import com.google.dart.server.generated.types.AnalysisErrorSeverity;
import com.google.dart.server.generated.types.AnalysisErrorType;
import com.google.dart.server.generated.types.Location;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    if (PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile) == null) return null;

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
  public void apply(@NotNull PsiFile psiFile, @NotNull AnalysisError[] errors, @NotNull AnnotationHolder holder) {
    final Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
    for (AnalysisError error : errors) {
      if (shouldIgnoreMessageFromDartAnalyzer(error)) continue;
      final Annotation annotation = annotate(document, holder, error);
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
  private static Annotation annotate(@NotNull final Document document,
                                     @NotNull final AnnotationHolder holder,
                                     @NotNull final AnalysisError error) {
    final String severity = error.getSeverity();
    if (severity != null) {
      final Location location = error.getLocation();
      final int realStartLineOffset = document.getLineStartOffset(location.getStartLine() - 1);
      final int realOffset = realStartLineOffset + location.getStartColumn() - 1;
      // todo if there are CRLF chars within the length after the realOffset, then realLength will be incorrect:
      final int realLength = realOffset + location.getLength();

      final TextRange textRange = new TextRange(realOffset, realLength);
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
