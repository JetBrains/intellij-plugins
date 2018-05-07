package org.angularjs.cli;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIFilter extends AbstractFileHyperlinkFilter implements DumbAware {
  private static final String CREATE = "create ";
  private static final String UPDATE = "update ";

  public AngularCLIFilter(Project project, String baseDir) {
    super(project, baseDir);
  }

  @NotNull
  @Override
  public List<FileHyperlinkRawData> parse(@NotNull String line) {
    List<FileHyperlinkRawData> create = parse(line, CREATE);
    return !create.isEmpty() ? create : parse(line, UPDATE);
  }

  @NotNull
  public List<FileHyperlinkRawData> parse(@NotNull String line, @NotNull String prefix) {
    int index = StringUtil.indexOfIgnoreCase(line, prefix, 0);
    if (index >= 0) {
      final int start = index + prefix.length();
      int end = line.indexOf(" (", start);
      if (end == -1) end = line.length();
      final String fileName = line.substring(start, end).trim();
      return Collections.singletonList(new FileHyperlinkRawData(fileName, -1, -1, start, start + fileName.length()));
    }
    return Collections.emptyList();
  }

  @Override
  protected boolean supportVfsRefresh() {
    return true;
  }
}
