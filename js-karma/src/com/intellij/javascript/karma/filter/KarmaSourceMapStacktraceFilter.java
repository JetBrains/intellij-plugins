package com.intellij.javascript.karma.filter;

import com.intellij.execution.filters.*;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KarmaSourceMapStacktraceFilter extends AbstractFileHyperlinkFilter implements DumbAware {
  private static final String SEPARATOR = " <- ";
  private static final String WEBPACK_URL_PREFIX = "webpack:///";
  public static final KarmaSourceMapStacktraceFinder FINDER = new KarmaSourceMapStacktraceFinder();

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
    if (line.contains(SEPARATOR)) {
      List<FileHyperlinkRawData> list = FINDER.find(line);
      if (!list.isEmpty()) {
        return list;
      }
    }
    return myBaseFilter.parse(line);
  }

  public static class KarmaSourceMapStacktraceFinder implements FileHyperlinkRawDataFinder {
    private static final Pattern[] PATTERNS = new Pattern[] {
      Pattern.compile("^\\s*at\\s.*\\(([^(]*:\\d+:\\d+) <- .*"),
      Pattern.compile("^\\s*at\\s+([^\\s(].*:\\d+:\\d+) <- .*"),
      Pattern.compile("^[^@]*[^/]@(.*:\\d+:\\d+) <- .*"),
      Pattern.compile("^\\s*([^\\s].*:\\d+:\\d+) <- .*")
    };

    private static final PatternBasedFileHyperlinkRawDataFinder INNER_FINDER = new PatternBasedFileHyperlinkRawDataFinder(
      new PatternHyperlinkFormat[] {
        new PatternHyperlinkFormat(
          Pattern.compile("(.*):(\\d+):(\\d+)"), false, false,
          PatternHyperlinkPart.PATH, PatternHyperlinkPart.LINE, PatternHyperlinkPart.COLUMN
        )
      }
    );

    @NotNull
    @Override
    public List<FileHyperlinkRawData> find(@NotNull String line) {
      for (Pattern pattern : PATTERNS) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          List<FileHyperlinkRawData> result = ContainerUtil.newArrayList();
          for (int i = 1; i <= matcher.groupCount(); i++) {
            String originalLink = matcher.group(i);
            String normalizedLink = normalizeLinkPrefix(originalLink);
            List<FileHyperlinkRawData> list = INNER_FINDER.find(normalizedLink);
            for (FileHyperlinkRawData data : list) {
              result.add(new FileHyperlinkRawData(data.getFilePath(),
                                                  data.getDocumentLine(),
                                                  data.getDocumentColumn(),
                                                  matcher.start(i),
                                                  matcher.start(i) + originalLink.length()));
            }
          }
          return result;
        }
      }
      return Collections.emptyList();
    }

    @NotNull
    private static String normalizeLinkPrefix(@NotNull String link) {
      if (link.startsWith(WEBPACK_URL_PREFIX)) {
        link = link.substring(WEBPACK_URL_PREFIX.length());
        if (link.startsWith("~/")) {
          link = NodeModuleUtil.NODE_MODULES + link.substring(1);
        }
      }
      return link;
    }
  }
}
