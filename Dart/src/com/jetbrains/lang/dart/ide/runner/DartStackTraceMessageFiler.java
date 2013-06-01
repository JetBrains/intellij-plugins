package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartStackTraceMessageFiler implements Filter {
  // #0      DefaultFailureHandler.fail (file:///C:/dart/dart-sdk/lib/unittest/expect.dart:85:5)
  private static final Pattern tracePattern = Pattern.compile("(#[0-9]+\\s+)(.*)\\s+\\((.*):(\\d+):(\\d+)\\)");
  private final Project myProject;
  private final String myLibraryRootPath;

  @Nullable
  public static Result parseLine(String line, @NotNull Project project, @Nullable String libraryRootPath) {
    final Matcher matcher = tracePattern.matcher(line.trim());
    if (!matcher.matches()) {
      return null;
    }
    final String prefix = matcher.group(1);
    final String name = matcher.group(2);
    final String fileUrl = matcher.group(3);

    final int lineNumber = Integer.parseInt(matcher.group(4)) - 1;
    final int offset = Integer.parseInt(matcher.group(5)) - 1;
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
    if (fileUrl.startsWith(DartResolveUtil.PACKAGE_PREFIX) && libraryRootPath != null) {
      String libUrl = VfsUtilCore.pathToUrl(libraryRootPath);
      final VirtualFile libraryRoot = VirtualFileManager.getInstance().findFileByUrl(libUrl);
      final VirtualFile packages = DartResolveUtil.findPackagesFolder(libraryRoot, project);
      if (packages != null) {
        String relativePath = fileUrl.substring(DartResolveUtil.PACKAGE_PREFIX.length());
        relativePath = FileUtil.toSystemIndependentName(relativePath);
        file = VfsUtil.findRelativeFile(packages, relativePath.split("/"));
      }
    }
    if (file == null) {
      return null;
    }
    OpenFileHyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(
      project,
      file,
      lineNumber,
      offset
    );
    return new Result(prefix.length(), prefix.length() + name.length(), hyperlinkInfo);
  }

  public DartStackTraceMessageFiler(@NotNull Project project, @Nullable String libraryRootPath) {
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
