package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.idea.config.JstdConfigStructure;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.ui.TestsOutputConsolePrinter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StacktracePrinter extends TestsOutputConsolePrinter {

  private static final String DEFAULT_PATH_PREFIX = "/test/";

  private final JstdConfigStructure myConfigStructure;
  private final HyperlinkInfoHelper myHyperlinkInfoHelper;

  public StacktracePrinter(SMTRunnerConsoleView consoleView, JstdConfigStructure configStructure, String browserName) {
    super(consoleView, consoleView.getProperties(), null);
    myConfigStructure = configStructure;
    myHyperlinkInfoHelper = findHyperlinkPrinter(browserName);
  }

  @Nullable
  private static HyperlinkInfoHelper findHyperlinkPrinter(String browserName) {
    if (browserName == null) {
      return null;
    }
    browserName = browserName.toLowerCase();
    if (browserName.startsWith("chrome")) {
      return ChromeHyperlinkInfoHelper.INSTANCE;
    } else if (browserName.startsWith("firefox")) {
      return FirefoxHyperlinkInfoHelper.INSTANCE;
    }
    return null;
  }

  public void defaultPrint(String text, ConsoleViewContentType contentType) {
    super.print(text, contentType);
  }

  @Override
  public void print(String text, ConsoleViewContentType contentType) {
    if (myHyperlinkInfoHelper == null) {
      defaultPrint(text, contentType);
      return;
    }
    if (contentType == ConsoleViewContentType.ERROR_OUTPUT) {
      text = text.replace("\r\n", "\n");
      StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        if (!writeHyperlinkIfPossible(myHyperlinkInfoHelper, token, contentType)) {
          defaultPrint(token, contentType);
        }
      }
    } else {
      defaultPrint(text, contentType);
    }
  }

  private boolean writeHyperlinkIfPossible(HyperlinkInfoHelper hyperlinkInfoHelper, String line, ConsoleViewContentType consoleViewContentType) {
    Pattern[] urlPatterns = hyperlinkInfoHelper.getAllPossibleUrlPatterns();
    for (Pattern urlPattern : urlPatterns) {
      Matcher matcher = urlPattern.matcher(line);
      if (matcher.find()) {
        String text = matcher.group(1);
        HyperlinkInfo hyperlinkInfo = buildHyperlinkInfo(text, hyperlinkInfoHelper);
        if (hyperlinkInfo != null) {
          defaultPrint(line.substring(0, matcher.start(1)), consoleViewContentType);
          printHyperlink(text, hyperlinkInfo);
          defaultPrint(line.substring(matcher.end(1)), consoleViewContentType);
          return true;
        }
      }
    }
    return false;
  }

  private HyperlinkInfo buildHyperlinkInfo(String urlWithPosition, HyperlinkInfoHelper hyperlinkInfoHelper) {
    String[] components = urlWithPosition.split(":");
    if (components.length < 2) {
      return null;
    }
    Integer lastNum = toInteger(components[components.length - 1]);
    if (lastNum == null) {
      return null;
    }
    int lineNo = lastNum;
    Integer columnNo = null;
    String urlStr = StringUtil.join(components, 0, components.length - 1, ":");
    if (components.length >= 3) {
      Integer preLastNum = toInteger(components[components.length - 2]);
      if (preLastNum != null) {
        urlStr = StringUtil.join(components, 0, components.length - 2, ":");
        lineNo = preLastNum;
        columnNo = lastNum;
      }
    }
    lineNo = hyperlinkInfoHelper.zeroBaseLineNo(lineNo);
    if (columnNo != null) {
      columnNo = hyperlinkInfoHelper.zeroBaseColumnNo(columnNo);
    }
    return createHyperlinkInfoFromParts(urlStr, lineNo, columnNo);
  }

  private static Integer toInteger(@NotNull String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Creates HyperlinkInfo instance by structured info.
   * @param url JS file url for navigation ('http://localhost:9876/test/qunit-test.js' or 'test/qunit-test.js')
   * @param lineNumber zero-based line number for navigation
   * @param columnNumber zero-based column number for navigation
   * @return computed HyperlinkInfo instance of null if {@code url} matches no files.
   */
  @Nullable
  private HyperlinkInfo createHyperlinkInfoFromParts(@NotNull String url,
                                                     final int lineNumber,
                                                     @Nullable final Integer columnNumber) {
    final File file = findFileByPath(url);
    if (file == null) {
      return null;
    }
    return new HyperlinkInfo() {
      @Override
      public void navigate(final Project project) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          @Override
          public void run() {
            final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
            if (virtualFile != null) {
              Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
              if (document != null) {
                int startLineNumber = document.getLineStartOffset(lineNumber);
                int resultOffset = startLineNumber + (columnNumber != null ? columnNumber : 0);
                OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile, resultOffset);
                openFileDescriptor.navigate(true);
              }
            }
          }
        });
      }
    };
  }

  private File findFileByPath(String urlStr) {
    File file = myConfigStructure.findLoadFile(urlStr);
    if (file != null) {
      return file;
    }
    try {
      URL url = new URL(urlStr);
      String path = url.getPath();
      if (path.startsWith(DEFAULT_PATH_PREFIX)) {
        path = path.substring(DEFAULT_PATH_PREFIX.length());
      }
      return myConfigStructure.findLoadFile(path);
    } catch (MalformedURLException ignored) {
    }
    return null;
  }

  private static abstract class HyperlinkInfoHelper {
    abstract Pattern[] getAllPossibleUrlPatterns();

    int zeroBaseLineNo(int lineNo) {
      return lineNo - 1;
    }

    int zeroBaseColumnNo(int columnNo) {
      return columnNo - 1;
    }
  }

  private static class ChromeHyperlinkInfoHelper extends HyperlinkInfoHelper {

    private static final ChromeHyperlinkInfoHelper INSTANCE = new ChromeHyperlinkInfoHelper();
    private static final Pattern[] URL_PATTERNS = {
        Pattern.compile("^\\s*at\\s.*\\(([^\\(]*)\\)$"),
        Pattern.compile("^\\s*at\\s*(http://[^\\(]*)$")
    };

    @Override
    Pattern[] getAllPossibleUrlPatterns() {
      return URL_PATTERNS;
    }
  }

  private static class FirefoxHyperlinkInfoHelper extends HyperlinkInfoHelper {

    private static final FirefoxHyperlinkInfoHelper INSTANCE = new FirefoxHyperlinkInfoHelper();
    private static final Pattern[] FIREFOX_URL_WITH_LINE = {
        Pattern.compile("^\\s*\\w*\\(.*\\)@([^\\(]*)$")
    };

    @Override
    Pattern[] getAllPossibleUrlPatterns() {
      return FIREFOX_URL_WITH_LINE;
    }
  }

}
