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

  private static final Pattern LINE_PATTERN = Pattern.compile("^\\s*at (http://localhost:\\d+/base/([^?]+)?.*)$");

  private final Project myProject;
  private final KarmaConfig myConfig;

  public KarmaBrowserErrorFilter(@NotNull Project project, @NotNull KarmaConfig config) {
    myProject = project;
    myConfig = config;
  }

  @Nullable
  @Override
  public Result applyFilter(String line, int entireLength) {
    //at http://localhost:9876/base/spec/personSpec.js?1368878723000:22
    Matcher m = LINE_PATTERN.matcher(line);
    if (m.find()) {
      int hyperlinkStartInd = m.start(1);
      int hyperlinkEndInd = m.end(1);
      File file = findFile(m.group(2));
      if (file != null) {
        VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, false);
        if (virtualFile != null) {
          int lineNumber = getLineFrom(m.group(1));
          lineNumber = Math.max(-1, lineNumber - 1);
          HyperlinkInfo link = new OpenFileHyperlinkInfo(myProject, virtualFile, lineNumber);
          return new Filter.Result(hyperlinkStartInd, hyperlinkEndInd, link);
        }
      }
    }
    return null;
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

}
