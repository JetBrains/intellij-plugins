package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.common.JsErrorMessage;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class JsErrorFilter implements Filter {

  private static final Logger LOG = Logger.getInstance(JsErrorFilter.class);

  private final Project myProject;
  private final File myBasePath;

  public JsErrorFilter(@NotNull Project project, @NotNull File basePath) {
    myProject = project;
    myBasePath = basePath;
  }

  @Nullable
  @Override
  public Result applyFilter(@NotNull String line, int entireLength) {
    JsErrorMessage message;
    try {
      message = JsErrorMessage.parseFromText(line, myBasePath);
    } catch (Exception e) {
      LOG.error("Can't parse error message from '" + line + "'", e);
      return null;
    }
    if (message == null) {
      return null;
    }
    VirtualFile virtualFile = VfsUtil.findFileByIoFile(message.getFileWithError(), false);
    if (virtualFile == null || !virtualFile.isValid()) {
      return null;
    }
    int column = ObjectUtils.notNull(message.getColumnNumber(), 0);
    HyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(myProject,
                                                            virtualFile,
                                                            message.getLineNumber() - 1,
                                                            column);
    return new Filter.Result(message.getHyperlinkStartInclusiveInd(), message.getHyperlinkEndExclusiveInd(), hyperlinkInfo);
  }

}
