package org.angularjs.cli;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIFilter extends AbstractFileHyperlinkFilter implements DumbAware {
  private static final String CREATE = "create ";

  public AngularCLIFilter(Project project, String baseDir) {
    super(project, baseDir);
  }

  @NotNull
  @Override
  public List<FileHyperlinkRawData> parse(@NotNull String line) {
    final int index = line.indexOf(CREATE);
    if (index >= 0) {
      final int start = index + CREATE.length();
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
