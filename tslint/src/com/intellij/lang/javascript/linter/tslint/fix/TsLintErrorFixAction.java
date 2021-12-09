package com.intellij.lang.javascript.linter.tslint.fix;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.javascript.linter.JSLinterFixSingleErrorBaseAction;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintFixInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.StringUtilRt;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;


public class TsLintErrorFixAction extends JSLinterFixSingleErrorBaseAction {

  @NotNull
  private final TsLinterError myError;

  private static final Comparator<TsLintFixInfo.TsLintFixReplacements> REPLACEMENTS_COMPARATOR =
    Comparator
      .<TsLintFixInfo.TsLintFixReplacements>comparingInt(value -> value.innerStart + value.innerLength)
      .thenComparingInt(value -> value.innerStart)
      .reversed();

  public TsLintErrorFixAction(@NotNull PsiFile file, @NotNull TsLinterError error, long modificationStamp) {
    super(TsLintBundle.message("tslint.framework.title"), file, error.getCode(), modificationStamp);
    myError = error;
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return super.isAvailable(project, editor, file)
           && myError.getFixInfo() != null
           && !StringUtil.equals(myError.getCode(), "linebreak-style");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    if (!isAvailable(project, editor, file)) {
      return;
    }

    TsLintFixInfo info = myError.getFixInfo();
    if (info == null) {
      return;
    }
    TsLintFixInfo.TsLintFixReplacements[] replacements = info.innerReplacements;
    if (replacements == null || replacements.length == 0) {
      return;
    }
    Arrays.sort(replacements, REPLACEMENTS_COMPARATOR);

    WriteCommandAction.runWriteCommandAction(project, getText(), null, () -> {
      Document document = editor.getDocument();
      String separator = FileDocumentManager.getInstance().getLineSeparator(file.getViewProvider().getVirtualFile(), project);
      if (!applyReplacements(document, separator, replacements)) return;

      PsiDocumentManager.getInstance(project).commitDocument(document);
    });

    DaemonCodeAnalyzer.getInstance(project).restart(file);
  }

  private static boolean applyReplacements(@NotNull Document document,
                                           @NotNull String separator,
                                           TsLintFixInfo.TsLintFixReplacements @NotNull [] replacements) {
    String lf = LineSeparator.LF.getSeparatorString();
    if (lf.equals(separator)) {
      return applyFor(document.getTextLength(), replacements,
                      replacement -> document
                        .replaceString(replacement.innerStart, replacement.innerStart + replacement.innerLength, StringUtil
                          .notNullize(replacement.innerText)));
    }
    StringBuilder newContent = new StringBuilder(StringUtilRt.convertLineSeparators(document.getText(), separator));
    if (applyFor(newContent.length(), replacements,
                 replacement -> newContent
                   .replace(replacement.innerStart, replacement.innerStart + replacement.innerLength, StringUtil.notNullize(
                     replacement.innerText)))) {
      document.setText(StringUtilRt.convertLineSeparators(newContent, lf));
      return true;
    }
    return false;
  }

  private static boolean applyFor(int documentLength,
                                  TsLintFixInfo.TsLintFixReplacements @NotNull [] replacements,
                                  @NotNull Consumer<TsLintFixInfo.TsLintFixReplacements> apply) {
    for (TsLintFixInfo.TsLintFixReplacements replacement : replacements) {
      int offset = replacement.innerStart;
      if (offset > documentLength || (offset + replacement.innerLength) > documentLength) {
        //incorrect value
        return false;
      }

      apply.consume(replacement);
    }
    return true;
  }
}
