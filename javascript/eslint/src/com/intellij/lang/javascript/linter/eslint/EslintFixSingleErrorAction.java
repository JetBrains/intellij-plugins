package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.javascript.linter.JSLinterFixSingleErrorBaseAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.StringUtilRt;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EslintFixSingleErrorAction extends JSLinterFixSingleErrorBaseAction implements DumbAware {

  @SafeFieldForPreview private final @NotNull EslintError.FixInfo myFixInfo;

  public EslintFixSingleErrorAction(@NotNull @IntentionName String toolName,
                                    @NotNull PsiFile file,
                                    @NotNull EslintError.FixInfo fixInfo,
                                    @Nullable String errorCode,
                                    long modificationStamp) {
    super(toolName, file, errorCode, modificationStamp);
    myFixInfo = fixInfo;
  }

  @Override
  public @NotNull String getText() {
    if (myFixInfo.description != null) {
      return myToolName + ": " + myFixInfo.description;
    }
    return super.getText();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
    return super.isAvailable(project, editor, psiFile)
           && !StringUtil.equals(myErrorCode, "linebreak-style");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
    if (!isAvailable(project, editor, psiFile)) {
      return;
    }

    Document document = editor.getDocument();
    String separator = FileDocumentManager.getInstance().getLineSeparator(psiFile.getViewProvider().getVirtualFile(), project);
    WriteCommandAction.runWriteCommandAction(project, getText(), null, () -> applyReplacement(document, separator, myFixInfo));

    DaemonCodeAnalyzer.getInstance(project).restart(psiFile, this);
  }

  private static void applyReplacement(@NotNull Document document,
                                       @NotNull String separator,
                                       @NotNull EslintError.FixInfo fixInfo) {
    String lf = LineSeparator.LF.getSeparatorString();
    if (lf.equals(separator)) {
      document.replaceString(fixInfo.startOffset, fixInfo.endOffset, fixInfo.replacementText);
    }
    else {
      StringBuilder result = new StringBuilder(StringUtil.convertLineSeparators(document.getText(), separator));
      result.replace(fixInfo.startOffset, fixInfo.endOffset, fixInfo.replacementText);
      document.setText(StringUtilRt.convertLineSeparators(result.toString(), lf));
    }
  }
}
