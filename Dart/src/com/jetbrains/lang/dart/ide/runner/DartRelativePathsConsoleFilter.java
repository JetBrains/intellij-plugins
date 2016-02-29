package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// resolves paths relative to working dir that are reported by package:test or 'pub build'
public class DartRelativePathsConsoleFilter implements Filter {
  @NotNull private final Project myProject;
  @NotNull private final String myBaseDirPath;

  public DartRelativePathsConsoleFilter(@NotNull final Project project, @NotNull final String baseDirPath) {
    myProject = project;
    myBaseDirPath = baseDirPath;
  }

  @Nullable
  public Result applyFilter(final String text, final int entireLength) {
    final String trimmedText = StringUtil.trimLeading(text);
    final Trinity<String, Integer, Integer> fileRelPathLineAndColumn = getFileRelPathLineAndColumn(trimmedText);
    if (fileRelPathLineAndColumn == null) return null;

    final String fileRelPath = fileRelPathLineAndColumn.first;
    final int line = fileRelPathLineAndColumn.second;
    final int column = fileRelPathLineAndColumn.third;

    final VirtualFile file =
      LocalFileSystem.getInstance().findFileByPath(myBaseDirPath + "/" + trimmedText.substring(0, fileRelPath.length()));
    if (file == null || file.isDirectory()) return null;

    return new Result(entireLength - trimmedText.length(),
                      entireLength - trimmedText.length() + fileRelPath.length(),
                      new OpenFileHyperlinkInfo(myProject, file, line, column));
  }

  @Nullable
  public static Trinity<String, Integer, Integer> getFileRelPathLineAndColumn(@NotNull final String text) {
    // "web\anagram.dart:23:8:"
    // "  ../subdir/someFile.dart 73:29         main.<fn>"
    if (text.isEmpty()) return null;
    if (text.charAt(0) != '.' && !Character.isJavaIdentifierStart(text.charAt(0))) return null;

    int index = 0;
    while (text.length() > ++index) {
      final char ch = text.charAt(index);
      if (ch != '/' && ch != '\\' && ch != '.' && !Character.isJavaIdentifierPart(ch)) break;
    }

    if (text.length() <= index + 3) return null; // at least 4 symbols more: " 1:1" or ":1:1"

    final String relPath = text.substring(0, index);
    if (!relPath.endsWith(".dart") || (text.charAt(index) != ' ' && text.charAt(index) != ':')) return null;

    final Couple<Integer> lineAndColumn = DartPositionInfo.parseLineAndColumn(text.substring(index + 1));
    final int line = lineAndColumn == null ? -1 : lineAndColumn.first >= 0 ? lineAndColumn.first - 1 : lineAndColumn.first;
    final int column = lineAndColumn == null ? -1 : lineAndColumn.second >= 0 ? lineAndColumn.second - 1 : lineAndColumn.second;
    if (line < 0 || column < 0) return null;

    return Trinity.create(relPath, line, column);
  }
}
