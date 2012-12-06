package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingDocumentModel;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import org.jetbrains.annotations.NotNull;

public class DartFormattingModel implements FormattingModel {
  private final FormattingModel myModel;

  public DartFormattingModel(final PsiFile file,
                           CodeStyleSettings settings,
                           final Block rootBlock) {
    Document document = FormattingDocumentModelImpl.getDocumentToBeUsedFor(file);
    if (document != null && FormattingDocumentModelImpl.canUseDocumentModel(document, file) &&
        file instanceof JSFile && file.getContext() == null) {
      myModel = new DocumentBasedFormattingModel(rootBlock, file.getProject(), settings, file.getFileType(), file);
    } else {
      myModel = FormattingModelProvider.createFormattingModelForPsiFile(file, rootBlock, settings);
    }
  }

  @NotNull
  public Block getRootBlock() {
    return myModel.getRootBlock();
  }

  @NotNull
  public FormattingDocumentModel getDocumentModel() {
    return myModel.getDocumentModel();
  }

  public TextRange replaceWhiteSpace(TextRange textRange, String whiteSpace) {
    return myModel.replaceWhiteSpace(textRange, whiteSpace);
  }

  public TextRange shiftIndentInsideRange(TextRange range, int indent) {
    return myModel.shiftIndentInsideRange(range, indent);
  }

  public void commitChanges() {
    myModel.commitChanges();
  }
}
