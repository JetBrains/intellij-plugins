// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.filter;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KarmaSourceMapStacktraceFilter extends AbstractFileHyperlinkFilter implements DumbAware {
  private static final String SEPARATOR = " <- ";

  private final AbstractFileHyperlinkFilter myBaseFilter;
  private final String myBaseDirName;

  public KarmaSourceMapStacktraceFilter(@NotNull Project project,
                                        @Nullable String baseDir,
                                        @NotNull AbstractFileHyperlinkFilter baseFilter) {
    super(project, baseDir);
    myBaseDirName = StringUtil.isNotEmpty(baseDir) ? PathUtil.getFileName(baseDir) : null;
    myBaseFilter = baseFilter;
  }

  @Override
  public @NotNull List<FileHyperlinkRawData> parse(@NotNull String line) {
    int separatorInd = line.indexOf(SEPARATOR);
    if (separatorInd >= 0) {
      String first = line.substring(0, separatorInd);
      if (!StringUtil.isEmptyOrSpaces(first)) {
        if (first.contains("(") && !first.contains(")") && line.contains(")")) {
          first += ")";
        }
        List<FileHyperlinkRawData> result = myBaseFilter.parse(first);
        if (!result.isEmpty()) {
          return result;
        }
      }
    }
    return myBaseFilter.parse(line);
  }

  @Override
  public @Nullable VirtualFile findFile(@NotNull String filePath) {
    VirtualFile file = super.findFile(filePath);
    if (file == null && filePath.startsWith("/tmp/")) {
      return super.findFile(StringUtil.trimStart(filePath, "/tmp/"));
    }
    if (file == null && myBaseDirName != null && !myBaseDirName.isEmpty() &&
        StringUtil.startsWithIgnoreCase(filePath, myBaseDirName) &&
        filePath.length() > myBaseDirName.length()) {
      char ch = filePath.charAt(myBaseDirName.length());
      if (ch == '\\' || ch == '/') {
        return super.findFile(filePath.substring(myBaseDirName.length() + 1));
      }
    }
    return file;
  }
}
