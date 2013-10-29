package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartStackTraceMessageFilter implements Filter {
  // main.<anonymous closure>                file:///C:/WebstormProjects/dartUnitWin/MyTest.dart 8:39
  private static final Pattern tracePattern = Pattern.compile(".*\\s(.*)\\s(\\d+):(\\d+).*");
  private static final Pattern oldTracePattern = Pattern.compile(".*\\((.*):(\\d+):(\\d+)\\).*");
  private final Project myProject;
  private final String myLibraryRootPath;

  @Nullable
  public static Result parseLine(String line, @NotNull Project project, @Nullable String libraryRootPath) {
    final Trinity<String, Integer, Integer> position = findPosition(line);
    if (position == null) {
      return null;
    }
    final String fileUrl = position.getFirst();
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
    if (fileUrl.startsWith(DartResolveUtil.PACKAGE_PREFIX) && libraryRootPath != null) {
      String libUrl = VfsUtilCore.pathToUrl(libraryRootPath);
      final VirtualFile libraryRoot = VirtualFileManager.getInstance().findFileByUrl(libUrl);
      final VirtualFile packages = DartResolveUtil.findPackagesFolder(libraryRoot, project);
      if (packages != null) {
        String relativePath = fileUrl.substring(DartResolveUtil.PACKAGE_PREFIX.length());
        relativePath = FileUtil.toSystemIndependentName(relativePath);
        file = VfsUtilCore.findRelativeFile(relativePath, packages);
      }
    }
    if (file == null) {
      return null;
    }
    OpenFileHyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(
      project,
      file,
      position.getSecond(),
      position.getThird()
    );
    final int urlOffset = line.indexOf(fileUrl);
    return new Result(urlOffset, urlOffset + fileUrl.length(), hyperlinkInfo);
  }

  @Nullable
  static Trinity<String, Integer, Integer> findPosition(String line) {
    final Trinity<String, Integer, Integer> position = findPosition(line, tracePattern);
    return position != null ? position : findPosition(line, oldTracePattern);
  }

  @Nullable
  static Trinity<String, Integer, Integer> findPosition(String line, Pattern pattern) {
    final Matcher matcher = pattern.matcher(line.trim());
    if (!matcher.matches()) {
      return null;
    }
    final String fileUrl = matcher.group(1);

    final int lineNumber = Integer.parseInt(matcher.group(2)) - 1;
    final int offset = Integer.parseInt(matcher.group(3)) - 1;
    return Trinity.create(fileUrl, lineNumber, offset);
  }

  public DartStackTraceMessageFilter(@NotNull Project project, @Nullable String libraryRootPath) {
    myProject = project;
    myLibraryRootPath = libraryRootPath;
  }

  @Nullable
  @Override
  public Result applyFilter(String line, int entireLength) {
    try {
      int startOffset = entireLength - line.length();
      Result result = parseLine(line, myProject, myLibraryRootPath);
      return result == null ? null : new Result(
        startOffset + result.highlightStartOffset,
        startOffset + result.highlightEndOffset,
        result.hyperlinkInfo
      );
    }
    catch (ProcessCanceledException e) {
      return null;
    }
  }
}
