package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.AnalyzerMessage;
import com.jetbrains.lang.dart.analyzer.DartAnalyzerDriver;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.validation.fixes.DartResolverErrorCode;
import com.jetbrains.lang.dart.validation.fixes.DartTypeErrorCode;
import com.jetbrains.lang.dart.validation.fixes.FixAndIntentionAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartExternalAnnotator extends ExternalAnnotator<DartAnalyzerDriver, List<AnalyzerMessage>> {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.annotator.DartExternalAnnotator");

  @Override
  public DartAnalyzerDriver collectionInformation(@NotNull final PsiFile file) {
    final List<VirtualFile> library = DartResolveUtil.findLibrary(file, GlobalSearchScope.projectScope(file.getProject()));
    final VirtualFile libraryRoot = library.isEmpty() ? DartResolveUtil.getRealVirtualFile(file) : library.iterator().next();
    if (libraryRoot == null) {
      LOG.debug("No library root for " + file.getName());
      return null;
    }

    final Module module = ModuleUtilCore.findModuleForFile(libraryRoot, file.getProject());
    if (module == null) {
      LOG.debug("No module for " + file.getName());
      return null;
    }

    DartSettings settings = DartSettings.getSettingsForModule(module);
    if (settings == null) {
      LOG.debug("No settings for module " + module.getName());
      ModuleType moduleType = ModuleType.get(module);
      LOG.debug("Type " + (moduleType == null ? null : moduleType.getId()));
    }
    final VirtualFile analyzanalyzer = settings == null ? null : settings.getAnalyzer();
    return analyzanalyzer == null ? null : new DartAnalyzerDriver(module.getProject(), analyzanalyzer, settings.getSdkPath(), libraryRoot);
  }

  @Override
  public List<AnalyzerMessage> doAnnotate(@Nullable final DartAnalyzerDriver analyzerDriver) {
    if (analyzerDriver == null) {
      return Collections.emptyList();
    }
    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      @Override
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    }, ModalityState.defaultModalityState());
    return analyzerDriver.analyze();
  }

  @Override
  public void apply(@NotNull PsiFile file, @Nullable List<AnalyzerMessage> messages, @NotNull AnnotationHolder holder) {
    if (messages == null || !file.isValid()) {
      return;
    }
    final VirtualFile realVirtualFile = DartResolveUtil.getRealVirtualFile(file);
    messages = ContainerUtil.filter(messages, new Condition<AnalyzerMessage>() {
      @Override
      public boolean value(AnalyzerMessage message) {
        return Comparing.equal(message.getVirtualFile(), realVirtualFile);
      }
    });
    for (AnalyzerMessage message : messages) {
      createAnnotation(file, message, holder);
    }
    super.apply(file, messages, holder);
  }

  private static void createAnnotation(@NotNull PsiFile file, @NotNull AnalyzerMessage message, @NotNull AnnotationHolder holder) {
    final Document document = file.getViewProvider().getDocument();
    if (document == null || message.getLine() < 0) {
      return;
    }
    final int startOffset = document.getLineStartOffset(message.getLine()) + message.getOffset();
    final TextRange textRange = new TextRange(startOffset, startOffset + message.getLength());
    PsiElement element = file.findElementAt(startOffset + message.getLength() / 2);
    while (element != null && textRange.getStartOffset() < element.getTextOffset()) {
      element = element.getParent();
    }
    boolean annotateByElement = element != null && textRange.equals(element.getTextRange());
    Annotation annotation = annotateByElement ? annotateElement(message, holder, element)
                                              : annotateTextRange(message, holder, textRange);

    if (annotation == null) {
      return;
    }

    List<? extends IntentionAction> fixes = null;
    if ("STATIC_WARNING".equals(message.getSubSystem())) {
      DartResolverErrorCode code = DartResolverErrorCode.findError(message.getErrorCode());
      fixes = code == null ? Collections.<IntentionAction>emptyList() : code.getFixes(file, startOffset, message);
    }
    else if ("STATIC_TYPE_WARNING".equals(message.getSubSystem())) {
      DartTypeErrorCode code = DartTypeErrorCode.findError(message.getErrorCode());
      fixes = code == null ? Collections.<IntentionAction>emptyList() : code.getFixes(file, startOffset, message);
    }
    else {
      fixes = Collections.emptyList();
    }

    for (IntentionAction intentionAction : fixes) {
      if (intentionAction instanceof FixAndIntentionAction) {
        ((FixAndIntentionAction)intentionAction).setElement(element);
      }
      annotation.registerFix(intentionAction);
    }
  }

  @Nullable
  private static Annotation annotateElement(AnalyzerMessage message, AnnotationHolder holder, @NotNull PsiElement element) {
    switch (message.getType()) {
      case INFO:
        return holder.createInfoAnnotation(element, message.getMessage());
      case WARNING:
        return holder.createWarningAnnotation(element, message.getMessage());
      case ERROR:
        return holder.createErrorAnnotation(element, message.getMessage());
    }
    return null;
  }

  @Nullable
  private static Annotation annotateTextRange(AnalyzerMessage message, AnnotationHolder holder, TextRange textRange) {
    switch (message.getType()) {
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
