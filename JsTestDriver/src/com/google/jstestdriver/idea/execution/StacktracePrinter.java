package com.google.jstestdriver.idea.execution;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.testframework.ui.TestsOutputConsolePrinter;
import com.intellij.execution.ui.ConsoleViewContentType;
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

public class StacktracePrinter extends TestsOutputConsolePrinter {

  private static final String DEFAULT_PATH_PREFIX = "/test/";
  private static final String COMPONENT_SEPARATOR = ":";
  private static final Pattern COMPONENT_PATTERN = Pattern.compile(Pattern.quote(COMPONENT_SEPARATOR));

  @NotNull
  private final Project myProject;
  private final File myBasePath;
  private final HyperlinkBuilder myHyperlinkBuilder;


  public StacktracePrinter(@NotNull BaseTestsOutputConsoleView consoleView,
                           @NotNull File basePath,
                           @NotNull String browserName) {
    super(consoleView, consoleView.getProperties(), null);
    myProject = consoleView.getProperties().getProject();
    myBasePath = basePath;
    myHyperlinkBuilder = findHyperlinkBuilder(browserName);
  }

  @Nullable
  private static HyperlinkBuilder findHyperlinkBuilder(String browserName) {
    if (browserName == null) {
      return null;
    }
    browserName = browserName.toLowerCase();
    if (browserName.startsWith("chrome")) {
      return ChromeHyperlinkBuilder.INSTANCE;
    } else if (browserName.startsWith("firefox")) {
      return FirefoxHyperlinkBuilder.INSTANCE;
    } else if (browserName.startsWith("opera")) {
      return OperaHyperlinkBuilder.INSTANCE;
    }
    return null;
  }

  @Override
  public void print(String text, ConsoleViewContentType contentType) {
    if (myHyperlinkBuilder == null) {
      defaultPrint(text, contentType);
      return;
    }
    if (contentType == ConsoleViewContentType.ERROR_OUTPUT) {
      text = text.replace("\r\n", "\n");
      StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        if (!writeHyperlinkIfPossible(myHyperlinkBuilder, token)) {
          defaultPrint(token, contentType);
        }
      }
    } else {
      defaultPrint(text, contentType);
    }
  }

  private void defaultPrint(String text, ConsoleViewContentType contentType) {
    super.print(text, contentType);
  }

  private boolean writeHyperlinkIfPossible(@NotNull HyperlinkBuilder hyperlinkBuilder, @NotNull String line) {
    Pattern[] urlPatterns = hyperlinkBuilder.getAllPossibleUrlPatterns();
    for (Pattern urlPattern : urlPatterns) {
      Matcher matcher = urlPattern.matcher(line);
      if (matcher.find()) {
        String text = matcher.group(1);
        HyperlinkInfo hyperlinkInfo = buildHyperlinkInfo(text);
        if (hyperlinkInfo != null) {
          defaultPrint(line.substring(0, matcher.start(1)), ConsoleViewContentType.ERROR_OUTPUT);
          printHyperlink(text, hyperlinkInfo);
          defaultPrint(line.substring(matcher.end(1)), ConsoleViewContentType.ERROR_OUTPUT);
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  private HyperlinkInfo buildHyperlinkInfo(@NotNull String urlWithPosition) {
    String[] components = COMPONENT_PATTERN.split(urlWithPosition);
    if (components.length < 2) {
      return null;
    }
    Integer lastNum = toInteger(components[components.length - 1]);
    if (lastNum == null) {
      return null;
    }
    int zeroBasedLineNo = lastNum - 1;
    Integer zeroBasedColumnNo = null;
    String urlStr = null;
    if (components.length >= 3) {
      Integer preLastNum = toInteger(components[components.length - 2]);
      if (preLastNum != null) {
        urlStr = StringUtil.join(components, 0, components.length - 2, COMPONENT_SEPARATOR);
        zeroBasedLineNo = preLastNum - 1;
        zeroBasedColumnNo = lastNum - 1;
      }
    }
    if (urlStr == null) {
      urlStr = StringUtil.join(components, 0, components.length - 1, COMPONENT_SEPARATOR);
    }
    return createHyperlinkInfoFromParts(urlStr, zeroBasedLineNo, zeroBasedColumnNo);
  }

  @Nullable
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
    if (file != null) {
      VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
      if (virtualFile != null) {
        int column = columnNumber != null ? columnNumber : 0;
        return new OpenFileHyperlinkInfo(myProject, virtualFile, lineNumber, column);
      }
    }
    return null;
  }

  @Nullable
  private File findFileByPath(String urlStr) {
    File file = findFileByBasePath(urlStr);
    if (file != null) {
      return file;
    }
    try {
      URL url = new URL(urlStr);
      String path = url.getPath();
      if (path.startsWith(DEFAULT_PATH_PREFIX)) {
        path = path.substring(DEFAULT_PATH_PREFIX.length());
      }
      return findFileByBasePath(path);
    } catch (MalformedURLException ignored) {
    }
    return null;
  }

  @Nullable
  private File findFileByBasePath(@NotNull String subPath) {
    File file = new File(myBasePath, subPath);
    if (!file.isFile()) {
      File absoluteFile = new File(subPath);
      if (absoluteFile.isAbsolute() && absoluteFile.isFile()) {
        file = absoluteFile;
      }
    }
    return file.isFile() ? file : null;
  }

  private static abstract class HyperlinkBuilder {
    @NotNull
    public abstract Pattern[] getAllPossibleUrlPatterns();
  }

  private static class ChromeHyperlinkBuilder extends HyperlinkBuilder {

    private static final ChromeHyperlinkBuilder INSTANCE = new ChromeHyperlinkBuilder();
    private static final Pattern[] URL_PATTERNS = {
        Pattern.compile("^\\s*at\\s.*\\(([^\\(]*)\\)$"),
        Pattern.compile("^\\s*at\\s*(http://[^\\(]*)$")
    };

    @NotNull
    @Override
    public Pattern[] getAllPossibleUrlPatterns() {
      return URL_PATTERNS;
    }
  }

  private static class FirefoxHyperlinkBuilder extends HyperlinkBuilder {

    private static final FirefoxHyperlinkBuilder INSTANCE = new FirefoxHyperlinkBuilder();
    private static final Pattern[] FIREFOX_URL_WITH_LINE = {
        Pattern.compile("^\\s*\\w*\\(.*\\)@([^\\(]*)$"),
        Pattern.compile("^\\s*@([^\\(]*)$")
    };

    @NotNull
    @Override
    public Pattern[] getAllPossibleUrlPatterns() {
      return FIREFOX_URL_WITH_LINE;
    }
  }

  private static class OperaHyperlinkBuilder extends HyperlinkBuilder {

    private static final OperaHyperlinkBuilder INSTANCE = new OperaHyperlinkBuilder();
    private static final Pattern[] PATTERNS = {
        //<anonymous function: window.equals>([arguments not available])@http://localhost:9876/test/qunit-lib/QUnitAdapter.js:57
        Pattern.compile("^.*\\(.*\\)@([^\\(@]*)$")
    };

    @NotNull
    @Override
    public Pattern[] getAllPossibleUrlPatterns() {
      return PATTERNS;
    }
  }

}
