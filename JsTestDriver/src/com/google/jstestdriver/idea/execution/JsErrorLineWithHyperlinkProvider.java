package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.util.JsErrorMessage;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.javascript.testFramework.util.LineWithHyperlink;
import com.intellij.javascript.testFramework.util.LineWithHyperlinkProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class JsErrorLineWithHyperlinkProvider implements LineWithHyperlinkProvider {

  private final File myBasePath;

  public JsErrorLineWithHyperlinkProvider(@NotNull File basePath) {
    myBasePath = basePath;
  }

  @Nullable
  @Override
  public LineWithHyperlink getLineWithHyperlink(@NotNull Project project, @NotNull String errorText) {
    JsErrorMessage message = JsErrorMessage.parseFromText(errorText, myBasePath);
    if (message == null) {
      return null;
    }
    VirtualFile virtualFile = VfsUtil.findFileByIoFile(message.getFileWithError(), false);
    if (virtualFile != null && virtualFile.isValid()) {
      int column = ObjectUtils.notNull(message.getColumnNumber(), 0);
      HyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(project,
                                                              virtualFile,
                                                              message.getLineNumber() - 1,
                                                              column);
      return new LineWithHyperlink(message.getHyperlinkStartInclusiveInd(), message.getHyperlinkEndExclusiveInd(), hyperlinkInfo);
    }
    return null;
  }

}
