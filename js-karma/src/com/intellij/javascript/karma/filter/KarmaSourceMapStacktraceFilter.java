package com.intellij.javascript.karma.filter;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KarmaSourceMapStacktraceFilter extends AbstractFileHyperlinkFilter implements DumbAware {
  private static final String SEPARATOR = " <- ";

  private final AbstractFileHyperlinkFilter myBaseFilter;

  public KarmaSourceMapStacktraceFilter(@NotNull Project project,
                                        @Nullable String baseDir,
                                        @NotNull AbstractFileHyperlinkFilter baseFilter) {
    super(project, baseDir);
    myBaseFilter = baseFilter;
  }

  @NotNull
  @Override
  public List<FileHyperlinkRawData> parse(@NotNull String line) {
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

  @Nullable
  @Override
  public VirtualFile findFile(@NotNull String filePath) {
    VirtualFile file = super.findFile(filePath);
    if (file == null && filePath.startsWith("/tmp/")) {
      return super.findFile(StringUtil.trimStart(filePath, "/tmp/"));
    }
    return file;
  }
}
