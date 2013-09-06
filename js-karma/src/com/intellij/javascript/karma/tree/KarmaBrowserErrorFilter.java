package com.intellij.javascript.karma.tree;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaBrowserErrorFilter implements Filter {

  private static final Pattern[] LINE_PATTERNS = new Pattern[] {
    //at http://localhost:9876/base/spec/personSpec.js?1368878723000:22
    Pattern.compile("^\\s*at (http://[^:]+:\\d+/base/([^?]+)?.*)$"),

    //at http://localhost:9876/absolute/home/segrey/WebstormProjects/karma-chai-sample/test/test.js?1378466989000:1
    Pattern.compile("^\\s*at (http://[^:]+:\\d+/absolute(/[^?]+)?.*)$")
  };

  private final Project myProject;
  private final KarmaConfig myConfig;

  public KarmaBrowserErrorFilter(@NotNull Project project, @NotNull KarmaConfig config) {
    myProject = project;
    myConfig = config;
  }

  @Nullable
  @Override
  public Result applyFilter(String line, int entireLength) {
    LinkInfo linkInfo = createLinkInfo(line);
    if (linkInfo != null) {
      File file = findFile(linkInfo.getPath());
      if (file != null) {
        VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, false);
        if (virtualFile != null) {
          int lineNumber = Math.max(-1, linkInfo.getLineNumber() - 1);
          HyperlinkInfo link = new OpenFileHyperlinkInfo(myProject, virtualFile, lineNumber);
          return new Filter.Result(linkInfo.getHyperlinkStartInd(), linkInfo.getHyperlinkEndInd(), link);
        }
      }
    }
    return null;
  }

  @Nullable
  public static LinkInfo createLinkInfo(@NotNull String line) {
    Matcher m = findMatcher(line);
    if (m == null) {
      return null;
    }
    int hyperlinkStartInd = m.start(1);
    int hyperlinkEndInd = m.end(1);
    String path = m.group(2);
    int lineNumber = getLineFrom(m.group(1));
    return new LinkInfo(hyperlinkStartInd, hyperlinkEndInd, path, lineNumber);
  }

  @Nullable
  private static Matcher findMatcher(@NotNull String line) {
    for (Pattern pattern : LINE_PATTERNS) {
      Matcher m = pattern.matcher(line);
      if (m.find()) {
        return m;
      }
    }
    return null;
  }

  @Nullable
  private File findFile(@NotNull String path) {
    File absFile = new File(path);
    if (absFile.isAbsolute() && absFile.isFile()) {
      return absFile;
    }
    String basePath = myConfig.getBasePath();
    File baseDir = new File(basePath);
    if (baseDir.isDirectory()) {
      File file = new File(baseDir, path);
      if (file.isFile()) {
        return file;
      }
    }
    return null;
  }

  private static int getLineFrom(@NotNull String hyperlink) {
    int lastColonInd = hyperlink.lastIndexOf(':');
    if (lastColonInd == -1) {
      return -1;
    }
    String lineStr = hyperlink.substring(lastColonInd + 1);
    try {
      return Integer.parseInt(lineStr);
    }
    catch (NumberFormatException e) {
      return -1;
    }
  }

  public static class LinkInfo {
    private final int myHyperlinkStartInd;
    private final int myHyperlinkEndInd;
    private final String myPath;
    private final int myLineNumber;

    public LinkInfo(int hyperlinkStartInd, int hyperlinkEndInd, @NotNull String path, int lineNumber) {
      myHyperlinkStartInd = hyperlinkStartInd;
      myHyperlinkEndInd = hyperlinkEndInd;
      myPath = path;
      myLineNumber = lineNumber;
    }

    public int getHyperlinkStartInd() {
      return myHyperlinkStartInd;
    }

    public int getHyperlinkEndInd() {
      return myHyperlinkEndInd;
    }

    public String getPath() {
      return myPath;
    }

    public int getLineNumber() {
      return myLineNumber;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      LinkInfo linkInfo = (LinkInfo)o;

      if (myHyperlinkEndInd != linkInfo.myHyperlinkEndInd) return false;
      if (myHyperlinkStartInd != linkInfo.myHyperlinkStartInd) return false;
      if (myLineNumber != linkInfo.myLineNumber) return false;
      if (!myPath.equals(linkInfo.myPath)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = myHyperlinkStartInd;
      result = 31 * result + myHyperlinkEndInd;
      result = 31 * result + myPath.hashCode();
      result = 31 * result + myLineNumber;
      return result;
    }
  }

}
