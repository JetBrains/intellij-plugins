// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularCliFilter extends AbstractFileHyperlinkFilter implements DumbAware {
  @NonNls private static final String CREATE = "create ";
  @NonNls private static final String UPDATE = "update ";

  public AngularCliFilter(Project project, String baseDir) {
    super(project, baseDir);
  }

  @Override
  public @NotNull List<FileHyperlinkRawData> parse(@NotNull String line) {
    List<FileHyperlinkRawData> create = parse(line, CREATE);
    return !create.isEmpty() ? create : parse(line, UPDATE);
  }

  public @NotNull List<FileHyperlinkRawData> parse(@NotNull String line, @NotNull String prefix) {
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
