package com.intellij.javascript.karma.filter;

import com.intellij.execution.filters.PatternBasedFileHyperlinkFilter;
import com.intellij.execution.filters.PatternBasedFileHyperlinkRawDataFinder;
import com.intellij.execution.filters.PatternHyperlinkFormat;
import com.intellij.execution.filters.PatternHyperlinkPart;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class KarmaBrowserErrorFilter extends PatternBasedFileHyperlinkFilter {

  public static final PatternBasedFileHyperlinkRawDataFinder FINDER = new PatternBasedFileHyperlinkRawDataFinder(
    new PatternHyperlinkFormat[] {
      //at http://localhost:9876/base/spec/personSpec.js?1368878723000:22
      new PatternHyperlinkFormat(Pattern.compile("^\\s*at (http://[^:]+:\\d+/base/([^?]+).*(:\\d+))$"),
                                 false, false,
                                 PatternHyperlinkPart.HYPERLINK, PatternHyperlinkPart.PATH, PatternHyperlinkPart.LINE),
      //at http://localhost:9876/absolute/home/segrey/WebstormProjects/karma-chai-sample/test/test.js?1378466989000:1
      new PatternHyperlinkFormat(Pattern.compile("^\\s*at (http://[^:]+:\\d+/absolute([^?]+).*(:\\d+))$"),
                                 false, false,
                                 PatternHyperlinkPart.HYPERLINK, PatternHyperlinkPart.PATH, PatternHyperlinkPart.LINE),
    }
  );

  public KarmaBrowserErrorFilter(@NotNull Project project, @NotNull String basePath) {
    super(project, basePath, FINDER);
  }
}
