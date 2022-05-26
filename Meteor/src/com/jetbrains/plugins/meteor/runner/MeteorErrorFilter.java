package com.jetbrains.plugins.meteor.runner;

import com.intellij.javascript.nodejs.PatternBasedNodeJsFilter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import java.util.regex.Pattern;


public class MeteorErrorFilter extends PatternBasedNodeJsFilter {

  public static final String START_PATTERN = "(STDERR)";
  public static final String APP_PREFIX = "app/";
  private final String myDirectory;

  public static final Pattern[] MY_PATTERNS = ArrayUtil.mergeArrays(ALL_PATTERNS, new Pattern[]{
    //   hello.js:24:2: Unexpected token ILLEGAL
    Pattern.compile("^\\s*(\\S.*):(\\d+):(\\d+)(?:.*)?$"),
  });


  public MeteorErrorFilter(@NotNull Project project, @Nullable String workDirectory) {
    super(project);
    myDirectory = workDirectory == null ? null : StringUtil.trimEnd(FileUtil.toSystemIndependentName(workDirectory), '/');
  }

  @Override
  public Result applyFilter(@NotNull String outLine, int entireLength) {
    final int index = outLine.indexOf(START_PATTERN);
    if (index >= 0) {
      outLine = outLine.substring(index + START_PATTERN.length());
    }

    return super.applyFilter(outLine, entireLength);
  }

  @Override
  protected Pattern[] getPatterns() {
    return MY_PATTERNS;
  }

  @Override
  protected VirtualFile findVirtualFile(@NotNull String fileName) {
    fileName = FileUtil.toSystemIndependentName(fileName);
    if (myDirectory != null && !fileName.startsWith("/")) {
      //example:
      //W20150819-22:50:07.162(3)? (STDERR)     at app/clinet/hello.js:24:7
      fileName = StringUtil.trimStart(fileName, APP_PREFIX);
      return LocalFileFinder.findFile(myDirectory + "/" + fileName);
    }

    return super.findVirtualFile(fileName);
  }
}
