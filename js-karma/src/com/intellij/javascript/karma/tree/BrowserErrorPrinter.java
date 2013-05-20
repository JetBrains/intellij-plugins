package com.intellij.javascript.karma.tree;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.testframework.ui.TestsOutputConsolePrinter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class BrowserErrorPrinter extends TestsOutputConsolePrinter {

  private static final Pattern LINE_PATTERN = Pattern.compile("^\\s*at (http://localhost:\\d+/base/([^?]+)?.*)$");
  private final KarmaConfig myConfig;
  private final Project myProject;

  public BrowserErrorPrinter(@NotNull BaseTestsOutputConsoleView testsOutputConsoleView,
                             @NotNull KarmaConfig config) {
    super(testsOutputConsoleView, testsOutputConsoleView.getProperties(), null);
    myProject = testsOutputConsoleView.getProperties().getProject();
    myConfig = config;
  }

  @Override
  public void print(String text, ConsoleViewContentType contentType) {
    if (contentType == ConsoleViewContentType.ERROR_OUTPUT) {
      text = text.replace("\r\n", "\n");
      StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);
      while (tokenizer.hasMoreTokens()) {
        String line = tokenizer.nextToken();
        if (!writeHyperlinkIfPossible(line)) {
          defaultPrint(line, contentType);
        }
      }
    } else {
      defaultPrint(text, contentType);
    }
  }

  private void defaultPrint(String text, ConsoleViewContentType contentType) {
    super.print(text, contentType);
  }

  private boolean writeHyperlinkIfPossible(@NotNull String line) {
    //at http://localhost:9876/base/spec/personSpec.js?1368878723000:22
    Matcher m = LINE_PATTERN.matcher(line);
    if (m.find()) {
      String path = m.group(1);
      File file = findFile(path);
      if (file != null) {
        VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, false);
        if (virtualFile != null) {
          HyperlinkInfo link = new OpenFileHyperlinkInfo(myProject, virtualFile, -1);
          printHyperlink(line.substring(0, m.end(1)), link);
          defaultPrint(line.substring(m.end(1)), ConsoleViewContentType.ERROR_OUTPUT);
          return true;
        }
      }
    }
    return false;
  }

  private File findFile(@NotNull String path) {
    String basePath = myConfig.getBasePath();
    if (basePath != null) {
      File baseDir = new File(basePath);
      if (baseDir.isDirectory()) {
        File file = new File(baseDir, path);
        if (file.isFile()) {
          return file;
        }
      }
    }
    return null;
  }

  public static void main(String[] args) {
    String line = "at http://localhost:9876/base/spec/personSpec.js?1368878723000:22";
    Matcher m = LINE_PATTERN.matcher(line);
    if (m.find()) {
      System.out.println("Found");
      System.out.println(m.group(1));
      System.out.println(line.substring(0, m.start(1)));
    }
  }
}
