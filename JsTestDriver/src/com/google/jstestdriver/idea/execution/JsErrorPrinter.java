package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.util.JsErrorMessage;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.testframework.ui.TestsOutputConsolePrinter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.StringTokenizer;

/**
 * @author Sergey Simonchik
 */
public class JsErrorPrinter extends TestsOutputConsolePrinter {

  private final Project myProject;
  private final File myBasePath;

  public JsErrorPrinter(@NotNull BaseTestsOutputConsoleView consoleView, @NotNull File basePath) {
    super(consoleView, consoleView.getProperties(), null);
    myProject = consoleView.getProperties().getProject();
    myBasePath = basePath;
  }

  @Override
  public void print(String text, ConsoleViewContentType contentType) {
    if (contentType == ConsoleViewContentType.ERROR_OUTPUT) {
      text = text.replace("\r\n", "\n");
      StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        boolean printed = printErrorMessageIfPossible(token);
        if (!printed) {
          defaultPrint(token, contentType);
        }
      }
    } else {
      defaultPrint(text, contentType);
    }
  }

  private boolean printErrorMessageIfPossible(@NotNull String errorText) {
    JsErrorMessage message = JsErrorMessage.parseFromText(errorText, myBasePath);
    if (message == null) {
      return false;
    }
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(message.getFileWithError());
    if (virtualFile != null && virtualFile.isValid()) {
      int column = ObjectUtils.notNull(message.getColumnNumber(), 0);
      HyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(myProject,
                                                              virtualFile,
                                                              message.getLineNumber() - 1,
                                                              column);
      defaultPrint(errorText.substring(0, message.getHyperlinkStartInclusiveInd()), ConsoleViewContentType.ERROR_OUTPUT);
      printHyperlink(errorText.substring(message.getHyperlinkStartInclusiveInd(), message.getHyperlinkEndExclusiveInd()), hyperlinkInfo);
      defaultPrint(errorText.substring(message.getHyperlinkEndExclusiveInd()), ConsoleViewContentType.ERROR_OUTPUT);
      return true;
    }

    return false;
  }

  private void defaultPrint(String text, ConsoleViewContentType contentType) {
    super.print(text, contentType);
  }

}
