package com.jetbrains.lang.dart.ide.template.postfix;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import org.dartlang.analysis.server.protocol.Position;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DartRemotePostfixTemplate extends PostfixTemplate {

  protected DartRemotePostfixTemplate(@NotNull String name, @NotNull String key, @NotNull String example) {
    super(name, key, example);
  }

  @Override
  public boolean isApplicable(@NotNull PsiElement context, @NotNull Document copyDocument, int newOffset) {
    final Project project = context.getProject();
    final PsiFile psiFile = context.getContainingFile();
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);
    String version = service.getSdkVersion();
    Set<PostfixTemplate> templates = DartPostfixTemplateProvider.getTemplates(version);
    if (templates == null) {
      DartPostfixTemplateProvider.initializeTemplates(service);
      templates = DartPostfixTemplateProvider.getTemplates(version);
    }
    boolean found = false;
    for (PostfixTemplate temp : templates) {
      if (temp.getKey().equals(getKey())) {
        // Ensure the requested template is defined by the analysis server currently in use.
        found = true;
        break;
      }
    }
    if (!found) {
      return false;
    }
    service.updateFilesContent(); // Ignore copyDocument
    return service.edit_isPostfixCompletionApplicable(psiFile.getOriginalFile().getVirtualFile(), newOffset, getKey());
  }

  @Override
  public void expand(@NotNull PsiElement context, @NotNull Editor editor) {
    final Project project = context.getProject();
    final PsiFile psiFile = context.getContainingFile();
    final int offset = editor.getCaretModel().getOffset();
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);
    service.updateFilesContent();
    final SourceChange sourceChange = service.edit_getPostfixCompletion(psiFile.getVirtualFile(), offset, this.getKey());
    if (sourceChange != null) {
      // TODO(messick) Does key need to be restored if no expansion is available?
      try {
        AssistUtils.applySourceChange(project, sourceChange, false);
        Position position = sourceChange.getSelection();
        if (position != null) {
          editor.getCaretModel().moveToOffset(service.getConvertedOffset(psiFile.getVirtualFile(), position.getOffset()));
        }
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
    }
  }
}
